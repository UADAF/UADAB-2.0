package dsl

import Reactions
import UADAB
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.requests.Route
import net.dv8tion.jda.core.requests.restaction.MessageAction
import kotlin.math.max
import kotlin.math.min

val actionReactions = listOf("\u23ee", "\u23ea", "\u23f9", "\u23e9", "\u23ed")

fun MessageChannel.sendEmbedWithAttachments(e: Pair<MessageEmbed, Attachments>): MessageAction {
    return e.second.asSequence().fold(
        MessageAction(jda, Route.Messages.SEND_MESSAGE.compile(id), this)
    ) { act, (name, stream) -> act.addFile(stream, name) }.embed(e.first)
}

fun MessageChannel.editEmbedWithAttachments(messageId: Long, e: Pair<MessageEmbed, Attachments>): MessageAction {
    return e.second.asSequence().fold(
        MessageAction(
            jda,
            Route.Messages.EDIT_MESSAGE.compile(id, java.lang.Long.toUnsignedString(messageId)),
            this
        ).override(true)
    ) { act, (name, stream) -> act.addFile(stream, name) }.embed(e.first)
}

fun MessageChannel.sendPaginatedEmbed(embed: PaginatedEmbedCreator.() -> Unit) {
    val embeds = paginatedEmbed(embed)
    if (embeds.isEmpty()) {
        return
    }
    if (embeds.size == 1) {
        sendEmbedWithAttachments(embeds[0]).queue()
        return
    }
    sendEmbedWithAttachments(embeds[0]).queue { msg ->
        var curPage = 0
        Reactions.register(msg.id) {
            if (it.user != UADAB.bot.selfUser) {
                var newPage = when (it.reactionEmote.name) {
                    "\u23ee" -> 0
                    "\u23ea" -> curPage - 1
                    "\u23f9" -> {
                        msg.channel.getMessageById(msg.id).queue { m ->
                            m.reactions.forEach { toRemove ->
                                toRemove.users.queue { users ->
                                    users.forEach { u ->
                                        toRemove.removeReaction(u).queue()
                                    }
                                }
                            }
                        }
                        return@register true
                    }
                    "\u23e9" -> curPage + 1
                    "\u23ed" -> embeds.size - 1
                    else -> curPage
                }
                it.reaction.removeReaction(it.user).queue()
                newPage = newPage.coerceIn(embeds.indices)
                if (curPage != newPage) {
                    curPage = newPage
                    editEmbedWithAttachments(msg.idLong, embeds[curPage]).queue()
                }
            }
            return@register false
        }
        GlobalScope.launch {
            actionReactions.map(msg::addReaction).forEach { it.complete() }
        }
    }
}