package coroutines.mini

import java.util.concurrent.ConcurrentLinkedQueue
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
    private val tasks = ConcurrentLinkedQueue<Task<*>>()

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
                    onTaskCompleted(it)
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
        for (task in tasks) {
            try {
                task.join()
            } catch (_: TaskCancellationException) {
                // Task canceled
            }
        }
    }

    private fun onTaskCompleted(error: Throwable?) {
        error ?: return
        eventLoop.quit()
        throw error
    }
}
