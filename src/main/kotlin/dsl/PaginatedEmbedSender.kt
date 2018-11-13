package dsl

import Reactions
import UADAB
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.entities.MessageChannel

val actionReactions = listOf("\u23ee", "\u23ea", "\u23f9", "\u23e9", "\u23ed")
fun sendPaginatedEmbed(channel: MessageChannel, embed: PaginatedEmbedCreator.() -> Unit) {
    val embeds = paginatedEmbed(embed)
    channel.sendMessage(embeds[0]).queue { msg ->
        var curPage = 0
        Reactions.register(msg.id) {
            if (it.user != UADAB.bot.selfUser) {
                val newPage = when (it.reactionEmote.name) {
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
                if (curPage != newPage) {
                    curPage = Math.max(0, Math.min(newPage, embeds.size - 1))
                    msg.editMessage(embeds[curPage]).queue()
                }
            }
            return@register false
        }
        GlobalScope.launch {
            actionReactions.map(msg::addReaction).forEach { it.complete() }
        }
    }
}