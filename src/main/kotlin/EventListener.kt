import cmd.CommandClient
import com.kizitonwose.time.minutes
import dsl.embed
import dsl.sendPaginatedEmbed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.MessageUpdateEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import org.jetbrains.exposed.sql.transactions.transaction
import sources.*
import users.UADABUser
import utils.BashUtils
import java.awt.Color

object EventListener {

    @SubscribeEvent
    fun ReadyEvent.ready() {
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
        GlobalScope.launch {
            jda.presence.status = OnlineStatus.ONLINE
            timer.scheduleAtFixedRate(period = 5.minutes.inMilliseconds.longValue) {
                runBlocking {
                    jda.presence.game = GameListSource.get().random()
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
        bashCheck(message)
    }

    @SubscribeEvent
    fun MessageUpdateEvent.edit() {
        onMsg(message)
        bashCheck(message)
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

    private fun bashCheck(message: Message) {
        GlobalScope.launch {
            val matches = """(https?://)?(bash\.im/quote/)(\d+)""".toRegex().find(message.contentRaw) ?: return@launch
            if (matches.groups.count() != 4) return@launch

            val quote = BashUtils.fetchQuote(message.contentRaw) ?: return@launch
            val channel = message.textChannel
            message.delete().queue()

            channel.sendPaginatedEmbed {
                pattern {
                    title = "Цитата ${quote.id}"
                    url = message.contentRaw
                    thumbnail = "https://bash.im/favicon-180x180.png"
                    color = Color.white
                }
                +quote.content
            }
        }
    }

}