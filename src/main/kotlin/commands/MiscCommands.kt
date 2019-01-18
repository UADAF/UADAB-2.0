package commands

import cmd.CommandCategory
import cmd.CommandListBuilder
import cmd.ICommandList
import cmd.Init
import io.ktor.http.HttpStatusCode
import utils.HTTPStatusCode
import utils.HttpCodes
import java.awt.Color

object MiscCommands : ICommandList {

    override val cat: CommandCategory = CommandCategory("Misc", Color(0x95a3a6), "http://52.48.142.75/images/math_compass.png")

    override fun init(): Init<CommandListBuilder> = {
        command("http") {
            help = "Get info about http codes"
            args = "(i%code%)+? -(-n)o-description"
            val noDescription by parser.flag("no-description", shortname = 'n')
            val arguments by parser.leftoverDelegate()

            action {
                val dataSet = HttpCodes.getDataSet().await()

                val codes: ArrayList<HTTPStatusCode> = ArrayList()
                val invalids: ArrayList<String> = ArrayList()

                arguments.forEach {
                    val intValue = it.toIntOrNull()

                    if (intValue != null && dataSet.containsKey(intValue)) {
                        codes.add(dataSet[intValue]!!)
                    } else  {
                        invalids.add(it)
                    }
                }

                if (codes.isNotEmpty()) replyCat {
                    codes.forEach { code ->
                        if (noDescription.present) {
                            append field "HTTP ${code.code}" to code.phrase
                        } else {
                            page {
                                title = "HTTP ${code.code} - ${code.phrase}"
                                +code.description
                            }
                        }
                    }
                }

                if (invalids.isNotEmpty()) replyCat {
                    title = "These codes I could not recognize"
                    +invalids.joinToString(separator = ", ")
                }

            }
            onDenied {
                replyCat {
                    color = Color.RED
                    title = "Sorry"
                    +"You are not allowed to control me. Get out"
                }
            }
        }
    }

}