package cmd

import dsl.PaginatedEmbedCreator

interface ICommandList {

    fun init(): Init<CommandListBuilder>

    val cat: CommandCategory


    fun CommandContext.replyCat(embed: PaginatedEmbedCreator.() -> Unit) = reply {
        pattern {
            thumbnail = cat.img
        }
        embed()
    }

}