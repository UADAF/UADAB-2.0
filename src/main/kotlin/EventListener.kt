import cmd.CommandClient
import dsl.embed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.MessageUpdateEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import org.jetbrains.exposed.sql.transactions.transaction
import sources.ExternalSourceRegistry
import users.UADABUser
import java.awt.Color

object EventListener {

    @SubscribeEvent
    fun ReadyEvent.ready() {
        jda.presence.setPresence(OnlineStatus.ONLINE, Game.watching("за пользователями"))
        GlobalScope.launch {
            transaction {
                jda.users.forEach { UADABUser.fromDiscord(it, openTransaction = false) }
            }
        }
        GlobalScope.launch {
            UADAB.log.debug("Loading...")
            ExternalSourceRegistry.sources.forEach {
                launch {
                    it.startLoading()
                }
            }
        }
    }

    fun GuildMemberJoinEvent.onJoin() {
        UADABUser.fromDiscord(user).let { u ->
            guild.defaultChannel?.sendMessage(embed {
                title = "Info about ${user.name}"
                thumbnail = user.effectiveAvatarUrl
                color = u.classification.cornerColor
                inline field "Classification" to u.classification.name
                inline field "SSN" to u.ssn.getSSNString(redacted = false)
                append field "Discord ID" to user.id
                inline field "Online status" to member.onlineStatus.key.replace("dnd", "do not disturb").capitalize()
            })
        }
    }

    @SubscribeEvent
    fun MessageReceivedEvent.msg() {
        onMsg(message)
    }

    @SubscribeEvent
    fun MessageUpdateEvent.edit() {
        onMsg(message)
    }

    private fun onMsg(message: Message) {
        GlobalScope.launch {
            val (res, v) = UADAB.commandClient.handle(message)
            when(res) {
                CommandClient.ExecutionResult.ERROR -> {
                    message.channel.sendMessage(embed {
                        color = Color.RED
                        title = "Something went wrong"
                        +v
                    }).queue()
                }
                else -> {}
            }
        }
    }

}