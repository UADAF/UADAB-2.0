import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent

object EventListener {

    @SubscribeEvent
    fun ReadyEvent.ready() {
        jda.presence.status = OnlineStatus.ONLINE
    }

    @SubscribeEvent
    fun MessageReceivedEvent.msg() {

    }

}