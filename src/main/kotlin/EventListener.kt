import argparser.tokenize
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.MessageUpdateEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent

object EventListener {

    @SubscribeEvent
    fun ReadyEvent.ready() {
        jda.presence.setPresence(OnlineStatus.ONLINE, Game.watching("за пользователями"))
    }

    @SubscribeEvent
    fun MessageReceivedEvent.msg() {
        if(message.contentRaw.startsWith("sudo ")) {
            val tokenized = tokenize(message.contentRaw)
            if(tokenized.size > 1) {
                val cmd = tokenized[1]
                val args = tokenized.subList(2, tokenized.size)

            }
        }
    }

    @SubscribeEvent
    fun MessageUpdateEvent.edit() {

    }

}