import argparser.*
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent

object EventListener {

    @SubscribeEvent
    fun ReadyEvent.ready() {
        jda.presence.status = OnlineStatus.ONLINE
    }

    val parser = ArgParser()
    val asd by parser.flag("asd", shortname = 'a')
    val dsa by parser.flag("dsa", shortname = 'd')
    val sda by parser.value("sda")
    val leftover by parser.leftoverDelegate()

    @SubscribeEvent
    fun MessageReceivedEvent.msg() {
        if (!author.isBot) {
            val tokenized = tokenize(message.contentRaw)
            channel.sendMessage(tokenized.toString()).queue()
            parser.with(tokenized) {
                channel.sendMessage(
                    """Asd: ${if (asd.present) "Present" else "Absent"}
                      |Dsa: ${if (dsa.present) "Present" else "Absent"}
                      |Sda: ${sda.name}=${sda.value}
                      |Leftover: $leftover""".trimMargin()
                ).queue()
            }
        }
    }

}