package utils.getters

class Wrapper<T: Any> {

    private var single: T? = null
    private var multi: List<T> = emptyList()
    lateinit var state: WrapperState
        private set


    enum class WrapperState {
        SINGLE,
        MULTI,
        NONE
    }

    constructor() {
        none()
    }

    constructor(single: T?) {
        if (single == null) none()
        else single(single)
    }

    constructor(multi: List<T>?) {
        when (multi?.size) {
            null, 0 -> none()
            1 -> single(multi[0])
            else -> multi(multi)
        }
    }

    fun getSingle(): T {
        if(state != WrapperState.SINGLE) throw IllegalStateException("Excepted to be in state ${WrapperState.SINGLE}, but actually in $state")
        return single!!
    }

    fun getMulti(): List<T> {
        if(state != WrapperState.MULTI) throw IllegalStateException("Excepted to be in state ${WrapperState.MULTI}, but actually in $state")
        return multi
    }

    private fun single(e: T) {
        single = e
        state = WrapperState.SINGLE
    }

    private fun multi(e: List<T>) {
        multi = e
        state = WrapperState.MULTI
    }

    private fun none() {
        state = WrapperState.NONE
    }


}