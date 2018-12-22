package cmd

import dsl.PaginatedEmbedCreator
import dsl.sendPaginatedEmbed
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.GenericMessageEvent
import users.UADABUser
import java.io.File

typealias Success<T> = (T) -> Unit
typealias Failure = Success<Throwable>

class CommandContext(val message: Message, val args: List<String>) {

    val author by lazy { UADABUser.fromDiscord(message.author) }

    fun reply(msg: CharSequence,
              success: Success<Message>? = null, failure: Failure? = null)
            = message.channel.sendMessage(msg).queue(success, failure)

    fun reply(msg: Message,
              success: Success<Message>? = null, failure: Failure? = null)
            = message.channel.sendMessage(msg).queue(success, failure)

    fun reply(msg: MessageEmbed,
              success: Success<Message>? = null, failure: Failure? = null)
            = message.channel.sendMessage(msg).queue(success, failure)

    fun reply(file: File, fileName: String? = file.name, msg: Message? = null,
              success: Success<Message>?, failure: Failure?)
            = message.channel.sendFile(file, fileName, msg).queue(success, failure)

    fun reply(embed: PaginatedEmbedCreator.() -> Unit) = message.channel.sendPaginatedEmbed(embed)

}