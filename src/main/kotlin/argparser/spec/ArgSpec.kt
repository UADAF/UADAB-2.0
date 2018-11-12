package argparser.spec

abstract class ArgResult {

    abstract val type: String

}

abstract class ArgSpec<out T : ArgResult>(val name: String) {

    abstract val type: String

    abstract fun parse(arg: String): T?

    abstract fun default(): T
}
