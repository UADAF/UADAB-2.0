package dsl

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed

@EmbedDsl
class PatternEmbedCreator : BaseEmbedCreater()


fun EmbedBuilder.clone(): EmbedBuilder {
    return if (isEmpty) EmbedBuilder() else EmbedBuilder(this)
}


@EmbedDsl
class PaginatedEmbedCreator : BaseEmbedCreater() {
    private val pattern = PatternEmbedCreator()
    private var onBreak: PaginatedEmbedCreator.() -> Unit = {}
    val result = mutableListOf<EmbedBuilder>()

    fun onBreak(action: PaginatedEmbedCreator.() -> Unit) {
        onBreak = action
    }

    fun pattern(init: PatternEmbedCreator.() -> Unit) {
        pattern.init()
        builder = pattern.builder.clone()
    }

    fun breakPage() {
        onBreak()
        result.add(builder)
        builder = pattern.builder.clone()
    }

    override fun String.unaryPlus() {
        if (builder.descriptionBuilder.length + length >= MessageEmbed.TEXT_MAX_LENGTH) {
            breakPage()
        }
        builder.appendDescription(this) //Can't call super-extension
    }

    fun page(init: BaseEmbedCreater.() -> Unit) {
        init()
        breakPage()
    }

    override fun field(init: FieldBuilder.() -> Unit) {
        if (builder.fields.size >= 24) {
            breakPage()
        }
        super.field(init)
    }

    fun finish() = result.mapIndexed { i, e -> e.setFooter("Page ${i + 1}/${result.size}", null).build() }
}

fun paginatedEmbed(init: PaginatedEmbedCreator.() -> Unit): List<MessageEmbed> {
    val b = PaginatedEmbedCreator()
    b.init()
    return b.finish()
}