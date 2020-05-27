package utils.exception

import java.lang.RuntimeException

class MusicContextInitializationException(val msg: String): RuntimeException(msg)