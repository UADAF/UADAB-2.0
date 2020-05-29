package commands

import cmd.CommandCategory
import cmd.CommandListBuilder
import cmd.ICommandList
import dsl.BaseEmbedCreater
import dsl.Init
import getters.Getters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import users.Classification
import users.UADABUser
import users.assets
import utils.getters.Wrapper
import java.awt.Color
import java.awt.Color.GREEN
import java.awt.Color.RED
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object UserCommands : ICommandList {

    override val cat = CommandCategory("System", Color(0x5E5E5E), "http://52.48.142.75/images/gear.png")

    override fun init(): Init<CommandListBuilder> = {
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
                    users
                        .map { it to it.getBoxedImageAsync().await().toByteArray() }
                        .map { (user, img) -> monitorInfo(user, img) }
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
                        } ?: userData.getCompleted().forEach(::page)
                    }
                }
            }
        }
        command("reclass") {
            help = "Change classification of user"
            val nameArg by parser.plain("user")
            val leftover by parser.leftoverDelegate()
            allowed to assets
            action {
                val name = nameArg.value ?: return@action replyCat {
                    title = "Invalid args"
                    color = RED
                    +"No user specified"
                }
                val cls =
                    leftover.joinToString(" ").let(Classification.Companion::getOrNull) ?: return@action replyCat {
                        title = "Invalid args"
                        color = RED
                        +"Classification is not classification"
                    }

                val wuser = Getters.getUser(nameArg.value!!)
                when (wuser.state) {
                    Wrapper.WrapperState.NONE -> replyCat {
                        title = "No users found"
                        color = RED
                        +"$name matched no used"
                    }
                    Wrapper.WrapperState.MULTI -> replyCat {
                        title = "Many users found"
                        color = RED
                        +"$name matched too many users used"
                    }
                    Wrapper.WrapperState.SINGLE -> {
                        val user = wuser.getSingle()
                        val acls = author.classification.permissionLevel
                        val ucls = user.classification.permissionLevel
                        val tcls = cls.permissionLevel
                        if (acls < tcls || (ucls > tcls && acls <= ucls)) {
                            return@action replyCat {
                                title = "Insufficient permission"
                                color = RED
                                if (author.classification == Classification.ADMIN) {
                                    +"Admin is not admin"
                                }
                            }
                        }
                        user.classification = cls
                        user.save()
                        user.getBoxedImageAsync().run {
                            invokeOnCompletion {
                                replyCat {
                                    title = "Success"
                                    color = GREEN
                                    +"${user.name} is now ${cls.name}"
                                    if (it == null) thumbnail = "boxed_avatar.png" attach getCompleted().toByteArray()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun BufferedImage.toByteArray(): ByteArray =
        ByteArrayOutputStream().apply {
            ImageIO.write(this@toByteArray, "png", this@apply)
        }.toByteArray()

    /**
     * Get a function that build a single page for {user} with {image}
     */
    private fun monitorInfo(user: UADABUser, imageData: ByteArray): Init<BaseEmbedCreater> = {
        title = "Info about ${user.name}"
        thumbnail = "boxed_avatar.png" attach imageData
        inline field "Classification" to user.classification.name
        inline field "SSN" to user.ssn.redactedSSNString
        append field "Name" to user.name
        inline field "Location" to (user.discord.mutualGuilds.mapNotNull { guild ->
            guild.getMember(user.discord)?.voiceState?.channel?.let { voice ->
                guild to voice
            }
        }.firstOrNull()?.let { (guild, voice) ->
            "${guild.name}/${voice.name}"
        } ?: "[UNKNOWN]")
        inline field "Aliases" to "${user.discord.name}\n${
        user.discord.mutualGuilds.mapNotNull { guild ->
            guild.getMember(user.discord).nickname ?: null
        }.toSet().joinToString("\n")
        }"
    }

}