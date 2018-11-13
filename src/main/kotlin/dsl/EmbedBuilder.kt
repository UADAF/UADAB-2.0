package dsl

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color
import java.time.temporal.TemporalAccessor


@DslMarker
annotation class EmbedDsl

@EmbedDsl
open class BaseEmbedCreater {

    var builder = EmbedBuilder()

    open operator fun String.unaryPlus() {
        builder.appendDescription(this)
    }

    var thumbnail: String? = null
        set(value) {
            field = value
            builder.setThumbnail(value)
        }

    var color: Color? = null
        set(value) {
            field = value
            builder.setColor(value)
        }

    var timestamp: TemporalAccessor? = null
        set(value) {
            field = value
            builder.setTimestamp(value)
        }

    var title: String? = null
        set(value) {
            field = value
            builder.setTitle(value, url)
        }

    var url: String? = null
        set(value) {
            field = value
            builder.setTitle(title, value)
        }

    val append by lazy { FieldHolder(this, false) }
    val inline by lazy { FieldHolder(this, true) }

    open fun text(init: BaseEmbedCreater.() -> String) {
        +init()
    }

    open fun field(init: FieldBuilder.() -> Unit) {
        setElement(::FieldBuilder, init)
    }

    open fun author(init: AuthorBuilder.() -> Unit) {
        setElement(::AuthorBuilder, init)
    }

    open fun footer(init: FooterBuilder.() -> Unit) {
        setElement(::FooterBuilder, init)
    }

    private inline fun <T : ElementBuilder> setElement(eBuilder: (EmbedBuilder) -> T, init: T.() -> Unit) {
        val b = eBuilder(this.builder)
        b.init()
        b.complete()
    }

}


class FieldHolder(private val builder: BaseEmbedCreater, private val inline: Boolean) {

    class FieldBase(private val builder: BaseEmbedCreater, val name: String, private val inline: Boolean) {

        infix fun to(value: String) {
            builder.field {
                name = this@FieldBase.name
                this.value = value
                inline = this@FieldBase.inline
            }
        }

    }

    infix fun field(name: String) = FieldBase(builder, name, inline)

}

@EmbedDsl
interface ElementBuilder {
    fun complete()
}

class FieldBuilder(val builder: EmbedBuilder) : ElementBuilder {

    var name: String? = null
    var value: String? = null
    var inline = false


    override fun complete() {
        builder.addField(name, value, inline)
    }

}

class FooterBuilder(private val builder: EmbedBuilder) : ElementBuilder {

    var text: String? = null
    var icon: String? = null

    init {
        val e = builder.build().footer
        text = e.text
        icon = e.iconUrl
    }

    override fun complete() {
        builder.setTitle(text, icon)
    }

}

class AuthorBuilder(private val builder: EmbedBuilder) : ElementBuilder {
    var name: String? = null
    var url: String? = null
    var icon: String? = null

    init {
        val e = builder.build().author
        name = e.name
        url = e.url
        icon = e.iconUrl
    }

    override fun complete() {
        builder.setAuthor(name, url, icon)
    }

}

fun embed(init: BaseEmbedCreater.() -> Unit): MessageEmbed {
    val e = BaseEmbedCreater()
    e.init()
    return e.builder.build()
}