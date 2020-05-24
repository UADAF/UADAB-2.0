package dsl

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import java.io.InputStream

@EmbedDsl
class PatternEmbedCreator : BaseEmbedCreater()


fun EmbedBuilder.clone() = if (isEmpty) EmbedBuilder() else EmbedBuilder(this)


@EmbedDsl
class PaginatedEmbedCreator : BaseEmbedCreater() {
    private val pattern = PatternEmbedCreator()
    private var onBreak: Init<PaginatedEmbedCreator> = {}
    val result = mutableListOf<Pair<EmbedBuilder, Map<String, InputStream>>>()

    fun onBreak(action: Init<PaginatedEmbedCreator>) {
        onBreak = action
    }

    private fun resetFromPattern() {
        builder = pattern.builder.clone()
        attachments = mutableMapOf<String, InputStream>().apply { putAll(pattern.attachments) }
    }

    fun pattern(init: Init<PatternEmbedCreator>) {
        pattern.init()
        resetFromPattern()
    }

    fun breakPage() {
        onBreak()
        result.add(builder to attachments)
        resetFromPattern()
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

    fun finish(): List<Pair<MessageEmbed, Attachments>> {
        if (!builder.isEmpty && (!pattern.builder.isEmpty && builder.build() != pattern.builder.build())) {
            breakPage()
        }
        if(result.size == 1) {
            return listOf(result[0].let { (builder, att) -> builder.build() to att })
        }
        return result.mapIndexed { i, (e, att) ->
            e.setFooter("Page ${i + 1}/${result.size}", null).build() to att
        }
    }
}


inline fun paginatedEmbed(init: PaginatedEmbedCreator.() -> Unit): List<Pair<MessageEmbed, Attachments>> {
    val b = PaginatedEmbedCreator()
    b.init()
    return b.finish()
}