package argparser

import org.junit.Test
import org.junit.Assert.*
class TokenizerTests {

    @Test
    fun simple() {
        val tokenized = tokenize("asd dsa")
        assertEquals(listOf("asd", "dsa"), tokenized)
    }

    @Test
    fun singleQuotes() {
        val tokenized = tokenize("asd 'asd dsa' dsa")
        assertEquals(listOf("asd", "asd dsa", "dsa"), tokenized)
    }

    @Test
    fun doubleQuotes() {
        val tokenized = tokenize("asd \"asd dsa\" dsa")
        assertEquals(listOf("asd", "asd dsa", "dsa"), tokenized)
    }

    @Test
    fun nestedSingleQuotes() {
        val tokenized = tokenize("asd \"asd' 'dsa\" dsa")
        assertEquals(listOf("asd", "asd' 'dsa", "dsa"), tokenized)
    }

    @Test
    fun nestedDoubleQuotes() {
        val tokenized = tokenize("asd 'asd\" \"dsa' dsa")
        assertEquals(listOf("asd", "asd\" \"dsa", "dsa"), tokenized)
    }

    @Test
    fun nestedSingleQuote() {
        val tokenized = tokenize("asd \"asd ' dsa\" dsa")
        assertEquals(listOf("asd", "asd ' dsa", "dsa"), tokenized)
    }

    @Test
    fun nestedDoubleQuote() {
        val tokenized = tokenize("asd 'asd \" dsa' dsa")
        assertEquals(listOf("asd", "asd \" dsa", "dsa"), tokenized)
    }

    @Test
    fun multipleSingleQuotes() {
        val tokenized = tokenize("asd 'asd dsa' dsa 'sda ads'")
        assertEquals(listOf("asd", "asd dsa", "dsa", "sda ads"), tokenized)
    }

    @Test
    fun multipleDoubleQuotes() {
        val tokenized = tokenize("asd \"asd dsa\" dsa \"sda ads\"")
        assertEquals(listOf("asd", "asd dsa", "dsa", "sda ads"), tokenized)
    }

    @Test
    fun unclosedSingleQuote() {
        try {
            tokenize("asd 'asd dsa dsa")
            fail("Should not be parsed")
        } catch(e: IllegalArgumentException) {}
    }

    @Test
    fun unclosedDoubleQuote() {
        try {
            tokenize("asd \"asd dsa dsa")
            fail("Should not be parsed")
        } catch(e: IllegalArgumentException) {}
    }

    @Test
    fun overlappingSingleQuotes() {
        try {
            tokenize("asd \"asd' dsa\"' dsa")
            fail("Should not be parsed")
        } catch(e: IllegalArgumentException) {}
    }

    @Test
    fun overlappingDoubleQuotes() {
        try {
            tokenize("asd 'asd\" dsa'\" dsa")
            fail("Should not be parsed")
        } catch(e: IllegalArgumentException) {}
    }
}