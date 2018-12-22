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

val ADMIN_ONLY = setOf(ADMIN)
val ADMIN_OR_INTERFACE = setOf(ADMIN, ANALOG_INTERFACE)
val ASSETS = setOf(ADMIN, ANALOG_INTERFACE, ASSET, CATALYST)
val NORMAL = setOf(ADMIN, SYSTEM, ASSET, ANALOG_INTERFACE, CATALYST, IRRELEVANT, RELEVANT_ONE)
val EVERYONE = setOf(ADMIN, SYSTEM, ASSET, ANALOG_INTERFACE, CATALYST, IRRELEVANT, RELEVANT_ONE,
    IRRELEVANT_THREAT, RELEVANT_THREAT)