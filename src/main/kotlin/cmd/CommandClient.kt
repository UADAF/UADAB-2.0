package cmd

import argparser.tokenize
import net.dv8tion.jda.core.entities.Message
import uadamusic.MusicContext
import utils.exception.MusicContextInitializationException

class CommandClient(val prefix: String) {

    enum class ExecutionResult {
        SUCCESS,
        NOT_FOUND,
        NOT_A_COMMAND,
        MUSIC_CONTEXT_ERROR,
        ERROR
    }

    private val _commands: MutableMap<String, Command> = mutableMapOf()

    val commands: Map<String, Command>
        get() = _commands

    private val _commandCache: MutableMap<String, CommandContext> = mutableMapOf()

    val commandCache: Map<String, CommandContext>
        get() = _commandCache

    operator fun get(name: String): Command? = commands[name]

    fun register(command: Command) {
        _commands[command.name] = command
        command.aliases.forEach { _commands[it] = command }
    }

    fun register(vararg commands: Command) {
        commands.forEach(::register)
    }

    fun register(commands: Iterable<Command>) {
        commands.forEach(::register)
    }

    fun register(commands: ICommandList) {
        register(commandList(commands.init(), commands.cat))
    }

    fun register(vararg commands: ICommandList) {
        commands.forEach(::register)
    }

    suspend fun handle(message: Message): Pair<ExecutionResult, String> {
        val tokenized = tokenize(message.contentRaw)
        if(tokenized.size < 2 || tokenized[0] != prefix) {
            return ExecutionResult.NOT_A_COMMAND to ""
        }
        val cmd = tokenized[1]
        val args = tokenized.subList(2, tokenized.size)
        val context = if(cmd == "!!") {
           commandCache[message.guild.id]?.copy(message)
                ?: return ExecutionResult.NOT_FOUND to "First message in guild"
        } else {
            val command = this[cmd]
                ?: return ExecutionResult.NOT_FOUND to cmd
            CommandContext(command, message, args)
        }
        return executeByContext(context)
    }

    private suspend fun executeByContext(context: CommandContext): Pair<ExecutionResult, String> {
        val command = context.command
        try {
            if (command.canPerform(context)) {
                command.perform(context)
            } else {
                command.deny(context)
            }
            _commandCache[context.guild.id] = context
        } catch (e: MusicContextInitializationException) {
            return ExecutionResult.MUSIC_CONTEXT_ERROR to e.msg
        } catch (e: Exception) {
            e.printStackTrace()
            return ExecutionResult.ERROR to "${e.javaClass.simpleName}: ${e.localizedMessage}"
        }
        return ExecutionResult.SUCCESS to ""
    }

}