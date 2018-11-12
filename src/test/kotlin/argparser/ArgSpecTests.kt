package argparser

import argparser.spec.ArgResult
import argparser.spec.ArgSpec

fun <T : ArgResult> runSpec(spec: ArgSpec<T>, args: List<String>): T {
    var ret: T? = null
    for(arg in args) {
        ret = spec.parse(arg)
        if(ret != null) {
            break
        }
    }
    return ret ?: spec.default()
}