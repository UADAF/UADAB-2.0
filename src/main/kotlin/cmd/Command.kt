package cmd

import users.Classification

typealias CommandAction = suspend CommandContext.() -> Unit
typealias CanPerformCheck = suspend CommandContext.() -> Boolean?
open class Command(val name: String, val aliases: List<String>,
                   val allowedClasses: Set<Classification>, val canPerform: CanPerformCheck?,
                   val onDenied: CommandAction? = null, val action: CommandAction) {

    open suspend fun perform(context: CommandContext) {
        context.action()
    }

    open suspend fun deny(context: CommandContext) {
        onDenied?.invoke(context)
    }

    open suspend fun canPerform(context: CommandContext): Boolean {
        return canPerform?.invoke(context) ?: context.author.classification in allowedClasses
    }



}