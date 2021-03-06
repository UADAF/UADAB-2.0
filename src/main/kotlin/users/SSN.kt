package users

import kotlin.random.Random
import kotlin.random.nextInt


class SSN internal constructor(val intVal: Int) {

    private fun getSSNString(redacted: Boolean): String {
        val ssns = intVal.toString()
        val zeros = 9 - ssns.length
        val ssn = CharArray(9) { i -> if (i < zeros) '0' else ssns[i - zeros]}
        return if (redacted) {
            String.format("XXX-XX-%c%c%c%c", ssn[5], ssn[6], ssn[7], ssn[8])
        } else {
            String.format("%c%c%c-%c%c-%c%c%c%c", ssn[0], ssn[1], ssn[2], ssn[3], ssn[4], ssn[5], ssn[6], ssn[7], ssn[8])
        }
    }

    val ssnString by lazy { getSSNString(false) }
    val redactedSSNString by lazy { getSSNString(true) }

    override fun toString() = ssnString

    companion object {
        internal fun randomSSN() = SSN(Random.nextInt(0..999_99_9999))
    }
}
