package users

import utils.Boxes
import utils.poiColors
import java.awt.Color

data class Classification(val name: String, val cornerColor: Color, val barColor: Color) {


    init {
        classes[name.toLowerCase()] = this
    }


    fun getBox(size: Int) = Boxes.getBox(size, size, cornerColor, barColor)


    companion object {

        private val classes: MutableMap<String, Classification> = mutableMapOf()

        fun getOrNull(s: String) = classes[s.toLowerCase()]

        operator fun get(s: String) = classes[s.toLowerCase()] ?: throw ClassificationNotFoundException(s)

        val IRRELEVANT = Classification("Irrelevant", poiColors["white"]!!, poiColors["white"]!!)
        val ASSET = Classification("Asset", poiColors["yellow"]!!, poiColors["yellow"]!!)
        val ANALOG_INTERFACE = Classification("Analog Interface", poiColors["yellow"]!!, poiColors["black"]!!)
        val IRRELEVANT_THREAT = Classification("Threat", poiColors["red"]!!, poiColors["white"]!!)
        val RELEVANT_THREAT = Classification("Relevant Threat", poiColors["red"]!!, poiColors["white"]!!)
        val RELEVANT_ONE = Classification("Relevant One", poiColors["white"]!!, poiColors["blue"]!!)
        val CATALYST = Classification("Catalyst", poiColors["blue"]!!, poiColors["blue"]!!)
        val ADMIN = Classification("Admin", poiColors["yellow"]!!, poiColors["yellow"]!!)
        val SYSTEM = Classification("System", poiColors["yellow"]!!, poiColors["blue"]!!)
    }
}

class ClassificationNotFoundException(msg: String?, cause: Throwable?) : RuntimeException(msg, cause) {

    constructor(msg: String) : this(msg, null)

    constructor(cause: Throwable?) : this(null, cause)

}