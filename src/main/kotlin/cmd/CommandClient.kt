package cmd

import argparser.tokenize
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.GenericMessageEvent

class CommandClient {

    enum class ExecutionResult {
        SUCCESS,
        NOT_FOUND,
        NOT_A_COMMAND,
        ERROR
    }

    private val _commands: MutableMap<String, Command> = mutableMapOf()

    val commands: Map<String, Command>
        get() = _commands


    operator fun get(name: String): Command? = commands[name]

    fun register(command: Command) {
        _commands[command.name] = command
    }

    fun register(vararg commands: Command) {
        commands.forEach(::register)
    }

    fun register(commands: Iterable<Command>) {
        commands.forEach(::register)
    }

    fun register(commands: ICommandList) {
        register(commandList(commands.init()))
    }

    suspend fun handle(message: Message): Pair<ExecutionResult, String> {
        val tokenized = tokenize(message.contentRaw)
        if(tokenized.size < 2 || tokenized[0] != "sudo") {
            return ExecutionResult.NOT_A_COMMAND to ""
        }
        val cmd = tokenized[1]
        val args = tokenized.subList(2, tokenized.size)
        if(cmd == "!!") {
            TODO("Command repeat")
        }
        val command = this[cmd] ?: return ExecutionResult.NOT_FOUND to cmd
        val context = CommandContext(message, args)


        try {
            if(command.canPerform(context)) {
                command.perform(context)
            } else {
                command.deny(context)
            }
        } catch (e: Exception) {
            return ExecutionResult.ERROR to e.localizedMessage
        }


        return ExecutionResult.SUCCESS to ""
    }

}