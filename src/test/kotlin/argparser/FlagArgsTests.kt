package argparser

import argparser.spec.FlagArgSpec
import org.junit.Assert.*
import org.junit.Test

class FlagArgsTests {

    @Test
    fun flagWithPresent() {
        val spec = FlagArgSpec("asd")
        val args = listOf("asd", "dsa", "--asd")
        val res = runSpec(spec, args)
        assertTrue(res.present)
    }

    @Test
    fun flagWithAbsent() {
        val spec = FlagArgSpec("asd")
        val args = listOf("asd", "dsa", "-asd")
        val res = runSpec(spec, args)
        assertFalse(res.present)
    }

    @Test
    fun flagWithFlagnameAndPresent() {
        val spec = FlagArgSpec("asd", "dsa")
        val args = listOf("asd", "dsa", "--dsa")
        val res = runSpec(spec, args)
        assertTrue(res.present)
    }

    @Test
    fun flagWithFlagnameAndAbsent() {
        val spec = FlagArgSpec("asd", "dsa")
        val args = listOf("asd", "dsa", "-dsa")
        val res = runSpec(spec, args)
        assertFalse(res.present)
    }

    @Test
    fun flagWithShortnameAndPresent() {
        val spec = FlagArgSpec("asd", shortname = 'a')
        val args = listOf("asd", "dsa", "-a")
        val res = runSpec(spec, args)
        assertTrue(res.present)
    }

    @Test
    fun flagWithShortnameAndAbsent() {
        val spec = FlagArgSpec("asd", shortname = 'a')
        val args = listOf("asd", "dsa", "-s")
        val res = runSpec(spec, args)
        assertFalse(res.present)
    }

    @Test
    fun flagWithFlagnameAndShortnameAndPresentFlagname() {
        val spec = FlagArgSpec("asd", "dsa", 'a')
        val args = listOf("asd", "dsa", "--dsa")
        val res = runSpec(spec, args)
        assertTrue(res.present)
    }

    @Test
    fun flagWithFlagnameAndShortnameAndPresentShortname() {
        val spec = FlagArgSpec("asd", "dsa", 'a')
        val args = listOf("asd", "dsa", "-a")
        val res = runSpec(spec, args)
        assertTrue(res.present)
    }

    @Test
    fun flagWithFlagnameAndShortnameAndAbsent() {
        val spec = FlagArgSpec("asd", "dsa", 'a')
        val args = listOf("asd", "dsa", "-s", "--sda")
        val res = runSpec(spec, args)
        assertFalse(res.present)
    }

}