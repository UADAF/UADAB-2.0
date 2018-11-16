package cmd
typealias CommandAction = CommandContext.() -> Unit
open class Command(val name: String, val action: CommandAction, val onDenied: CommandAction? = null) {



    open fun perform(context: CommandContext) {
        context.action()
    }

    open fun deny(context: CommandContext) {
        onDenied?.invoke(context)
    }



}