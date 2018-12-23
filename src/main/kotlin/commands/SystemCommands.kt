package commands

import argparser.ArgParser
import cmd.CommandListBuilder
import cmd.ICommandList
import cmd.Init
import users.EVERYONE
import java.awt.Color

object SystemCommands : ICommandList {


    override fun init(): Init<CommandListBuilder> = {
        command("pi") {
            aliases {
                +"π"
            }
            allowed to EVERYONE
            val binary by parser.flag("binary", shortname = 'b')
            val octal by parser.flag("octal", shortname = 'o')
            val decimal by parser.flag("decimal", shortname = 'd')
            val hexadecimal by parser.flag("hexadecimal", shortname = 'x')

            fun stringify(toString: (Long) -> String): String {
                val (intPart, fracPart) = Math.PI.toString().split('.')
                return "${toString(intPart.toLong())}.${toString(fracPart.toLong())}"
            }

            action {
                reply {
                    parser.with(args) {
                        pattern {
                            color = Color.GREEN
                        }
                        if (binary.present) page {
                            title = "Binary π"
                            +stringify(java.lang.Long::toBinaryString)
                        }
                        if (octal.present) page {
                            title = "Octal π"
                            +stringify(java.lang.Long::toOctalString)
                        }
                        if (decimal.present || !(binary.present || octal.present || hexadecimal.present)) page {
                            title = "Decimal π"
                            +stringify(Long::toString)
                        }
                        if (hexadecimal.present) page {
                            title = "Hexadecimal π"
                            +stringify(java.lang.Long::toHexString)
                        }
                    }
                }
            }
        }
    }


}