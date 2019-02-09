package sources

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

abstract class BasicExternalSource<T> : IExternalSource<T> {

    private var data: CompletableDeferred<T> = CompletableDeferred()

    override suspend fun startLoading() {
        try {
            data.complete(load())
        } catch (e: Throwable) {
            data.completeExceptionally(e)
        }
        println("${javaClass.simpleName} loaded")
    }

    abstract suspend fun load(): T

    override fun clear() {
        data = CompletableDeferred()
    }

    override fun getAsync(): Deferred<T> {
        return data
    }

}