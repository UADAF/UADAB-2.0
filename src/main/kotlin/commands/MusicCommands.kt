package commands

import Reactions
import UADAB
import cmd.CommandCategory
import cmd.CommandContext
import cmd.CommandListBuilder
import cmd.ICommandList
import commands.MusicCommands.handleLoad
import commands.MusicCommands.replyCat
import dsl.Init
import dsl.PaginatedEmbedCreator
import dsl.embed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import music.*
import music.MusicHandler.data
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageReaction
import net.dv8tion.jda.core.requests.Route
import net.dv8tion.jda.core.requests.restaction.MessageAction
import quoter.util.json
import uadamusic.CONTEXT
import uadamusic.MusicData
import users.UADABUser
import users.admin_or_interface
import users.assets
import utils.extractCount
import java.awt.Color
import java.awt.Color.*
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

object MusicCommands : ICommandList {

    override val cat = CommandCategory("Music", Color(0xAFEBF3), "http://52.48.142.75/images/an.png")

    const val numbermoji = '\u20e3'
    private val playlistTimeFormat = SimpleDateFormat("HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("UTC") }

    const val volumeHelp =
        "Use `volume + n` or `volume - n` to change volume by delta value or `volume n` to set volume"

    val String.fileExtension: String
            get() = split(".").run { if(size > 1) last() else "" }

    override fun init(): Init<CommandListBuilder> = {
        command("play") {
            allowed to assets
            help = "Play"
            val all by parser.flag("all", shortname = 'a')
            val repeat by parser.flag("repeat", shortname = 'r')
            val first by parser.flag("first", shortname = 'f')
            val next by parser.flag("next", shortname = 'n')
            val leftoverArgs by parser.leftoverDelegate()
            action {
                val (count, namel) = extractCount(leftoverArgs)
                val name = namel.joinToString(" ")
                if (first.present && next.present) {
                    replyCat {
                        color = RED
                        title = "Only one of 'first' or 'next' can be specified"
                    }
                }
                val musicArgs = MusicHandler.MusicArgs(
                    count,
                    !(repeat.present || (all.present && count > 1) || name.isNotEmpty()),
                    all.present,
                    first.present,
                    next.present
                )
                when {
                    name.isEmpty() -> MusicHandler.load(MusicHandler.context, guild, musicArgs)
                    name.startsWith("http") -> MusicHandler.loadUrl(name, guild, musicArgs)
                    else -> playQuery(name, musicArgs)
                }?.let { handleLoad(it) }
            }
            onDenied { musicDeny() }
        }
        command("pause") {
            allowed to assets
            help = "Put music on pause"
            action {
                MusicHandler.pause(guild)
                replyCat {
                    color = GREEN
                    title = "Paused"
                    thumbnail = currentImg(guild)
                }
            }
            onDenied { musicDeny() }
        }
        command("resume") {
            allowed to assets
            help = "Resume music"
            action {
                MusicHandler.resume(guild)
                replyCat {
                    color = GREEN
                    title = "Resumed"
                    thumbnail = currentImg(guild)
                }
            }
            onDenied { musicDeny() }
        }
        command("clear") {
            allowed to assets
            help = "Clears playlist"
            aliases {
                +"reset"
            }
            action {
                val img = currentImg(guild)
                MusicHandler.reset(guild)
                replyCat {
                    color = GREEN
                    title = "Cleared"
                    thumbnail = img
                }
            }
            onDenied { musicDeny() }
        }
        command("playlist") {
            help = "Displays playlist"
            action {
                val cur = MusicHandler.currentTrack(guild)
                val pl = MusicHandler.getPlaylist(guild)
                if (cur == null) {
                    replyCat {
                        color = RED
                        title = "No playlist"
                    }
                } else {
                    var totalTime = cur.duration - cur.position
                    reply {
                        pattern {
                            color = GREEN
                            title = "Playlist"
                            thumbnail = cur.data.getImage(cat.img)
                        }
                        +"1: ${formatPlayable(cur.data)} ${(cur.position * 100) / cur.duration}%\n"
                        pl.forEachIndexed { i, e ->
                            +"${i + 2}: ${formatPlayable(e.data)}"
                            if (e.position != 0L) {
                                +" ${(e.position * 100) / e.duration}%"
                            }
                            +"\n"
                            totalTime += e.duration
                        }
                        +"\nTotal time: ${playlistTimeFormat.format(Date(totalTime))}"
                    }
                }
            }
            onDenied { musicDeny() }
        }
        command("skip") {
            allowed to assets
            help = "Skip specified song, use without arg to skip current"
            val skipRange by parser.range("range")
            val skipPlain by parser.leftoverDelegate()
            fun CommandContext.skip(id: Int) {
                val skipped = MusicHandler.skip(id, guild)
                replyPlayable(skipped.data) {
                    color = GREEN
                    title = "Skipped"
                }
            }

            action {
                if (MusicHandler.playlistSize(guild) == 0) {
                    replyCat {
                        color = RED
                        title = "Nothing playing"
                    }
                    return@action
                }
                var (start, end) = skipRange
                if (start == null && end == null) {
                    if (skipPlain.isEmpty()) {
                        skip(0)
                    } else {
                        var n = skipPlain.joinToString("").toInt()
                        if (n < 0) {
                            n += MusicHandler.playlistSize(guild) + 1
                            if (n < 0) {
                                reply {
                                    color = RED
                                    title = "Not enough songs in playlist"
                                    thumbnail = currentImg(guild)
                                }
                                return@action
                            }
                        }
                        if (n > MusicHandler.playlistSize(guild)) {
                            reply {
                                color = RED
                                title = "Not enough songs in playlist"
                                thumbnail = currentImg(guild)
                            }
                        } else {
                            val skipped = MusicHandler.skip(n - 1, guild)
                            replyPlayable(skipped.data) {
                                color = GREEN
                                title = "Skipped"
                            }
                        }
                    }
                } else {
                    if (start == null) {
                        start = 1
                    }
                    if (end == null) {
                        end = MusicHandler.playlistSize(guild) + 1
                    }
                    //start is inclusive, end is exclusive
                    start--
                    end--
                    if (start < 0) {
                        start += MusicHandler.playlistSize(guild) + 1
                        if (start < 0) {
                            replyCat {
                                color = RED
                                title = "Invalid range"
                            }
                            return@action
                        }
                    }
                    if (end < 0) {
                        end += MusicHandler.playlistSize(guild) + 1
                        if (end < 0) {
                            replyCat {
                                color = RED
                                title = "Invalid range"
                            }
                            return@action
                        }
                    }
                    //Always skip start, because skipping changes playlist, so to skip i after n skips, we should skip i - n, and for successive skips it's always starting i
                    val skipped = (start until end).map { MusicHandler.skip(start, guild) }
                    if (skipped.isEmpty()) {
                        replyCat {
                            color = RED
                            title = "Invalid range"
                        }
                    } else {
                        reply {
                            pattern {
                                color = GREEN
                                title = "Skipped"
                                thumbnail = skipped.first().data.getImage(cat.img)
                            }
                            skipped.forEach {
                                +formatPlayable(it.data)
                                +"\n"
                            }
                        }
                    }
                }
            }
            onDenied { musicDeny() }
        }
        command("volume") {
            allowed to assets
            help = "Change volume"

            val arguments by parser.leftoverDelegate()

            action {
                if (arguments.size > 1) {
                    volumeByDelta(arguments[0], arguments[1])
                } else if (arguments.size == 1) {
                    val value = arguments[0]
                    if (value.first() in setOf('+', '-') && value.length > 1) {
                        volumeByDelta(value.first().toString(), value.substring(1))
                    } else {
                        val volumeValue = parseVolumeValue(value) ?: return@action replyCat {
                            title = "Invalid delta value"
                            color = RED
                            +volumeHelp
                            +"Delta must be a positive number in range [0; 100]"
                        }
                        setVolume(volumeValue)
                    }
                }
            }
        }
        command("upload") {
            allowed to assets

            val authorValue by parser.value("author")
            val albumValue by parser.value("album")
            val trackNameValue by parser.leftoverDelegate()

            action {
                if (message.attachments.isEmpty()) {
                    return@action replyCat {
                        title = "No attachment"
                        color = RED
                        +"You need to upload music file with command"
                    }
                }
                val attachment = message.attachments.first()
                val artist = authorValue.value ?: ""
                val album = albumValue.value ?: ""
                var name = trackNameValue.joinToString(" ")

                val extension = name.fileExtension
                val fileExtension = attachment.fileName.fileExtension

                if (extension.isEmpty()) {
                    name += ".mp3"
                }

                if ((extension.isNotEmpty() && extension != "mp3") || fileExtension != "mp3") {
                    return@action replyCat {
                        title = "Invalid music extension"
                        append field "Name" to name
                        append field "File" to attachment.fileName
                        color = RED
                    }
                }

                val musicInfoName = "music.info.json"
                val artistInfo = Paths.get(UADAB.cfg.musicDir, artist, musicInfoName)
                val albumInfo = Paths.get(UADAB.cfg.musicDir, artist, album, musicInfoName)
                val path: java.nio.file.Path = Paths.get(UADAB.cfg.musicDir, artist, album, name)

                reply(embed {
                    title = "Upload"
                    color = GREEN
                    append field "Path" to path
                    if (album.isNotEmpty())
                        inline field "Album" to album
                    if (artist.isNotEmpty())
                        inline field "Author" to artist
                    append field "Track" to name
                    +"Waiting for approve"
                }, success = { msg ->
                    Reactions.register(msg.id) {
                        if (it.user != UADAB.bot.selfUser) {
                            val re = it.reactionEmote
                            if (re.name == "\u2705") {
                                val user = UADABUser.fromDiscord(it.user)
                                if (user.classification in admin_or_interface) {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        val embed = try {
                                            if (Files.notExists(artistInfo)) {
                                                Files.createDirectory(Paths.get(UADAB.cfg.musicDir, artist))
                                                Files.createFile(artistInfo).toFile().writeText(json {
                                                    "type" to "author"
                                                }.toString())
                                            }

                                            if (album.isNotEmpty() && Files.notExists(albumInfo)) {
                                                Files.createDirectory(Paths.get(UADAB.cfg.musicDir, artist, album))
                                                Files.createFile(albumInfo).toFile().writeText(json {
                                                    "type" to "album"
                                                }.toString())
                                            }
                                            attachment.withInputStream { stream ->
                                                Files.copy(stream, Paths.get(path.toFile().absolutePath))
                                            }
                                            embed {
                                                title = "Upload approved"
                                                color = GREEN
                                                append field "Approved by" to user.name
                                                append field "File" to path.toString()
                                                +"File downloaded successfully"
                                            }
                                        } catch (e: Throwable) {
                                            embed {
                                                title = "Upload approved"
                                                color = RED
                                                append field "Approved by" to user.name
                                                inline field "File" to path.toString()
                                                append field "Exception" to e.javaClass.simpleName
                                                append field "Message" to e.localizedMessage
                                            }
                                        }
                                        MessageAction(
                                            msg.jda,
                                            Route.Messages.EDIT_MESSAGE.compile(msg.channel.id, msg.id),
                                            msg.channel
                                        )
                                            .override(true)
                                            .embed(embed.first).queue()
                                        msg.clearReactions().queue()
                                    }
                                    return@register true
                                }
                            } else if (re.name == "\u274C") {
                                msg.delete().queue()
                                return@register true
                            }
                        }
                        return@register false
                    }
                    msg.addReaction("\u2705").queue()
                    msg.addReaction("\u274C").queue()
                })
            }
        }
    }

