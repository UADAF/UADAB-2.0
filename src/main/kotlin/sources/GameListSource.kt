package sources

import net.dv8tion.jda.core.entities.Game
import java.io.File


typealias GameList = List<Game>

object GameListSource : BasicExternalSource<GameList>() {


    override suspend fun load(): GameList = File("idlePhrases.txt").useLines { f ->
        f.map { it.split(":", limit = 2) }
            .map { (type, text) -> Game.of(Game.GameType.valueOf(type), text) }
            .toList()
    }


}