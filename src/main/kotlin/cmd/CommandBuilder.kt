@file:Suppress("REDUNDANT_NULLABLE")

package cmd

import argparser.ArgParser
import users.Classification
import users.NORMAL
import kotlin.reflect.jvm.isAccessible


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
    var aliases = mutableListOf<String>()
    val allowed by lazy { AllowedToSetter(this) }

    val parser by lazy { ArgParser() }

    fun action(a: CommandAction) {
        _action = {
            parser.with(args) {
                a()
            }
        }
    }

    fun onDenied(a: CommandAction) {
        _onDenied = a
    }

    fun canPerform(a: CanPerformCheck) {
        _canPerformCheck = a
    }

    fun aliases(init: Init<AliasesBuilder>) {
        val b = AliasesBuilder(this)
        b.init()
        aliases.addAll(b.aliases)
    }

    fun build() = Command(name, aliases, allowedClasses, _canPerformCheck, _onDenied, _action)

}

@CommandBuilderDsl
class AllowedToSetter(val b: CommandBuilder) {

    infix fun to(classes: Set<Classification>) {
        b.allowedClasses = classes
    }

}

@CommandBuilderDsl
class AliasesBuilder(val b: CommandBuilder) {

    val aliases = mutableListOf<String>()

    operator fun String.unaryPlus() {
        aliases.add(this)
    }

}

@CommandBuilderDsl
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