    private fun CommandContext.playVariants(data: List<MusicData>, musicArgs: MusicHandler.MusicArgs): MusicHandlerRet? {
        reply(embed {
            color = YELLOW
            title = "Select variant"
            data.forEachIndexed { i, e ->
                +"${i + 1}$numbermoji - ${e.type.name.capitalize()} - ${formatData(e)}\n\n"
            }
        }, success = { msg ->
            Reactions.register(msg.id) {
                if (it.user == UADAB.bot.selfUser)
                    return@register false
                val re = it.reactionEmote.name
                if (re.length <= 1 || re[1] != numbermoji)
                    return@register true
                val num = re[0]
                if (num in '1'..('0' + data.size)) {
                    val r = MusicHandler.load(data[num.toString().toInt() - 1], guild, musicArgs)
                    handleLoad(r)
                    //Use getMessageById to update message, because msg is cached version
                    msg.channel.getMessageById(msg.id).complete().reactions.filter(
                        MessageReaction::isSelf
                    ).forEach { rec ->
                        rec.removeReaction().queue()
                    }

                }
                true
            }
            for (j in 1..data.size) {
                msg.addReaction("$j$numbermoji").queue()
            }
        })
        return null
    }

    private fun CommandContext.playNothing(name: String): MusicHandlerRet? {
        replyCat {
            color = RED
            title = "Found a whole lot of nothing"
            +name
        }
        return null
    }

