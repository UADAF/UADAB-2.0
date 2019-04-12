package music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import commands.MusicCommands
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.core.entities.Guild
import sources.MusicSource
import sources.get
import uadamusic.MusicContext
import uadamusic.MusicData
import uadamusic.SONG
import java.lang.IllegalArgumentException
import java.nio.file.Paths
import java.util.*

object MusicHandler {

    private val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val musicManagers: MutableMap<Long, GuildMusicManager> = mutableMapOf()

    lateinit var context: MusicContext
        private set

    val AudioTrack.data: MusicData
        get() {
            return context.search(removeExtension(normalizePath(identifier)))!!.first()
        }

    fun normalizePath(p: String) = Paths.get(context.name).relativize(Paths.get(p)).toString()

    fun removeExtension(p: String) = p.substring(0, p.lastIndexOf('.'))

    val MusicData.imgUrl: String?
        get() {
            val i = img
            return if (i == null || i.isEmpty()) {
                null
            } else {
                UADAB.cfg.musicMetaUrl + i
            }
        }

    val MusicData.isSong: Boolean
        get() = type == SONG

    init {
        loadContext()
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    fun loadContext() = runBlocking {
        MusicSource.reload()
        context = MusicSource.get()
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
        var first: Boolean = false
    )

    fun loadDirect(data: MusicData, guild: Guild, args: MusicArgs): MusicHandlerRet {
        if (data.type != SONG) {
            throw IllegalArgumentException("loadDirect should only be called with song data arg")
        }
        val name = data.path
        var ret: MusicHandlerRet? = null
        val player = getGuildAudioPlayer(guild)
        val cleared = if ("://" in name) name else name.replace("//", "/")
        playerManager.loadItem(cleared, object : AudioLoadResultHandler {
            override fun loadFailed(exception: FriendlyException) {
                ret = MHError(exception, data)
            }

            override fun trackLoaded(track: AudioTrack) {
                if (args.noRepeat && (player.scheduler.hasTack(track) || player.playingTrack?.identifier == track.identifier)) {
                    ret = MHAlreadyInQueue(cleared, data, track)
                } else {
                    if (args.first) {
                        player.scheduler.playNow(track)
                    } else {
                        player.scheduler.queue(track)
                    }
                    ret = MHSuccess(cleared, data, track)
                }
            }

            override fun noMatches() {
                ret = MHNotFound(cleared, data)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {}

        }).get()
        return ret ?: MHUnknown(data)
    }

    fun getVariants(name: String) = context.search(name)

    private fun MusicData.getSongs(): List<MusicData> {
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

    fun load(data: MusicData, guild: Guild, args: MusicArgs): MusicHandlerRet {
        if (data.type == SONG) {
            return loadDirect(data, guild, args)
        }
        val validSongs = data.getSongs()
        if (validSongs.isEmpty()) {
            return MHNotFound(data.path, data)
        }
        val rets = mutableListOf<MusicHandlerRet>()
        if (args.all) {
            for (i in 0 until args.count) {
                validSongs.forEach { rets.add(load(it, guild, args)) }
            }
        } else {
            val shuffledSongs = validSongs.shuffled().toMutableList()
            for (i in 0 until args.count) {
                rets.add(
                    if (validSongs.isEmpty()) {
                        MHNoMoreTracks()
                        break
                    } else {
                        load(shuffledSongs.removeLast(), guild, args)
                    }
                )
            }
        }
        if(rets.size == 1) {
            return rets.first()
        }
        val successCount = rets.count { r -> r is MHSuccess }
        return when {
            successCount == args.count -> MHFullyLoaded(rets)
            successCount == rets.size - 1 && rets.last() is MHNoMoreTracks -> MHAllLoaded(rets)
            successCount > 0 -> MHPartiallyLoaded(rets, successCount)
            else -> MHNotLoaded()
        }
    }
}