package utils

import java.lang.IllegalArgumentException

fun extractCount(a: List<String>): Pair<Int, List<String>> {
    val countPos = a.indexOf("*")
    if (a.lastIndexOf("*") != countPos) {
        throw IllegalArgumentException("'*' cannot be specified multiple times")
    }
    return if (countPos < 0) {
        1 to a
    } else {
        if (countPos < a.lastIndex) {
            val count =
                a[countPos + 1].toIntOrNull() ?: throw IllegalArgumentException("'*' must be followed by a number")
            count to a.filterIndexed { i, _ -> i !in countPos..countPos + 1 }
        } else {
            throw IllegalArgumentException("'*' must be followed by a number")
        }
    }
}