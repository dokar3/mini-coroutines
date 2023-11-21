package coroutines.mini

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/// Steal ideas from kotlinx's dispatcher implementations

internal val AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors()

interface CoroutineDispatcher : CoroutineContext.Element {
    fun dispatch(task: Runnable)

    fun shutdown()

    companion object : CoroutineContext.Key<CoroutineDispatcher>
}

object Dispatchers {
    val Default: CoroutineDispatcher = ThreadPoolDispatcher(
        pool = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS),
    )

    val IO: CoroutineDispatcher = ThreadPoolDispatcher(
        pool = Executors.newFixedThreadPool(64.coerceAtLeast(AVAILABLE_PROCESSORS))
    )

    val Unconfined: CoroutineDispatcher = UnconfinedDispatcher

    fun shutdown() {
        Default.shutdown()
        IO.shutdown()
    }
}

class ThreadPoolDispatcher(
    private val pool: ExecutorService,
) : CoroutineDispatcher,
    CoroutineContext.Element,
    CoroutineContext.Key<ThreadPoolDispatcher> {
    override fun dispatch(task: Runnable) {
        pool.submit(task)
    }

    override fun shutdown() {
        pool.shutdown()
    }

    override val key: CoroutineContext.Key<*> = CoroutineDispatcher
}

private object UnconfinedDispatcher : CoroutineDispatcher {
    override fun dispatch(task: Runnable) {
        task.run()
    }

    override fun shutdown() {}

    override val key: CoroutineContext.Key<*> = CoroutineDispatcher
}