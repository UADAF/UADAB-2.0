package argparser.spec

class PlainArgResult(val value: String?) : ArgResult() {
    override val type: String = "plain"
}

class PlainArgSpec(name: String) : ArgSpec<PlainArgResult>(name) {
    override val type: String = "plain"

    override fun parse(arg: String) = PlainArgResult(arg)

    override fun default() = PlainArgResult(null)
}