package dao

import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

class BashQuote {

    @Selector(".quote__header_permalink")
    lateinit var id: String

    @Selector(".quote__header_date")
    lateinit var date: String

    @Selector(".quote__body", converter = BashContentConverter::class)
    lateinit var content: String

    @Selector(".quote__total")
    lateinit var rating: String
}

class BashContentConverter: ElementConverter<String> {
    override fun convert(node: Element, selector: Selector): String { // It should be replaced with NORMAL <br> replacing :(
        node.children().replaceAll {
            if (it.tagName() == "br")
                Element("p").apply { it.text(":;:;:") }
            else it
        }
        return node.text().replace(":;:;:", "\n")
    }

}