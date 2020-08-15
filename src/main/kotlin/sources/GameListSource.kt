package sources

import dao.DAOPhrases
import net.dv8tion.jda.core.entities.Game
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import utils.WeightedList
import utils.toWeightedList
import java.io.File


typealias GameList = WeightedList<Game>

object GameListSource : BasicExternalSource<GameList>() {
    override suspend fun load(): GameList = transaction {
        DAOPhrases.selectAll()
            .map { it[DAOPhrases.weight] to Game.of(it[DAOPhrases.type], it[DAOPhrases.phrase]) }
            .toWeightedList()
    }
}