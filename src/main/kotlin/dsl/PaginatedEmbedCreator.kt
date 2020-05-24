package dsl

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed

@EmbedDsl
class PatternEmbedCreator : BaseEmbedCreater()


fun EmbedBuilder.clone() = if (isEmpty) EmbedBuilder() else EmbedBuilder(this)


@EmbedDsl
class PaginatedEmbedCreator : BaseEmbedCreater() {
    private val pattern = PatternEmbedCreator()
    private var onBreak: Init<PaginatedEmbedCreator> = {}
    val result = mutableListOf<EmbedBuilder>()

    fun onBreak(action: Init<PaginatedEmbedCreator>) {
        onBreak = action
    }

    fun pattern(init: Init<PatternEmbedCreator>) {
        pattern.init()
        builder = pattern.builder.clone()
    }

    fun breakPage() {
        onBreak()
        result.add(builder)
        builder = pattern.builder.clone()
    }

    override fun text(s: String) {
        if (builder.descriptionBuilder.length + s.length >= MessageEmbed.TEXT_MAX_LENGTH) breakPage()
        super.text(s)
    }

    fun page(init: Init<BaseEmbedCreater>) {
        init()
        breakPage()
    }

    override fun field(init: Init<FieldBuilder>) {
        if (builder.fields.size > 23) {
            breakPage()
        }
        super.field(init)
    }

    fun finish(): List<MessageEmbed> {
        if (!builder.isEmpty && (!pattern.builder.isEmpty && builder.build() != pattern.builder.build())) {
            breakPage()
        }
        if(result.size == 1) {
            return listOf(result[0].build())
        }
        return result.mapIndexed { i, e -> e.setFooter("Page ${i + 1}/${result.size}", null).build() }
    }
}


inline fun paginatedEmbed(init: PaginatedEmbedCreator.() -> Unit): List<MessageEmbed> {
    val b = PaginatedEmbedCreator()
    b.init()
    return b.finish()
}