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

object MusicCommands : ICommandList {

    override val cat = CommandCategory("Music", Color(0xAFEBF3), "http://52.48.142.75/images/an.png")

    const val numbermoji = '\u20e3'

    override fun init(): Init<CommandListBuilder> = {

        command("play") {
            allowed to assets
            help = "Play"
            val all by parser.flag("all", shortname = 'a')
            val repeat by parser.flag("repeat", shortname = 'r')
            val first by parser.flag("first", shortname = 'f')
            val largs by parser.leftoverDelegate()
            action {
                val (count, namel) = extractCount(largs)
                val name = namel.joinToString(" ")
                val margs = MusicHandler.MusicArgs(count, !repeat.present, all.present, first.present)
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
            }
        }
        command("resume") {
            allowed to assets
            help = "Resume music"
            action {
                MusicHandler.resume(guild)
            }
        }
        command("clear") {
            allowed to assets
            help = "Clears playlist"
            action {
                MusicHandler.reset(guild)
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
                    reply {
                        pattern {
                            color = GREEN
                            title = "Playlist"
                            thumbnail = cur.data.imgUrl ?: cat.img
                        }
                        +"1: ${formatData(cur.data)} ${(cur.position * 100) / cur.duration}%\n"
                        pl.forEachIndexed { i, e ->
                            +"${i + 2}: ${formatData(e.data)}"
                        }
                    }
                }
            }
        }
    }

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
                +"Loaded ${res.loaded} songs"
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
