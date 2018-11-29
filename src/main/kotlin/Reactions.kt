import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent

typealias ReactionHandler = (MessageReactionAddEvent) -> Boolean
object Reactions {


    private val handlers = mutableMapOf<String, MutableList<ReactionHandler>>()


    fun register(id: String, handler: ReactionHandler) = handlers.getOrPut(id, ::mutableListOf).add(handler)

    fun unregister(id: String, handler: ReactionHandler) = handlers[id]?.remove(handler)


    @SubscribeEvent
    fun MessageReactionAddEvent.handle() {
        //Run all handlers, and remove ones that returns 'true'
        handlers[messageId]?.removeAll { handler -> handler(this) }
    }

}