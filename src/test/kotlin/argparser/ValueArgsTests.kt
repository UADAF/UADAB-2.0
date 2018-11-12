package argparser

import argparser.spec.ValueArgSpec
import org.junit.Test
import org.junit.Assert.*
class ValueArgsTests {


    @Test
    fun valueWithPresent() {
        val spec = ValueArgSpec("asd")
        val args = listOf("asd", "dsa", "--asd=value")
        val res = runSpec(spec, args)
        assertEquals("asd", res.name)
        assertEquals("value", res.value)
    }

    @Test
    fun valueWithAbsentValue() {
        val spec = ValueArgSpec("asd")
        val args = listOf("asd", "dsa", "--asd")
        val res = runSpec(spec, args)
        assertEquals("asd", res.name)
        assertNull(res.value)
    }

    @Test
    fun valueWithAbsentValueWithEquals() {
        val spec = ValueArgSpec("asd")
        val args = listOf("asd", "dsa", "--asd=")
        val res = runSpec(spec, args)
        assertEquals("asd", res.name)
        assertNull(res.value)
    }

    @Test
    fun valueWithAbsent() {
        val spec = ValueArgSpec("asd")
        val args = listOf("asd", "dsa", "--as=value")
        val res = runSpec(spec, args)
        assertNull(res.name)
        assertNull(res.value)
    }

    @Test
    fun valueWithArgnameAndPresent() {
        val spec = ValueArgSpec("asd", "dsa")
        val args = listOf("asd", "dsa", "--dsa=value")
        val res = runSpec(spec, args)
        assertEquals("dsa", res.name)
        assertEquals("value", res.value)
    }

    @Test
    fun valueWithArgnameAndAbsentValue() {
        val spec = ValueArgSpec("asd", "dsa")
        val args = listOf("asd", "dsa", "--dsa")
        val res = runSpec(spec, args)
        assertEquals(res.name, "dsa")
        assertNull(res.value)
    }

    @Test
    fun valueWithArgnameAndAbsentValueWithEquals() {
        val spec = ValueArgSpec("asd", "dsa")
        val args = listOf("asd", "dsa", "--dsa=")
        val res = runSpec(spec, args)
        assertEquals(res.name, "dsa")
        assertNull(res.value)
    }

    @Test
    fun valueWithArgnameAndAbsent() {
        val spec = ValueArgSpec("asd", "dsa")
        val args = listOf("asd", "dsa", "--as=value")
        val res = runSpec(spec, args)
        assertNull(res.name)
        assertNull(res.value)
    }

}