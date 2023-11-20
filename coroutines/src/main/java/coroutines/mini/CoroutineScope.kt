package coroutines.mini

import kotlin.coroutines.CoroutineContext

interface CoroutineScope {
    val isActive: Boolean

    suspend fun <T> launch(
        context: CoroutineContext = Dispatchers.Unconfined,
        block: suspend () -> T
    ): Task<T>

    suspend fun <T> withContext(
        context: CoroutineContext,
        block: suspend () -> T
    ): T

    suspend fun cancel()

    suspend fun join()
}

internal class CoroutineScopeImpl(
    private val eventLoop: EventLoop,
) : CoroutineScope {
    private val tasks = linkedSetOf<Task<*>>()

    override val isActive: Boolean get() = !eventLoop.isQuited()

    override suspend fun <T> launch(
        context: CoroutineContext,
        block: suspend () -> T
    ): Task<T> {
        return eventLoop.queue.enqueue(context, block)
            .also { task ->
                tasks.add(task)
                task.invokeOnCompletion {
                    tasks.remove(task)
                }
            }
    }

    override suspend fun <T> withContext(
        context: CoroutineContext,
        block: suspend () -> T,
    ): T = launch(context, block).await()

    override suspend fun cancel() {
        eventLoop.quit()
        tasks.toTypedArray().forEach { it.cancel() }
    }

    override suspend fun join() {
        tasks.toTypedArray().forEach {
            try {
                it.join()
            } catch (_: TaskCancellationException) {
                // Task canceled
            }
        }
    }
}
