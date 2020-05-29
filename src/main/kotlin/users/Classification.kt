package users

import utils.Boxes
import utils.poiColors
import java.awt.Color

data class Classification(val name: String, val cornerColor: Color, val barColor: Color, val permissionLevel: Int) {


    init {
        classes[name.toLowerCase()] = this
    }


    fun getBox(size: Int) = Boxes.getBox(size, size, cornerColor, barColor)


    companion object {

        private val classes: MutableMap<String, Classification> = mutableMapOf()

        fun getOrNull(s: String) = classes[s.toLowerCase()]

        operator fun get(s: String) = classes[s.toLowerCase()] ?: throw ClassificationNotFoundException(s)

        val IRRELEVANT = Classification("Irrelevant", poiColors.getValue("white"), poiColors.getValue("white"), 0)
        val ASSET = Classification("Asset", poiColors.getValue("yellow"), poiColors.getValue("yellow"), 100)
        val ANALOG_INTERFACE = Classification("Analog Interface", poiColors.getValue("yellow"), poiColors.getValue("black"), 1000)
        val IRRELEVANT_THREAT = Classification("Threat", poiColors.getValue("red"), poiColors.getValue("white"), -10)
        val RELEVANT_THREAT = Classification("Relevant Threat", poiColors.getValue("red"), poiColors.getValue("white"), -1000)
        val RELEVANT_ONE = Classification("Relevant One", poiColors.getValue("white"), poiColors.getValue("blue"), 0)
        val CATALYST = Classification("Catalyst", poiColors.getValue("blue"), poiColors.getValue("blue"), 10)
        val ADMIN = Classification("Admin", poiColors.getValue("yellow"), poiColors.getValue("yellow"), 10000000)
        val SYSTEM = Classification("System", poiColors.getValue("yellow"), poiColors.getValue("blue"), 10000001)
    }
}

class ClassificationNotFoundException(msg: String?, cause: Throwable?) : RuntimeException(msg, cause) {

    constructor(msg: String) : this(msg, null)

    constructor(cause: Throwable?) : this(null, cause)

}