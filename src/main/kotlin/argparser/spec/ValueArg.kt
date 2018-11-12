package argparser.spec

class ValueArgResult(val name: String?, val value: String?) : ArgResult() {
    override val type: String = "value"
}

class ValueArgSpec(name: String, val argname: String? = null) : ArgSpec<ValueArgResult>(name) {

    override val type: String = "value"

    override fun parse(arg: String): ValueArgResult? {
        val n = argname ?: name
        return if(arg.startsWith("--$n")) {
            val value = arg.removePrefix("--$n").removePrefix("=")
            ValueArgResult(n, if(value.isNotEmpty()) value else null)
        } else {
            null
        }
    }

    override fun default() = ValueArgResult(null, null)
}