    private fun CommandContext.playSingle(data: List<MusicData>, musicArgs: MusicHandler.MusicArgs): MusicHandlerRet? {
        return MusicHandler.load(data.first(), guild, musicArgs)
    }

    private fun CommandContext.playMany(name: String): MusicHandlerRet? {
        replyCat {
            color = RED
            title = "To many results, try narrowing it down"
            +name
        }
        return null
    }

    private fun CommandContext.playQuery(name: String, musicArgs: MusicHandler.MusicArgs): MusicHandlerRet? =
        MusicHandler.context.search(name)?.let { data ->
            when (data.size) {
                0 -> playNothing(name)
                1 -> playSingle(data, musicArgs)
                in 2..10 -> playVariants(data, musicArgs)
                else -> playMany(name)
            }
        }

    private fun parseVolumeValue(valueString: String): Int? {
        val value = valueString.toIntOrNull()
        if (value == null || value < 0 || value > 100) {
            return null;
        }
        return value
    }

    private fun getDeltaFromOp(delta: Int, op: String): Int =
        if (op == "+") delta else -delta

    private fun Int.constraint(lower: Int, upper: Int): Int =
        min(max(this, lower), upper)

    private fun CommandContext.volumeByDelta(operator: String, deltaString: String) {
        if (operator !in setOf("+", "-")) {
            return replyCat {
                title = "Invalid usage"
                color = RED
                +volumeHelp
            }
        }
        val delta = parseVolumeValue(deltaString) ?: return replyCat {
            title = "Invalid delta value"
            color = RED
            +volumeHelp
            +"Delta must be a positive number in range [0; 100]"
        }
        val player = MusicHandler.getGuildAudioPlayer(guild)
        setVolume(player.player.volume + getDeltaFromOp(delta, operator))
    }

