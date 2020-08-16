package music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import commands.MusicCommands
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.core.entities.Guild
import sources.MusicSource
import sources.get
import uadamusic.*
import utils.MusicContextState
import utils.exception.MusicContextInitializationException
import java.lang.IllegalArgumentException
import java.nio.file.Paths
import java.util.*

sealed class Playable {
    class Music(val data: MusicData): Playable()
    class Url(val url: String, val title: String = url): Playable()


    fun getIdentifier(): String =
        when (this) {
            is Url -> url
            is Music -> data.path.let {
                if ("://" in it) it else it.replace("//", "/")
            }
        }

    fun getImage(default: String? = null): String? =
        when (this) {
            is Url -> default
            is Music -> data.img ?: default
        }

    fun fromInfo(info: AudioTrackInfo): Playable =
        when (this) {
            is Url -> Url(info.uri, info.title ?: info.uri)
            is Music -> this
        }
}

object MusicHandler {

    private val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val musicManagers: MutableMap<Long, GuildMusicManager> = mutableMapOf()

    private var contextState: MusicContextState = MusicContextState(RuntimeException("Not initialized yet"))

    val context: MusicContext
        get() = contextState.context

    val isContextAvailable: Boolean
        get() = contextState.isAvailable

    val AudioTrack.data: Playable
        get() {
            if (info.uri.startsWith("http"))
                return Playable.Url(info.uri, info.title ?: info.uri ?: "")
            return context.search(removeExtension(normalizePath(identifier)))?.first()?.let {
                Playable.Music(it)
            } ?: throw IllegalStateException("Current track cannot be found in current context")
        }

    fun normalizePath(p: String) = Paths.get(context.name).relativize(Paths.get(p)).toString()

    fun removeExtension(p: String) = p.substring(0, p.lastIndexOf('.'))

    val MusicData.imgUrl: String?
        get() {
            return img?.let { if (it.isEmpty()) null else it }?.let { UADAB.cfg.musicMetaUrl + it }
        }

    val MusicData.isSong: Boolean
        get() = type == SONG

