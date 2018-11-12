import argparser.ArgParser
import argparser.spec.FlagArgResult
import argparser.spec.FlagArgSpec
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
        if(!author.isBot) {
            val tokenized = ArgParser.tokenize(message.contentRaw)
            channel.sendMessage(tokenized.toString()).queue()
            var res: FlagArgResult? = null
            val spec = FlagArgSpec("asd", shortname = 'a')
            for (it in tokenized) {
                res = spec.parse(it)
                if (res != null) {
                    channel.sendMessage("Present").queue()
                    break
                }
            }
            if (res == null) {
                channel.sendMessage("Not present").queue()
            }
        }
    }

}