    private fun CommandContext.setVolume(value: Int) {
        MusicHandler.getGuildAudioPlayer(guild).player.volume = value.constraint(0, 100)
        replyCat {
            title = "Volume changed"
            color = GREEN
            +"New volume is $value"
        }
    }

    private fun CommandContext.musicDeny() {
        replyCat {
            color = RED
            title = "No! You can't do that."
            +"this incident will be reported!"
        }
    }

    private fun currentImg(guild: Guild): String = MusicHandler.currentTrack(guild)?.data?.getImage(cat.img) ?: ""

    private fun CommandContext.replyPlayable(playable: Playable, embed: Init<PaginatedEmbedCreator>) = reply {
        +formatPlayable(playable)
        thumbnail = playable.getImage(cat.img)
        embed()
    }

    fun CommandContext.handleLoad(res: MusicHandlerRet) {
        when (res) {
            is MHSuccess -> replyPlayable(res.playable) {
                color = GREEN
                title = "Success"
            }
            is MHAlreadyInQueue -> replyPlayable(res.playable) {
                color = YELLOW
                title = "This song is already in queue"
            }
            is MHNotFound -> replyPlayable(res.playable) {
                color = RED
                title = "No valid songs found... this really shouldn't happen..."
            }
            is MHError -> replyPlayable(res.playable) {
                color = RED
                title = "Error occurred"
                +"\n"
                +res.error.javaClass.simpleName
                +": "
                +res.error.localizedMessage
            }
            is MHUnknown -> replyCat {
                color = BLACK
                title = "Something went really really wrong..."
            }
            is MHFullyLoaded -> replyCat {
                color = GREEN
                title = "Success"
                thumbnail = (res.results.first { it is MHSuccess } as MHSuccess).playable.getImage()
                +"Loaded ${res.results.size} songs"
            }
            is MHAllLoaded -> replyCat {
                color = YELLOW
                title = "Loaded all possible songs"
                thumbnail = (res.results.first { it is MHSuccess } as MHSuccess).playable.getImage()
                +"Loaded ${res.results.size - 1} songs"
            }
            is MHPartiallyLoaded -> replyCat {
                color = YELLOW
                title = "Something went wrong somewhere"
                thumbnail = (res.results.first { it is MHSuccess } as MHSuccess).playable.getImage()
                +"Loaded ${res.loaded} songs\n"
                +"Unable to load ${res.results.size - res.loaded} songs"
            }
            is MHNotLoaded -> replyCat {
                color = RED
                title = "Everything went wrong everywhere"
                +"Unable to load anything..."
            }
            is MHNoMoreTracks -> replyCat {
                color = YELLOW
                title = "Loaded all possible tracks"
            }
        }
    }

    private fun formatPlayable(playable: Playable): String =
        when (playable) {
            is Playable.Music -> formatData(playable.data)
            is Playable.Url -> "[${playable.title}](${playable.url})"
        }

    private fun formatData(data: MusicData): String {
        var ret = data.title
        var cur = data
        while (cur.parent!!.type != CONTEXT) {
            val title = cur.parent!!.title
            if (title.isNotEmpty()) {
                ret = "$title/$ret"
            }
            cur = cur.parent!!
        }
        return ret
    }

}
