package commands

import argparser.spec.RangeArgResult
import cmd.*
import commands.MiscCommands.replyCat
import dsl.PaginatedEmbedCreator
import dsl.paginatedEmbed
import getters.Getters
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.GuildVoiceState
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.VoiceState
import quoter.DisplayType
import sources.HttpCodeSource
import sources.QuoterSource
import sources.get
import users.UADABUser
import utils.extractCount
import utils.getters.Wrapper
import java.awt.Color
import utils.getters.Wrapper.WrapperState.*
import java.awt.Color.*
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

typealias MonitorBuilder = PaginatedEmbedCreator.() -> Unit
object MiscCommands : ICommandList {

    override val cat: CommandCategory =
        CommandCategory("Misc", Color(0x95a3a6), "http://52.48.142.75/images/math_compass.png")

    override fun init(): Init<CommandListBuilder> = {
        command("http") {
            help = "Get info about http codes"
            args = "(i%code%)+? -(-n)o-description"
            val noDescription by parser.flag("no-description", shortname = 'n')
            val arguments by parser.leftoverDelegate()

            action {
                val dataSet = HttpCodeSource.get()

                val codes: ArrayList<HttpCodeSource.HTTPStatusCode> = ArrayList()
                val invalids: ArrayList<String> = ArrayList()

                arguments.forEach {
                    val intValue = it.toIntOrNull()

                    if (intValue != null && dataSet.containsKey(intValue)) {
                        codes.add(dataSet.getValue(intValue))
                    } else {
                        invalids.add(it)
                    }
                }

                if (codes.isNotEmpty()) replyCat {
                    codes.forEach { code ->
                        if (noDescription.present) {
                            append field "HTTP ${code.code}" to code.phrase
                        } else {
                            page {
                                title = "HTTP ${code.code} - ${code.phrase}"
                                +code.description
                            }
                        }
                    }
                }

                if (invalids.isNotEmpty()) replyCat {
                    title = "These codes I could not recognize"
                    +invalids.joinToString(separator = ", ")
                }

            }
            onDenied {
                replyCat {
                    color = Color.RED
                    title = "Sorry"
                    +"You are not allowed to control me. Get out"
                }
            }
        }
        command("quote") {
            help = "Add or get quote"
            val quoteRange by parser.range("quoteRange")
            val all by parser.flag("all", shortname = 'a')
            val repo by parser.value("repo")
            val arguments by parser.leftoverDelegate()
            action {
                val (count, leftover) = extractCount(arguments)
                if (count != 1 && (leftover.isNotEmpty() || quoteRange.isNotEmpty || all.present)) {
                    replyCat {
                        title = "Invalid args"
                        color = RED
                        +"Count can't be specified with other args"
                    }
                    return@action
                }
                if (all.present && quoteRange.isNotEmpty) {
                    replyCat {
                        title = "Invalid args"
                        color = RED
                        +"Can't have both range and --all"
                    }
                    return@action
                }
                val q = QuoterSource.get()
                val r = repo.value ?: q.defaultRepo
                if (leftover.size > 1) {
                    if (leftover[0] == "add") {
                        if (all.present || quoteRange.isNotEmpty) {
                            replyCat {
                                title = "Invalid args"
                                color = RED
                                +"--all and range are not allowed when adding, enclose them in ' or \" if they are part of the quote"
                            }
                            return@action
                        }
                        if (leftover.size > 2) {
                            val qauthor = leftover[1]
                            val quote = leftover.subList(2, leftover.size).joinToString(" ")

                            var attachments: List<String>? = null
                            if (message.attachments.isNotEmpty()) {
                                val attch = message.attachments[0]
                                if (attch.isImage) {
                                    attachments = listOf(attch.url)
                                }
                            }
                            val resp = q.add(
                                author.discord.name,
                                qauthor,
                                quote,
                                if (quote.count { it == '\n' } > 1) DisplayType.DIALOG else DisplayType.TEXT,
                                attachments,
                                r)
                            if (resp.response.status != HttpStatusCode.OK) {
                                replyCat {
                                    title = "Something went wrong"
                                    color = RED
                                    +resp.response.status.toString()
                                }
                            } else {
                                replyCat {
                                    title = "Added, $qauthor:"
                                    color = GREEN
                                    +quote
                                }
                            }
                        } else {
                            replyCat {
                                title = "No quote specified"
                                color = RED
                                +"First word is author"
                            }
                        }
                    } else {
                        replyCat {
                            title = "Invalid args"
                            color = RED
                            +"Unknown args: ${leftover.joinToString(" ")}"
                        }
                    }
                } else {
                    val quotes = if (leftover.isNotEmpty()) {
                        val id = leftover[0].toIntOrNull()
                        if (id == null) {
                            replyCat {
                                title = "Invalid id"
                                color = RED
                                +"Unknown id $id"
                            }
                            return@action
                        }
                        listOf(q.byId(id, r))
                    } else {
                        when {
                            all.present -> q.all(r)
                            quoteRange.isNotEmpty -> q.byRange(quoteRange.from ?: 1, quoteRange.to ?: q.total(r), r)
                            else -> q.random(count, r)
                        }
                    }.filterNotNull()
                    if (quotes.isEmpty()) {
                        replyCat {
                            title = "No quotes found"
                            color = RED
                        }
                    } else {
                        replyCat {
                            quotes.forEachIndexed { i, quote ->
                                with(quote) {
                                    if (attachments.isNotEmpty() || content.length > 1024) {
                                        if (builder.length() != 0 || builder.fields.size != 0) {
                                            breakPage()
                                        }

                                    }
                                    if (content.length > 1024) {
                                        +content
                                    } else {
                                        append field "#$id ${authors.joinToString(", ")}:" to content
                                    }
                                    if (attachments.isNotEmpty()) {
                                        try {
                                            attachments.forEachIndexed { j, a ->
                                                if (a.matches("^https?://.+$".toRegex())) {
                                                    image = a
                                                    if (i != quotes.lastIndex || j != attachments.lastIndex) {
                                                        breakPage()
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            println(attachments)
                                            throw e
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        command("monitor") {
            help = "Can you hear me?"
            aliases { +"mon" }
            val nameArg by parser.plain("user")
            val leftover by parser.leftoverDelegate()
            action {
                val name = nameArg.value ?: return@action replyCat {
                    title = "Invalid args"
                    color = RED
                    +"No user specified"
                }
                val users = Getters.getUser(name).toList()
                if (users.isEmpty()) {
                    return@action replyCat {
                        title = "No users found"
                        color = RED
                        +"$name matched no used"
                    }
                }
                // Async fetch all users with images
                val userData = GlobalScope.async(Dispatchers.IO) {
                    users.map { userImage(it) }.map { monitorInfo(it.first, it.second) }
                }
                // on complete reply message
                // if error - send error message
                // if success, for each user create and fill new page
                userData.invokeOnCompletion {
                    replyCat {
                        it?.let {
                            title = "Unable to monitor"
                            +"Something went wrong with boxing:\n"
                            +it.toString()
                        } ?: userData.getCompleted().forEach {
                            page { this@replyCat.it() }
                        }
                    }
                }
            }
        }
    }

    // Пиздани, если это так не работает (в плане асинхронности)
    /**
     * Blocking fetch image for user and return pair of user name image
     */
    private suspend fun userImage(user: UADABUser): Pair<UADABUser, ByteArray> =
        user to ByteArrayOutputStream().apply {
            ImageIO.write(user.getBoxedImageAsync().await(), "png", this)
        }.toByteArray()

    /**
     * Get a function that build a single page for {user} with {image}
     */
    private fun monitorInfo(user: UADABUser, imageData: ByteArray): MonitorBuilder = {
        title = "Info about ${user.name}"
        thumbnail = "attachment://boxed_avatar.png"
        inline field "Classification" to user.classification.name
        inline field "SSN" to user.ssn.redactedSSNString
        append field "Name" to user.name
        inline field "Location" to (user.discord.mutualGuilds.mapNotNull { guild ->
            guild.getMember(user.discord)?.voiceState?.channel?.let { voice ->
                guild to voice
            }
        }.firstOrNull()?.let { location ->
            "${location.first.name}/${location.second.name}"
        } ?: "[UNKNOWN]")
        "boxed_avatar.png" attach imageData
        inline field "Aliases" to "${user.discord.name}\n${
            user.discord.mutualGuilds.mapNotNull { guild ->
                guild.getMember(user.discord).nickname ?: null
            }.toSet().joinToString("\n")
        }"
    }

    val RangeArgResult.isEmpty
        get() = from == null && to == null

    val RangeArgResult.isNotEmpty
        get() = !isEmpty

}