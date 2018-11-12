import argparser.ArgParser
import argparser.argparser
import argparser.spec.FlagArgResult
import argparser.spec.FlagArgSpec
import argparser.spec.ValueArgResult
import argparser.spec.ValueArgSpec
import argparser.tokenize
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent

object EventListener {

    @SubscribeEvent
    fun ReadyEvent.ready() {
        jda.presence.status = OnlineStatus.ONLINE
    }

    val parser = argparser {
        flag("asd", shortname = 'a')
        flag("dsa", shortname = 'd')
        value("sda")
    }
    val asd by parser.delegate<FlagArgResult>("asd")
    val dsa by parser.delegate<FlagArgResult>("dsa")
    val sda by parser.delegate<ValueArgResult>("sda")

    @SubscribeEvent
    fun MessageReceivedEvent.msg() {
        if (!author.isBot) {
            val tokenized = tokenize(message.contentRaw)
            channel.sendMessage(tokenized.toString()).queue()
            parser.with(tokenized) {
                channel.sendMessage(
                    """Asd: ${if (asd.present) "Present" else "Absent"}
                      |Dsa: ${if (dsa.present) "Present" else "Absent"}
                      |Sda: ${sda.name}=${sda.value}""".trimMargin()
                ).queue()
            }
        }
    }

}