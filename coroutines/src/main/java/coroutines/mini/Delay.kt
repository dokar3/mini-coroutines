package coroutines.mini

import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

/// Based on the original Delay implementation

interface Delay {
    fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: Continuation<Unit>,
    )
}

suspend fun delay(timeMillis: Long) {
    if (timeMillis <= 0) return
    return suspendCoroutine { cont ->
        if (timeMillis < Long.MAX_VALUE) {
            cont.context.delay.scheduleResumeAfterDelay(timeMillis, cont)
        }
    }
}

private val CoroutineContext.delay: Delay
    get() = this[ContinuationInterceptor] as? Delay ?: DefaultDelay

private object DefaultDelay : Delay {
    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: Continuation<Unit>
    ) {
        Thread.sleep(timeMillis)
    }
}

internal class TaskDelay(
    private val task: TaskImpl<*>,
) : Delay,
    ContinuationInterceptor,
    CoroutineContext.Element,
    CoroutineContext.Key<TaskDelay> {
    override val key: CoroutineContext.Key<*> = this

    override fun <T> interceptContinuation(
        continuation: Continuation<T>
    ): Continuation<T> {
        return continuation
    }

    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: Continuation<Unit>,
    ) {
        if (timeMillis <= 0 || task.isCompleted) return
        task.onReschedule?.invoke(timeMillis)
    }
}