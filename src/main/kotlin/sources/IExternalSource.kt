package sources

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

interface IExternalSource<T> {

    suspend fun startLoading()

    fun clear()

    suspend fun reload() {
        clear()
        startLoading()
    }

    fun getAsync(): Deferred<T>
}

suspend fun <T> IExternalSource<T>.get(): T =
    getAsync().await()


fun <T> IExternalSource<T>.getIfLoaded(): T? {
    val ac = getAsync()
    return if(ac.isCompleted) {
        runBlocking { ac.await() }
    } else {
        null
    }
}