package commands

import UADAB
import cmd.CommandCategory
import cmd.CommandListBuilder
import cmd.ICommandList
import cmd.Init
import dsl.embed
import net.dv8tion.jda.core.Permission
import users.Classification
import users.Classification.Companion.ADMIN
import users.Classification.Companion.ANALOG_INTERFACE
import users.admin_or_interface
import users.assets
import users.everyone
import java.awt.Color
import java.awt.Color.GREEN
import kotlin.system.exitProcess

object SystemCommands : ICommandList {

    override val cat = CommandCategory("System", Color(0x5E5E5E), "http://52.48.142.75/images/gear.png")

    override fun init(): Init<CommandListBuilder> = {
        command("asd") {
            allowed to assets
            help = "Bot joins your channel"
            action {
                val u = author.discord
                val ch = guild.getMember(u).voiceState.channel ?: return@action replyCat {
                    color = Color.RED
                    title = "Unable to join"
                    +"You must be in voice channel"
                }

                if (ch.userLimit >= ch.members.count()) return@action replyCat {
                    color = Color.RED
                    title = "Users limit exceeded"
                    +"Kick someone, please"
                }

                if (!guild.selfMember.hasPermission(Permission.VOICE_CONNECT)) return@action replyCat {
                    color = Color.RED
                    title = "I-I can't. It's... gone..."
                    +"Conditions satisfied. Engaging Purge Precept."
                }

                guild.audioManager.openAudioConnection(ch)
                replyCat {
                    color = Color.GREEN
                    title = "Success"
                    +"Joined"
                }
            }
            onDenied {
                replyCat {
                    color = Color.RED
                    title = "Sorry"
                    +"You are not allowed to control me"
                }
            }
        }
        command("dsa") {
            allowed to assets
            help = "Bot leaves your channel"
            action {
                if (guild.selfMember.voiceState.channel == null) return@action replyCat {
                    color = Color.RED
                    title = "Unable to leave"
                    +"I am not in voice channel"
                }

                guild.audioManager.closeAudioConnection()
                replyCat {
                    color = Color.GREEN
                    title = "Success"
                    +"Left"
                }
            }
            onDenied {
                replyCat {
                    color = Color.RED
                    title = "Sorry"
                    +"You are not allowed to control me"
                }
            }
        }
        command("help") {
            allowed to everyone
            help = "Get some help about commands"
            args = "(%command%|args)+"
            action {
                if (args.isNotEmpty()) {
                    reply {
                        val cmdsNotFound = mutableListOf<String>()
                        args.forEach { name ->
                            if (name == "args") {
                                page {
                                    title = "Info about arguments notation"
                                    color = cat.color
                                    thumbnail = cat.img
                                    append field "%name%" to "Text argument. Like %command% in here corresponds to name of some command"
                                    append field "text" to "Constant text. Like 'args' in here means that you have to type 'args' to get this message"
                                    append field "i%name%" to "Number argument. Just some number, like 1, 12, or 6741"
                                    append field "(%arg%)+" to "Arguments with + on the end can be repeated infinitely, use space as separator"
                                    append field "(%arg%)?" to "Arguments with ? are optional, you can just skip them"
                                    append field "--some-flag" to "Flag argument. It's like constant text but with few features"
                                    append field "-(-s)ome-flag" to "Flag with shortname. You can use '-s' instead of '--some-flag'"
                                    append field "-(-s)ome-flag -(-o)ther-flag" to "Shortnames can be combined, so you can write both flags as '-so'"
                                    append field "r%from:to%" to "Range argument. Two numbers separated by :. You can skip one or both numbers. Usually that would mean 'to start/end'"
                                    append field "--some-arg=%some-value%" to "Named args. It's like constant text, then '=' and then text arg"
                                    append field "(%arg1%|%arg2%)" to "OR-type args. You can use one of them. If this is repeated, you can use both, just separate by space"
                                }
                            } else {
                                val cmd = UADAB.commandClient[name]
                                if(cmd == null) {
                                    cmdsNotFound.add(name)
                                } else {
                                    page {
                                        title = name
                                        color = cmd.category.color
                                        thumbnail = cmd.category.img
                                        append field "${UADAB.commandClient.prefix} $name ${cmd.args}" to cmd.help
                                    }
                                }
                            }
                        }
                        if(cmdsNotFound.isNotEmpty()) {
                            page {
                                title = "I'm sorry, i couldn't find those commands"
                                color = cat.color
                                thumbnail = cat.img
                                +cmdsNotFound.joinToString(", ")
                            }
                        }
                    }
                } else {
                    var curCat: CommandCategory? = null
                    reply {
                        UADAB.commandClient.commands.forEach { name, cmd ->
                            if (!cmd.hidden) {
                                if (cmd.category != curCat) {
                                    if (curCat != null) {
                                        breakPage()
                                    }
                                    curCat = cmd.category
                                    color = cmd.category.color
                                    title = cmd.category.name
                                    thumbnail = cmd.category.img
                                }
                                append field "${UADAB.commandClient.prefix} $name ${cmd.args}" to cmd.help
                            }
                        }
                    }
                }
            }
        }
        command("404") {
            allowed to everyone
            hidden = true
            action { reply("Фыфырифтофыфыри!") }
        }
        command("shutdown") {
            allowed to admin_or_interface
            action {
                reply(embed {
                    title = "Shutting down..."
                    color = GREEN
                    +"Goodbye"
                    val cls = author.classification
                    when(cls) {
                        ADMIN -> +", Admin"
                        ANALOG_INTERFACE -> +", Interface"
                        else -> +"... how did you just do it???"
                    }
                }, success = {
                    exitProcess(0)
                })
            }
        }
    }


}