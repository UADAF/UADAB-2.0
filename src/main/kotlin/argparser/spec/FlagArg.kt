package argparser.spec

class FlagArgResult(val present: Boolean) : ArgResult() {
    override val type: String = "flag"
}

class FlagArgSpec(name: String, val flagname: String? = null, val shortname: Char? = null) : ArgSpec<FlagArgResult>(name) {

    override val type: String = "flag"

    override fun parse(arg: String): FlagArgResult? {
        val n = flagname ?: name
        return if(arg == "--$n" || (shortname != null && arg == "-$shortname")) {
            FlagArgResult(true)
        } else {
            null
        }
    }

    override fun default() = FlagArgResult(false)

}