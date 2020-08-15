package dao

import net.dv8tion.jda.core.entities.Game
import org.jetbrains.exposed.sql.Table

object DAOUsers : Table("users") {
    val discordId = long("discord_id").primaryKey()
    val name = varchar("name", 255)
    val classification = varchar("classification", 255)
    val ssn = integer("ssn")
}

object DAOPhrases : Table("idle_phrases") {
    val type = enumerationByName("type",
        Game.GameType.values().map { it.name.length }.max() ?: 9,
        Game.GameType::class.java)
    val phrase = text("phrase")
    val weight = float("weight")
}