    init {
        loadContext()
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    fun loadContext() = runBlocking {
        contextState = try {
            MusicContextState(MusicSource.get())
        } catch (e: Exception) {
            MusicContextState(MusicContextInitializationException("Failed to load music context: ${e.localizedMessage}"))
        }
    }

    @Synchronized
    fun getGuildAudioPlayer(guild: Guild): GuildMusicManager {
        val guildId = guild.idLong
        var musicManager = musicManagers[guildId]

        if (musicManager == null) {
            musicManager = GuildMusicManager(playerManager)
            musicManagers[guildId] = musicManager
        }

        guild.audioManager.sendingHandler = musicManager.sendHandler

        return musicManager
    }

    fun isPaused(guild: Guild): Boolean {
        return getGuildAudioPlayer(guild).player.isPaused
    }

    fun playlistSize(guild: Guild): Int {
        with(getGuildAudioPlayer(guild)) {
            val isPlaying = player.playingTrack != null
            return scheduler.size() + (if (isPlaying) 1 else 0)
        }
    }

    fun pause(guild: Guild) {
        getGuildAudioPlayer(guild).player.isPaused = true
    }

    fun resume(guild: Guild) {
        getGuildAudioPlayer(guild).player.isPaused = false
    }

    fun reset(guild: Guild) {
        with(getGuildAudioPlayer(guild)) {
            scheduler.clear()
            player.stopTrack()
        }
    }

    fun skip(id: Int, guild: Guild): AudioTrack {
        with(getGuildAudioPlayer(guild)) {
            if (id == 0) {
                val cur = player.playingTrack
                scheduler.nextTrack()
                return cur
            }
            return scheduler.skipTrack(id - 1)
        }
    }

    fun currentTrack(guild: Guild): AudioTrack? {
        return getGuildAudioPlayer(guild).player.playingTrack
    }

    /**
     * !IMPORTANT! This function doesn't return currently playing track, use [currentTrack]
     */
    fun getPlaylist(guild: Guild): List<AudioTrack> {
        return getGuildAudioPlayer(guild).scheduler.getPlaylist()
    }

    fun getAllPlaylists(): Map<Guild, List<AudioTrack>> = mapOf(
        *musicManagers.keys.map(UADAB.bot::getGuildById)
            .map { it to getPlaylist(it) }
            .toTypedArray()
    )

    data class MusicArgs(
        var count: Int = 1,
        var noRepeat: Boolean = true,
        var all: Boolean = false,
        var first: Boolean = false,
        var next: Boolean = false
    )

    private fun isInQueue(args: MusicArgs, player: GuildMusicManager, track: AudioTrack): Boolean =
        args.noRepeat && (player.scheduler.hasTack(track) || player.playingTrack?.identifier == track.identifier)

    private fun GuildMusicManager.enqueueTrack(args: MusicArgs, track: AudioTrack) =
        when {
            args.first -> scheduler.playNow(track)
            args.next -> scheduler.playNext(track)
            else -> scheduler.queue(track)
        }

    private fun loadItem(
        playable: Playable,
        args: MusicArgs,
        player: GuildMusicManager
    ): MusicHandlerRet {
        var ret: MusicHandlerRet = MHUnknown(playable)
        playerManager.loadItem(playable.getIdentifier(), object : AudioLoadResultHandler {
            override fun loadFailed(exception: FriendlyException) {
                ret = MHError(exception, playable)
            }
            override fun trackLoaded(track: AudioTrack) {
                ret = if (isInQueue(args, player, track)) {
                    MHAlreadyInQueue(playable.fromInfo(track.info))
                } else {
                    player.enqueueTrack(args, track)
                    MHSuccess(playable.fromInfo(track.info))
                }
            }
            override fun noMatches() {
                ret = MHNotFound(playable)
            }
            override fun playlistLoaded(playlist: AudioPlaylist) {}
        }).get()
        return ret
    }

    private fun loadDirect(data: MusicData, guild: Guild, args: MusicArgs): MusicHandlerRet {
        if (data.type == SONG) {
            return loadItem(Playable.Music(data), args, getGuildAudioPlayer(guild))
        }
        throw IllegalArgumentException("loadDirect should only be called with song data arg")
    }

    fun loadUrl(url: String, guild: Guild, args: MusicArgs): MusicHandlerRet {
        return loadItem(Playable.Url(url), args, getGuildAudioPlayer(guild))
    }

    fun getVariants(name: String) = context.search(name)

    private fun MusicData.getSongs(): List<MusicData> {
        if(isSong) {
            return listOf(this)
        }
        val queue = LinkedList<MusicData>()
        val ret = mutableListOf<MusicData>()
        queue.add(this)
        while (queue.isNotEmpty()) {
            val cur = queue.remove()
            if (cur.isSong) {
                ret.add(cur)
            } else {
                cur.children?.forEach { queue.add(it) }
            }
        }
        return ret
    }

    fun <T> MutableList<T>.removeLast() = removeAt(lastIndex)

    val MusicDataType.level
        get() = when(this) {
            CONTEXT -> 0
            GROUP -> 1
            AUTHOR -> 2
            ALBUM -> 3
            SONG -> 4
            else -> 5
        }

    fun loadAll(data: MusicData, guild: Guild, args: MusicArgs): List<MusicHandlerRet> {
        if(data.type == SONG) {
            return listOf(load(data, guild, args))
        }
        val ret = mutableListOf<MusicHandlerRet>()
        data.children?.sortedWith(Comparator { a, b ->
            if(a.type.level != b.type.level) {
                a.type.level.compareTo(b.type.level)
            } else {
                a.name.compareTo(b.name)
            }
        })?.forEach {
            ret.addAll(loadAll(it, guild, args))
        }
        return ret
    }

    fun load(data: MusicData, guild: Guild, args: MusicArgs): MusicHandlerRet {
        val validSongs = data.getSongs()
        if (validSongs.isEmpty()) {
            return MHNotFound(Playable.Music(data))
        }
        val rets = mutableListOf<MusicHandlerRet>()
        if (validSongs.size == 1) {
            val s = validSongs.first()
            for (i in 0 until args.count) {
                rets.add(loadDirect(s, guild, args))
            }
        } else if (args.all) {
            for (i in 0 until args.count) {
                rets.addAll(loadAll(data, guild, args))
            }
        } else {
            val shuffledSongs = validSongs.shuffled().toMutableList()
            for (i in 0 until args.count) {
                rets.add(
                    if (shuffledSongs.isEmpty()) {
                        MHNoMoreTracks()
                        break
                    } else {
                        loadDirect(shuffledSongs.removeLast(), guild, args)
                    }
                )
            }
        }
        if(rets.size == 1) {
            return rets.first()
        }
        val successCount = rets.count { r -> r is MHSuccess }
        return when {
            successCount == rets.size -> MHFullyLoaded(rets)
            successCount == rets.size - 1 && rets.last() is MHNoMoreTracks -> MHAllLoaded(rets)
            successCount > 0 -> MHPartiallyLoaded(rets, successCount)
            else -> MHNotLoaded()
        }
    }
}