package users

import UADAB
import dao.DAOUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.future.await
import net.dv8tion.jda.core.entities.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.Boxes
import utils.ImageUtils
import utils.upsert
import java.awt.image.BufferedImage

data class UADABUser(val name: String, val discord: User, var classification: Classification, var ssn: SSN) {

    init {
        register(this)
    }

    fun save() = transaction {
        saveTransactionless()
    }

    fun getBoxedImageAsync(classif: Classification = classification) =
        GlobalScope.async(Dispatchers.IO) { getBoxedImage(classif) }

    suspend fun getBoxedImage(classif: Classification = classification): BufferedImage {
        val avatar = ImageUtils.readImg(discord.effectiveAvatarUrl).await()
        val box = Boxes.getBox(avatar.width, avatar.height, classif.cornerColor, classif.barColor)
        return ImageUtils.mergeImages(avatar, box)
    }

    private fun saveTransactionless() {
        DAOUsers.upsert(DAOUsers.discordId) {
            it[name] = this@UADABUser.name
            it[discordId] = this@UADABUser.discord.idLong
            it[classification] = this@UADABUser.classification.name
            it[ssn] = this@UADABUser.ssn.intVal
        }
    }

    companion object {

        private val mUsers: MutableList<UADABUser> = mutableListOf()
        private val byId: MutableMap<Long, UADABUser> = mutableMapOf()
        private val byName: MutableMap<String, MutableList<UADABUser>> = mutableMapOf()
        private val bySSN: MutableMap<Int, UADABUser> = mutableMapOf();
        private val log: Logger = LoggerFactory.getLogger("UADABUser")
        val users: List<UADABUser> = mUsers


        private fun register(user: UADABUser) {
            mUsers.add(user)
            byId[user.discord.idLong] = user
            byName.computeIfAbsent(user.name) { mutableListOf() }.add(user)
            bySSN[user.ssn.intVal] = user
        }

        private fun unregister(user: UADABUser) {
            mUsers.remove(user)
            byId.remove(user.discord.idLong)
            byName[user.name]?.remove(user)
            bySSN.remove(user.ssn.intVal)
        }

        operator fun get(name: String) = byName[name]

        operator fun get(ssn: Int) = bySSN[ssn];

        private fun fromDB(r: ResultRow) = with(DAOUsers) {
            UADABUser(
                r[name],
                UADAB.bot.getUserById(r[discordId]) ?: throw UserNotFoundException(r[discordId].toString()),
                Classification[r[classification]],
                SSN(r[ssn])
            )
        }

        private fun tryGetFromDB(id: Long) = transaction { tryGetFromDBTransactionless(id) }

        private fun tryGetFromDBTransactionless(id: Long) = DAOUsers.select { DAOUsers.discordId eq id }.firstOrNull()

        fun loadFromDB() = transaction { loadFromDBTransactionless() }

        fun loadFromDBTransactionless() = DAOUsers.selectAll().forEach { row ->
            try {
                fromDB(row)
            } catch (e: UserNotFoundException) {
                log.warn("Unable to find user ${row[DAOUsers.name]}")
            }
        }

        fun fromDiscord(discord: User, openTransaction: Boolean = true): UADABUser {
            if (discord.idLong in byId) return byId[discord.idLong]!!
            val db = if (openTransaction) tryGetFromDB(discord.idLong) else tryGetFromDBTransactionless(discord.idLong)
            if (db != null) {
                return fromDB(db)
            }
            val ret = UADABUser(
                discord.name,
                discord,
                Classification.IRRELEVANT,
                SSN.randomSSN()
            )
            ret.save()
            return ret
        }

        fun saveAll() = transaction {
            users.forEach(UADABUser::saveTransactionless)
        }
    }

}


class UserNotFoundException(msg: String?, cause: Throwable?) : RuntimeException(msg, cause) {

    constructor(msg: String) : this(msg, null)

    constructor(cause: Throwable?) : this(null, cause)

}