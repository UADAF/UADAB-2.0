package dao

import org.jetbrains.exposed.sql.Table

object DAOUsers : Table("users") {
    val discordId = long("discord_id").primaryKey()
    val name = varchar("name", 255)
    val classification = varchar("classification", 255)
    val ssn = integer("ssn")
}