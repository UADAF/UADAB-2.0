package argparser

import argparser.spec.RangeArgSpec
import org.junit.Test
import org.junit.Assert.*
class RangeArgsTests {


    @Test
    fun rangeWithBoth() {
        val spec = RangeArgSpec("asd")
        val args = listOf("asd", "dsa", "1:10")
        val res = runSpec(spec, args)
        assertEquals(1, res.from)
        assertEquals(10, res.to)
    }

    @Test
    fun rangeWithFrom() {
        val spec = RangeArgSpec("asd")
        val args = listOf("asd", "dsa", "1:")
        val res = runSpec(spec, args)
        assertEquals(1, res.from)
        assertNull(res.to)
    }

    @Test
    fun rangeWithTo() {
        val spec = RangeArgSpec("asd")
        val args = listOf("asd", "dsa", ":10")
        val res = runSpec(spec, args)
        assertNull(res.from)
        assertEquals(10, res.to)
    }

    @Test
    fun rangeWithNone() {
        val spec = RangeArgSpec("asd")
        val args = listOf("asd", "dsa", ":")
        val res = runSpec(spec, args)
        assertNull(res.from)
        assertNull(res.to)
    }

    @Test
    fun rangeWithAbsent() {
        val spec = RangeArgSpec("asd")
        val args = listOf("asd", "dsa")
        val res = runSpec(spec, args)
        assertNull(res.from)
        assertNull(res.to)
    }

}