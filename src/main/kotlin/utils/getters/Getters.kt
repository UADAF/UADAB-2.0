package getters

import UADAB
import users.UADABUser
import net.dv8tion.jda.core.entities.Member
import utils.getters.Wrapper
import java.util.stream.Collectors

object Getters {

    private val mention = Regex("<?@!?(\\d+)+>?")
    private val ssn = Regex("\\d{3}-\\d{2}-\\d{4}")
    fun getUser(from: String): Wrapper<UADABUser> {
        val name = extractMention(from)
        val user = (bySSN(name) ?: byId(name))?.let(::listOf)
        return Wrapper(user ?: UADABUser[name] ?: byName(name))
    }

    private fun extractMention(from: String): String {
        val matcher = mention.find(from)
        return if (matcher != null) matcher.groupValues[1] else from
    }

    private fun bySSN(from: String) = if (from.matches(ssn)) {
        var ssn = 0
        ssn += Integer.parseInt(from.substring(0, 3)) * 1000000
        ssn += Integer.parseInt(from.substring(4, 6)) * 10000
        ssn += Integer.parseInt(from.substring(7))
        UADABUser[ssn]
    } else null

    private fun byId(from: String) = if (from.matches("\\d+".toRegex())) {
        UADABUser.fromDiscord(UADAB.bot.getUserById(from))
    } else null

    private fun byName(from: String): List<UADABUser> {
        var users = UADAB.bot.getUsersByName(from, true)
        if (users.isEmpty()) {
            users = UADAB.bot.guilds.parallelStream()
                .flatMap { g -> g.getMembersByEffectiveName(from, true).parallelStream() }.map(Member::getUser)
                .distinct()
                .collect(Collectors.toList())
        }
        return users.map { UADABUser.fromDiscord(it) }
    }

}