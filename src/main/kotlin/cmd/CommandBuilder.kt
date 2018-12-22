@file:Suppress("REDUNDANT_NULLABLE")

package cmd

import users.Classification
import users.NORMAL


typealias Init<T> = T.() -> Unit

@DslMarker
annotation class CommandBuilderDsl

@CommandBuilderDsl
class CommandBuilder {

    var name: String = ""
    private var _action: CommandAction = {}
    private var _onDenied: CommandAction? = null
    private var _canPerformCheck: CanPerformCheck? = null
    var allowedClasses: Set<Classification> = NORMAL

    val allowed by lazy { AllowedToSetter(this) }


    fun action(a: CommandAction) {
        _action = a
    }

    fun onDenied(a: CommandAction) {
        _onDenied = a
    }

    fun canPerform(a: CanPerformCheck) {
        _canPerformCheck = a
    }

    fun build() = Command(name, allowedClasses, _canPerformCheck, _onDenied, _action)

}

@CommandBuilderDsl
class AllowedToSetter(val b: CommandBuilder) {

    infix fun to(classes: Set<Classification>) {
        b.allowedClasses = classes
    }

}

class CommandListBuilder {

    val cl = mutableListOf<Command>()

    fun command(name: String? = null, init: Init<CommandBuilder>) {
        val b = CommandBuilder()
        if(name != null) {
            b.name = name
        }
        b.init()
        cl.add(b.build())
    }

}

fun createCommand(init: Init<CommandBuilder>): Command {
    val b = CommandBuilder()
    b.init()
    return b.build()
}

fun commandList(init: Init<CommandListBuilder>): List<Command> {
    val b = CommandListBuilder()
    b.init()
    return b.cl
}