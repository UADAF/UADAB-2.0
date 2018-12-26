package commands

import cmd.CommandCategory
import cmd.CommandListBuilder
import cmd.ICommandList
import cmd.Init
import net.dv8tion.jda.core.Permission
import users.assets
import java.awt.Color
import java.nio.file.Files
import java.nio.file.Paths

object SystemCommands : ICommandList {

    override val cat = CommandCategory("System", Color(0x5E5E5E), "http://52.48.142.75/images/gear.png")

    override fun init(): Init<CommandListBuilder> = {
        command("asd") {
            val d = Paths.get("")
            if (!Files.exists(d)) {
                Files.createDirectories(d.parent)
                Files.createFile(d)
            }
            allowed to assets
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
    }


}