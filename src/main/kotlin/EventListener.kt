import argparser.tokenize
import dsl.embed
import dsl.sendPaginatedEmbed
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.MessageUpdateEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import org.jetbrains.exposed.sql.transactions.transaction
import users.UADABUser

object EventListener {

    @SubscribeEvent
    fun ReadyEvent.ready() {
        jda.presence.setPresence(OnlineStatus.ONLINE, Game.watching("за пользователями"))
        transaction {
            jda.users.forEach { UADABUser.fromDiscord(it, openTransaction = false) }
        }
    }

    fun GuildMemberJoinEvent.onJoin() {
        UADABUser.fromDiscord(user).let { u ->
            guild.defaultChannel?.sendMessage(embed {
                title = "Info about ${user.name}"
                thumbnail = user.effectiveAvatarUrl
                color = u.classification.cornerColor
                inline field "Classification" to u.classification.name
                inline field "SSN" to u.ssn.getSSNString(redacted = false)
                append field "Discord ID" to user.id
                inline field "Online status" to member.onlineStatus.key.replace("dnd", "do not disturb").capitalize()
            })
        }
    }

    @SubscribeEvent
    fun MessageReceivedEvent.msg() {
        if(message.contentRaw.startsWith("sudo ")) {
            val tokenized = tokenize(message.contentRaw)
            if(tokenized.size > 1) {
                val cmd = tokenized[1]
                val args = tokenized.subList(2, tokenized.size)
                if (cmd == "users") {
                    sendPaginatedEmbed(channel) {
                        for(user in UADABUser.users) {
                            val members = user.discord.mutualGuilds.map { it.getMember(user.discord) }
                            val voice = members.find { it.voiceState.inVoiceChannel() }?.voiceState?.channel?.name ?: "None"
                            page {
                                title = "Info about ${user.name}"
                                thumbnail = user.discord.effectiveAvatarUrl
                                color = user.classification.cornerColor
                                inline field "Classification" to user.classification.name
                                inline field "SSN" to user.ssn.getSSNString(redacted = false)
                                append field "Discord ID" to user.discord.id
                                append field "Voice interface location" to voice
                                inline field "Online status" to members[0].onlineStatus.key.replace("dnd", "do not disturb").capitalize()
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun MessageUpdateEvent.edit() {

    }

}