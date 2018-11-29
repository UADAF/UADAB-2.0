package utils

import UADAB
import io.ktor.client.request.get
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.awt.Color
import kotlin.random.Random
import kotlin.random.nextInt

private lateinit var xkcdColors: Map<String, Color>

fun getXkcdColors(): Deferred<Map<String, Color>> {
    return if (::xkcdColors.isInitialized) {
        CompletableDeferred(xkcdColors)
    } else {
        GlobalScope.async { loadColors() }
    }
}

private suspend fun loadColors(): Map<String, Color> {
    xkcdColors = UADAB.http.get<String>("http://xkcd.com/color/rgb.txt").removeSuffix("\n")
            .lineSequence()
            .drop(1) //Skip license
            .map { it.split("\t") }
            .map { it[0] to it[1] }
            .associate { (name, color) -> name to Color(color.removePrefix("#").toInt(16)) }
    return xkcdColors
}

val poiColors: Map<String, Color> by lazy {
    mapOf(
            "white" to Color.WHITE,
            "red" to Color(0xEB1C24),
            "yellow" to Color(0xEEE93C),
            "blue" to Color(0x116BF6),
            "black" to Color.BLACK
    )
}

fun randomColor(rMin: Int = 0, rMax: Int = 0xFF,
                gMin: Int = 0, gMax: Int = 0xFF,
                bMin: Int = 0, bMax: Int = 0xFF): Color {
    val r = Random.nextInt(rMin..rMax)
    val g = Random.nextInt(gMin..gMax)
    val b = Random.nextInt(bMin..bMax)
    return Color(r, g, b)
}