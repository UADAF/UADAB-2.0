package cmd

import dsl.Attachments
import dsl.PaginatedEmbedCreator
import dsl.sendEmbedWithAttachments
import dsl.sendPaginatedEmbed
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import users.UADABUser
import java.io.File

typealias Success<T> = (T) -> Unit
typealias Failure = Success<Throwable>

class CommandContext(val command: Command, val message: Message, val args: List<String>) {

    val author by lazy { UADABUser.fromDiscord(message.author) }

    val guild: Guild
        get() = message.guild

    fun copy(byMessage: Message): CommandContext =
        CommandContext(command, byMessage, args)

    fun reply(msg: CharSequence,
              success: Success<Message>? = null, failure: Failure? = null)
            = message.channel.sendMessage(msg).queue(success, failure)

    fun reply(msg: Message,
              success: Success<Message>? = null, failure: Failure? = null)
            = message.channel.sendMessage(msg).queue(success, failure)

    fun reply(msg: Pair<MessageEmbed, Attachments>,
              success: Success<Message>? = null, failure: Failure? = null)
            = message.channel.sendEmbedWithAttachments(msg).queue(success, failure)

    fun reply(file: File, fileName: String? = file.name, msg: Message? = null,
              success: Success<Message>?, failure: Failure?)
            = message.channel.sendFile(file, fileName, msg).queue(success, failure)

    fun reply(embed: PaginatedEmbedCreator.() -> Unit) = message.channel.sendPaginatedEmbed(embed)

}