package users
import users.Classification.Companion.ADMIN
import users.Classification.Companion.ANALOG_INTERFACE
import users.Classification.Companion.ASSET
import users.Classification.Companion.CATALYST
import users.Classification.Companion.IRRELEVANT
import users.Classification.Companion.IRRELEVANT_THREAT
import users.Classification.Companion.RELEVANT_ONE
import users.Classification.Companion.RELEVANT_THREAT
import users.Classification.Companion.SYSTEM

val admin = setOf(ADMIN)
val admin_or_interface = setOf(ADMIN, ANALOG_INTERFACE)
val assets = setOf(ADMIN, ANALOG_INTERFACE, ASSET, CATALYST)
val default = setOf(ADMIN, SYSTEM, ASSET, ANALOG_INTERFACE, CATALYST, IRRELEVANT, RELEVANT_ONE)
val everyone = setOf(ADMIN, SYSTEM, ASSET, ANALOG_INTERFACE, CATALYST, IRRELEVANT, RELEVANT_ONE,
    IRRELEVANT_THREAT, RELEVANT_THREAT)