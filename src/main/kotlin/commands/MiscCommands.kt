package commands

import argparser.spec.RangeArgResult
import cmd.CommandCategory
import cmd.CommandListBuilder
import cmd.ICommandList
import cmd.Init
import sources.HttpCodeSource
import sources.get
import utils.extractCount
import java.awt.Color
import java.awt.Color.RED

object MiscCommands : ICommandList {

    override val cat: CommandCategory = CommandCategory("Misc", Color(0x95a3a6), "http://52.48.142.75/images/math_compass.png")

    override fun init(): Init<CommandListBuilder> = {
        command("http") {
            help = "Get info about http codes"
            args = "(i%code%)+? -(-n)o-description"
            val noDescription by parser.flag("no-description", shortname = 'n')
            val arguments by parser.leftoverDelegate()

            action {
                val dataSet = HttpCodeSource.get()

                val codes: ArrayList<HttpCodeSource.HTTPStatusCode> = ArrayList()
                val invalids: ArrayList<String> = ArrayList()

                arguments.forEach {
                    val intValue = it.toIntOrNull()

                    if (intValue != null && dataSet.containsKey(intValue)) {
                        codes.add(dataSet.getValue(intValue))
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
        command("quote") {
            help = "Add or get quote"
            val quoteRange by parser.range("quoteRange")
            val all by parser.flag("all", shortname = 'a')
            val arguments by parser.leftoverDelegate()
            action {
                val (count, leftover) = extractCount(arguments)
                if(count != 1 && (leftover.isNotEmpty() || quoteRange.isNotEmpty || all.present)) {
                    replyCat {
                        title = "Invalid args"
                        color = RED
                        +"Count can't be specified with other args"
                    }
                    return@action
                }
                if(all.present && quoteRange.isNotEmpty) {
                    replyCat {
                        title = "Invalid args"
                        color = RED
                        +"Can't have both range and --all"
                    }
                    return@action
                }
                if(leftover.isNotEmpty()) {
                    if(leftover[0] == "add") {
                        if (all.present || quoteRange.isNotEmpty) {
                            replyCat {
                                title = "Invalid args"
                                color = RED
                                +"--all and range are not allowed when adding, enclode them in ' or \" if they are part of the quote"
                            }
                            return@action
                        }
                        TODO("Add quote")
                    } else {
                        replyCat {
                            title = "Invalid args"
                            color = RED
                            +"Unknown args: ${leftover.joinToString(" ")}"
                        }
                    }
                } else {
                    when {
                        all.present -> TODO("Display all quotes")
                        quoteRange.isNotEmpty -> TODO("Display quote range")
                        else -> TODO("Display random quotes")
                    }
                }
            }
        }
    }

    val RangeArgResult.isEmpty
        get() = from == null && to == null

    val RangeArgResult.isNotEmpty
        get() = !isEmpty

}