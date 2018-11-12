package argparser

import argparser.spec.PlainArgSpec
import org.junit.Assert
import org.junit.Test

class PlainArgsTests {
    @Test
    fun plainWithArgs() {
        val spec = PlainArgSpec("asd")
        val args = listOf("dsa", "asd", "sda")
        val res = runSpec(spec, args)
        Assert.assertEquals(res.value, "dsa")
    }

    @Test
    fun plainWithEmpty() {
        val spec = PlainArgSpec("asd")
        val args = emptyList<String>()
        val res = runSpec(spec, args)
        Assert.assertNull(res.value)
    }
}