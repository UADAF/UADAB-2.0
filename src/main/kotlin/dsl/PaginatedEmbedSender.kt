package dsl

import Reactions
import UADAB
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.entities.MessageChannel

val actionReactions = listOf("\u23ee", "\u23ea", "\u23f9", "\u23e9", "\u23ed")
fun MessageChannel.sendPaginatedEmbed(embed: PaginatedEmbedCreator.() -> Unit) {
    val embeds = paginatedEmbed(embed)
    if(embeds.isEmpty()) {
        return
    }
    if(embeds.size == 1) {
        sendMessage(embeds[0]).queue()
        return
    }
    sendMessage(embeds[0]).queue { msg ->
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
                newPage = Math.max(0, Math.min(newPage, embeds.size - 1))
                if (curPage != newPage) {
                    curPage = newPage
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