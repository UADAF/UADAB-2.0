package utils

import uadamusic.MusicContext
import java.lang.IllegalStateException

class MusicContextState {

    private var _context: MusicContext? = null

    var exception: Throwable? = null
        private set

    val context: MusicContext
        get() = _context
            ?: throw exception
            ?: throw IllegalStateException("MusicContextState is in undefined state when _context is null and exception is null too")

    val isAvailable: Boolean
        get() = _context != null

    constructor(exc: Throwable) {
        _context = null
        exception = exc
    }

    constructor(ctx: MusicContext) {
        _context = ctx
        exception = null
    }

}