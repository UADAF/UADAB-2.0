package argparser

import argparser.spec.ArgResult
import argparser.spec.ArgSpec
import kotlin.reflect.KProperty

class ArgParser {

    private val specs: MutableMap<String, ArgSpec<*>> = mutableMapOf()

    var results: MutableMap<String, ArgResult> = mutableMapOf()
        private set

    var leftover = mutableListOf<String>()

    private fun parse(arg: String, visitedSpecs: Set<String>): Pair<String, ArgResult>? {
        //specs.entries to allow non-local return
        specs.entries.forEach { (name, spec) ->
            if(spec.name !in visitedSpecs) {
                val res = spec.parse(arg)
                if (res != null) {
                    return name to res
                }
            }
        }
        return null
    }

    private fun parseMultiflag(arg: String) {
        val r = arg.substring(1)
            .map { c -> parse("-$c", results.keys) }
            .filterNotNull()
            .toList()
        if(r.isNotEmpty()) {
            r.forEach { (name, res) -> results[name] = res }
        } else {
            leftover.add(arg)
        }
    }

    private fun parseNormal(arg: String) {
        val r = parse(arg, results.keys)
        if(r != null) {
            val (name, res) = r
            results[name] = res
        } else {
            leftover.add(arg)
        }
    }

    fun parse(args: List<String>): Map<String, ArgResult> {
        args.forEach { arg ->
            if(arg.length > 1 && arg[0] == '-' && arg[1] != '-') {
                parseMultiflag(arg)
            } else {
                parseNormal(arg)
            }
        }
        specs
            .filter { (name, _) -> name !in results }
            .forEach{ (name, spec) -> results[name] = spec.default()}
        return results
    }

    inline fun with(args: List<String>, body: (Map<String, ArgResult>) -> Unit) {
        val res = parse(args)
        body(res)
        reset()
    }

    fun reset() {
        results = mutableMapOf()
        leftover = mutableListOf()
    }

    fun register(spec: ArgSpec<*>) {
        specs[spec.name] = spec
    }

    inline fun <reified T : ArgResult> delegate(name: String) = ArgDelegate { results[name] as T }

    fun leftoverDelegate() = ArgDelegate { leftover.toList() }

    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other !is ArgParser) {
            return false
        }
        if(specs.size != other.specs.size) {
            return false
        }
        return specs.values.all { it in other.specs.values }
    }

    override fun hashCode(): Int {
        return specs.hashCode() * 3
    }


}

class ArgDelegate<T>(val getter: () -> T) {

    operator fun getValue(thisRef: Any?, kProp: KProperty<*>) = getter()

}

