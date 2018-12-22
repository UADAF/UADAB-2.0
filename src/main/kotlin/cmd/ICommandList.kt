package cmd

interface ICommandList {

    fun init(): Init<CommandListBuilder>

}