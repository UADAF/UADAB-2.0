package sources

import io.ktor.client.request.get
import java.awt.Color


typealias ColorData = Map<String, Color>

object XkcdColorSource : BasicExternalSource<ColorData>() {

    override suspend fun load() = UADAB.http.get<String>("http://xkcd.com/color/rgb.txt").removeSuffix("\n")
        .lineSequence()
        .drop(1) //Skip license
        .map { it.split("\t") }
        .map { it[0] to it[1] }
        .associate { (name, color) -> name to Color(color.removePrefix("#").toInt(16)) }

}