import dsl.embed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.message.GenericMessageEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.MessageUpdateEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import org.jetbrains.exposed.sql.transactions.transaction
import users.UADABUser

object EventListener {

    @SubscribeEvent
    fun ReadyEvent.ready() {
        jda.presence.setPresence(OnlineStatus.ONLINE, Game.watching("за пользователями"))
        transaction {
            jda.users.forEach { UADABUser.fromDiscord(it, openTransaction = false) }
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

    private fun GenericMessageEvent.onMsg(message: Message) {
        GlobalScope.launch {
            UADAB.commandClient.handle(this@onMsg, message)
        }
    }

}