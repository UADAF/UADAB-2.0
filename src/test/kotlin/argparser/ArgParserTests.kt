package argparser

import argparser.spec.*
import org.junit.Test
import org.junit.Assert.*

class ArgParserTests {


    @Test
    fun simple() {
        val parser = ArgParser().apply {
            register(PlainArgSpec("asd"))
        }
        val args = listOf("dsa", "sda", "asd")
        val res = parser.parse(args)
        assertEquals("dsa", (res["asd"] as PlainArgResult).value)
    }

    @Test
    fun multiple() {
        val parser = ArgParser().apply {
            register(PlainArgSpec("asd"))
            register(PlainArgSpec("dsa"))
        }
        val args = listOf("dsa", "sda", "asd")
        val res = parser.parse(args)
        assertEquals("dsa", (res["asd"] as PlainArgResult).value)
        assertEquals("sda", (res["dsa"] as PlainArgResult).value)
    }

    @Test
    fun missingArg() {
        val parser = ArgParser().apply {
            register(PlainArgSpec("asd"))
            register(PlainArgSpec("dsa"))
        }
        val args = listOf("dsa")
        val res = parser.parse(args)
        assertEquals("dsa", (res["asd"] as PlainArgResult).value)
        assertNull((res["dsa"] as PlainArgResult).value)
    }

    @Test
    fun shortFlagPresent() {
        val parser = ArgParser().apply {
            register(FlagArgSpec("asd", shortname = 'a'))
        }
        val args = listOf("sda", "dsa", "-a")
        val res = parser.parse(args)
        assertTrue((res["asd"] as FlagArgResult).present)
    }

    @Test
    fun shortFlagAbsent() {
        val parser = ArgParser().apply {
            register(FlagArgSpec("asd", shortname = 'a'))
        }
        val args = listOf("sda", "dsa", "-d")
        val res = parser.parse(args)
        assertFalse((res["asd"] as FlagArgResult).present)
    }

    @Test
    fun shortMultiFlagPresent() {
        val parser = ArgParser().apply {
            register(FlagArgSpec("asd", shortname = 'a'))
            register(FlagArgSpec("dsa", shortname = 'd'))
        }
        val args = listOf("sda", "dsa", "-ad")
        val res = parser.parse(args)
        assertTrue((res["asd"] as FlagArgResult).present)
        assertTrue((res["dsa"] as FlagArgResult).present)
    }

    @Test
    fun shortMultiFlagPresentAbsent() {
        val parser = ArgParser().apply {
            register(FlagArgSpec("asd", shortname = 'a'))
            register(FlagArgSpec("dsa", shortname = 'd'))
        }
        val args = listOf("sda", "dsa", "-ab")
        val res = parser.parse(args)
        assertTrue((res["asd"] as FlagArgResult).present)
        assertFalse((res["dsa"] as FlagArgResult).present)
    }


    @Test
    fun shortMultiFlagAbsent() {
        val parser = ArgParser().apply {
            register(FlagArgSpec("asd", shortname = 'a'))
            register(FlagArgSpec("dsa", shortname = 'd'))
        }
        val args = listOf("sda", "dsa", "-b")
        val res = parser.parse(args)
        assertFalse((res["asd"] as FlagArgResult).present)
        assertFalse((res["dsa"] as FlagArgResult).present)
    }

    @Test
    fun delegate() {
        val parser = ArgParser().apply {
            register(PlainArgSpec("asd"))
        }
        val args = listOf("dsa", "sda", "asd")
        val asd by parser.delegate<PlainArgResult>("asd")
        parser.parse(args)
        assertEquals("dsa", asd.value)
    }

    @Test
    fun mixed() {
        val parser = ArgParser().apply {
            register(FlagArgSpec("asd"))
            register(PlainArgSpec("dsa"))
        }
        val args = listOf("dsa", "sda", "--asd")
        val asd by parser.delegate<FlagArgResult>("asd")
        val res = parser.parse(args)
        assertTrue(asd.present)
        assertEquals("dsa", (res["dsa"] as PlainArgResult).value)
    }

    @Test
    fun dsl() {
        val manual = ArgParser().apply {
            register(FlagArgSpec("asd", "fgh", 'a'))
            register(PlainArgSpec("dsa"))
            register(PlainArgSpec("sda"))
            register(RangeArgSpec("ads"))
            register(ValueArgSpec("sad", "das"))
        }
        val dsl = argparser {
            flag("asd", "fgh", 'a')
            plain("dsa")
            plain("sda")
            range("ads")
            value("sad", "das")
        }
        assertEquals(manual, dsl)
    }

}