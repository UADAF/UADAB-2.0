import argparser.*
import dsl.sendPaginatedEmbed
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import java.awt.Color

object EventListener {

    @SubscribeEvent
    fun ReadyEvent.ready() {
        jda.presence.status = OnlineStatus.ONLINE
        val nas = jda.getTextChannelById("404373837287784449")
        sendPaginatedEmbed(nas) {
            pattern {
                thumbnail = "http://52.48.142.75/images/gear.png"
                title = "Root voice"
                color = Color.GREEN
            }
            +"If you can hear this you are alone"
            breakPage()
            +"The only thing left of me is the sound of my voice"
            breakPage()
            +"So let me tell you who we were."
            breakPage()
            +"Let me tell you who you are."
            breakPage()
            +"Someone once asked me if I had learned anything from it all."
            breakPage()
            +"So let me tell you what I learned."
            breakPage()
            +"I learned: everyone dies alone."
        }
    }

    @SubscribeEvent
    fun MessageReceivedEvent.msg() {

    }

}