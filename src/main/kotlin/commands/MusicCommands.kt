package commands

import cmd.*
import dsl.PaginatedEmbedCreator
import dsl.embed
import music.*
import users.assets
import java.lang.IllegalArgumentException
import music.MusicHandler.imgUrl
import net.dv8tion.jda.core.entities.MessageReaction
import uadamusic.CONTEXT
import uadamusic.MusicData
import java.awt.Color
import java.awt.Color.*
import music.MusicHandler.data
import net.dv8tion.jda.core.entities.Guild
import sources.MusicSource
import users.admin_or_interface
import java.text.SimpleDateFormat
import java.util.*

object MusicCommands : ICommandList {

    override val cat = CommandCategory("Music", Color(0xAFEBF3), "http://52.48.142.75/images/an.png")

    const val numbermoji = '\u20e3'
    private val playlistTimeFormat = SimpleDateFormat("HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("UTC") }
    override fun init(): Init<CommandListBuilder> = {

        command("play") {
            allowed to assets
            help = "Play"
            val all by parser.flag("all", shortname = 'a')
            val repeat by parser.flag("repeat", shortname = 'r')
            val first by parser.flag("first", shortname = 'f')
            val next by parser.flag("next", shortname = 'n')
            val largs by parser.leftoverDelegate()
            action {
                val (count, namel) = extractCount(largs)
                val name = namel.joinToString(" ")
                if(first.present && next.present) {
                    replyCat {
                        color = RED
                        title = "Only one of 'first' or 'next' can be specified"
                    }
                }
                val margs = MusicHandler.MusicArgs(
                    count,
                    !(repeat.present || (all.present && count > 1)),
                    all.present,
                    first.present,
                    next.present
                )
                val res = if (name.isEmpty()) {
                    MusicHandler.load(MusicHandler.context, guild, margs)
                } else {
                    val data = MusicHandler.context.search(name) ?: emptyList()
                    when (data.size) {
                        0 -> {
                            replyCat {
                                color = RED
                                title = "Found a whole lot of nothing"
                                +name
                            }
                            null
                        }
                        1 -> {
                            MusicHandler.load(data.first(), guild, margs)
                        }
                        in 2..10 -> {
                            reply(embed {
                                color = YELLOW
                                title = "Select variant"
                                data.forEachIndexed { i, e ->
                                    +"${i + 1}$numbermoji - ${e.type.name.capitalize()} - ${formatData(e)}\n\n"
                                }
                            }, success = { msg ->
                                Reactions.register(msg.id) {
                                    if (it.user != UADAB.bot.selfUser) {
                                        val re = it.reactionEmote.name
                                        if (re.length > 1 && re[1] == numbermoji) {
                                            val num = re[0]
                                            if (num in '1'..('0' + data.size)) {
                                                val r =
                                                    MusicHandler.load(data[num.toString().toInt() - 1], guild, margs)
                                                handleLoad(r)
                                                //Use getMessageById to update message, because msg is cached version
                                                msg.channel.getMessageById(msg.id).complete().reactions.filter(
                                                    MessageReaction::isSelf
                                                ).forEach { rec ->
                                                    rec.removeReaction().queue()
                                                }

                                            }
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                }
                                for (j in 1..data.size) {
                                    msg.addReaction("$j$numbermoji").queue()
                                }
                            })
                            null
                        }
                        else -> {
                            replyCat {
                                color = RED
                                title = "To many results, try narrowing it down"
                                +name
                            }
                            null
                        }
                    } ?: return@action
                }
                handleLoad(res)
            }
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
                            thumbnail = cur.data.imgUrl ?: cat.img
                        }
                        +"1: ${formatData(cur.data)} ${(cur.position * 100) / cur.duration}%\n"
                        pl.forEachIndexed { i, e ->
                            +"${i + 2}: ${formatData(e.data)}"
                            if(e.position != 0L) {
                                +" ${(e.position * 100) / e.duration}%"
                            }
                            +"\n"
                            totalTime += e.duration
                        }
                        +"\nTotal time: ${playlistTimeFormat.format(Date(totalTime))}"
                    }
                }
            }
        }
        command("skip") {
            allowed to assets
            help = "Skip specified song, use without arg to skip current"
            val skipRange by parser.range("range")
            val skipPlain by parser.leftoverDelegate()
            fun CommandContext.skip(id: Int) {
                val skipped = MusicHandler.skip(id, guild)
                replyData(skipped.data) {
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
                            replyData(skipped.data) {
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
                                thumbnail = skipped.first().data.imgUrl ?: cat.img
                            }
                            skipped.forEach {
                                +formatData(it.data)
                                +"\n"
                            }
                        }
                    }
                }
            }
        }
        command("reload") {
            allowed to admin_or_interface
            help = "Reload music context"
            action {
                MusicSource.reload()
                replyCat {
                    color = GREEN
                    title = "Reloaded"
                }
            }
        }
    }

    fun currentImg(guild: Guild): String = MusicHandler.currentTrack(guild)?.data?.imgUrl ?: cat.img!!

    fun CommandContext.replyData(data: MusicData, embed: Init<PaginatedEmbedCreator>) = reply {
        thumbnail = data.imgUrl ?: cat.img
        +formatData(data)
        embed()
    }

    fun CommandContext.handleLoad(res: MusicHandlerRet) {
        when (res) {
            is MHSuccess -> replyData(res.data) {
                color = GREEN
                title = "Success"
            }
            is MHAlreadyInQueue -> replyData(res.data) {
                color = YELLOW
            }
            is MHNotFound -> replyData(res.data) {
                color = RED
                title = "No valid songs found... this really shouldn't happen..."
            }
            is MHError -> replyData(res.data) {
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
                thumbnail = res.results.first().data?.imgUrl
                +"Loaded ${res.results.size} songs"
            }
            is MHAllLoaded -> replyCat {
                color = YELLOW
                title = "Loaded all possible songs"
                thumbnail = res.results.first().data?.imgUrl
                +"Loaded ${res.results.size - 1} songs"
            }
            is MHPartiallyLoaded -> replyCat {
                color = YELLOW
                title = "Something went wrong somewhere"
                thumbnail = res.results.first { it is MHSuccess }.data?.imgUrl
                +"Loaded ${res.loaded} songs\n"
                +"Unable to load ${res.results.size - res.loaded} songs"
            }
            is MHNotLoaded -> replyCat {
                color = RED
                title = "Everything went wrong everywhere"
                +"Unable to load anything..."
            }
        }
    }

    fun extractCount(a: List<String>): Pair<Int, List<String>> {
        val countPos = a.indexOf("*")
        if (a.lastIndexOf("*") != countPos) {
            throw IllegalArgumentException("'*' cannot be specified multiple times")
        }
        return if (countPos < 0) {
            1 to a
        } else {
            if (countPos < a.lastIndex) {
                val count =
                    a[countPos + 1].toIntOrNull() ?: throw IllegalArgumentException("'*' must be followed by a number")
                count to a.filterIndexed { i, _ -> i !in countPos..countPos + 1 }
            } else {
                throw IllegalArgumentException("'*' must be followed by a number")
            }
        }
    }

    fun formatData(data: MusicData): String {
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
