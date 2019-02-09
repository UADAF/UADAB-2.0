package utils

import java.awt.Color
import kotlin.random.Random
import kotlin.random.nextInt

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