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

        val IRRELEVANT = Classification("Irrelevant", poiColors.getValue("white"), poiColors.getValue("white"))
        val ASSET = Classification("Asset", poiColors.getValue("yellow"), poiColors.getValue("yellow"))
        val ANALOG_INTERFACE = Classification("Analog Interface", poiColors.getValue("yellow"), poiColors.getValue("black"))
        val IRRELEVANT_THREAT = Classification("Threat", poiColors.getValue("red"), poiColors.getValue("white"))
        val RELEVANT_THREAT = Classification("Relevant Threat", poiColors.getValue("red"), poiColors.getValue("white"))
        val RELEVANT_ONE = Classification("Relevant One", poiColors.getValue("white"), poiColors.getValue("blue"))
        val CATALYST = Classification("Catalyst", poiColors.getValue("blue"), poiColors.getValue("blue"))
        val ADMIN = Classification("Admin", poiColors.getValue("yellow"), poiColors.getValue("yellow"))
        val SYSTEM = Classification("System", poiColors.getValue("yellow"), poiColors.getValue("blue"))
    }
}

class ClassificationNotFoundException(msg: String?, cause: Throwable?) : RuntimeException(msg, cause) {

    constructor(msg: String) : this(msg, null)

    constructor(cause: Throwable?) : this(null, cause)

}