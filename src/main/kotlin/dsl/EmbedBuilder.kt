package dsl

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color
import java.io.*
import java.time.temporal.TemporalAccessor

typealias Init<T> = T.() -> Unit

@DslMarker
annotation class EmbedDsl

typealias Attachments = Map<String, InputStream>

@EmbedDsl
open class BaseEmbedCreater {

    var builder = EmbedBuilder()

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

    var image: String? = null
        set(value) {
            field = value
            builder.setImage(value)
        }

    var attachments: MutableMap<String, InputStream> = mutableMapOf()

    val append by lazy { FieldHolder(this, false) }
    val inline by lazy { FieldHolder(this, true) }


    open fun text(s: String) {
        builder.appendDescription(s)
    }

    open fun text(init: BaseEmbedCreater.() -> String) {
        text(init())
    }

    open operator fun String.unaryPlus() {
        text(this)
    }

    open operator fun String.unaryMinus() {
        +"$this\n"
    }

    open fun field(init: Init<FieldBuilder>) {
        setElement(::FieldBuilder, init)
    }

    open fun author(init: Init<AuthorBuilder>) {
        setElement(::AuthorBuilder, init)
    }

    open fun footer(init: Init<FooterBuilder>) {
        setElement(::FooterBuilder, init)
    }

    open infix fun String.attach(stream: InputStream): String {
        attachments[this] = stream
        return "attachment://${this}"
    }

    open infix fun String.attach(stream: File): String {
        attachments[this] = FileInputStream(stream)
        return "attachment://${this}"
    }

    open infix fun String.attach(stream: ByteArray): String {
        attachments[this] = ByteArrayInputStream(stream)
        return "attachment://${this}"
    }

    private inline fun <T : ElementBuilder> setElement(eBuilder: (EmbedBuilder) -> T, init: Init<T>) {
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
        if(e != null) {
            text = e.text
            icon = e.iconUrl
        }
    }

    override fun complete() {
        builder.setFooter(text, icon)
    }

}

class AuthorBuilder(private val builder: EmbedBuilder) : ElementBuilder {
    var name: String? = null
    var url: String? = null
    var icon: String? = null

    init {
        val e = builder.build().author
        if(e != null) {
            name = e.name
            url = e.url
            icon = e.iconUrl
        }
    }

    override fun complete() {
        builder.setAuthor(name, url, icon)
    }

}

inline fun embed(init: Init<BaseEmbedCreater>): Pair<MessageEmbed, Attachments> {
    val e = BaseEmbedCreater()
    e.init()
    return e.builder.build() to e.attachments.toMap()
}