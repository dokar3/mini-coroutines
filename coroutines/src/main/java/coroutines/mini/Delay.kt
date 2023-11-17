package coroutines.mini

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

/// Based on the original Delay implementation

interface Delay : CoroutineContext.Element {
    fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: Continuation<Unit>,
    )

    companion object : CoroutineContext.Key<Delay>
}

suspend fun delay(timeMillis: Long) {
    if (timeMillis <= 0) return
    return suspendCoroutine { cont ->
        if (timeMillis < Long.MAX_VALUE) {
            cont.context.delay.scheduleResumeAfterDelay(timeMillis, cont)
        }
    }
}

private val CoroutineContext.delay: Delay get() = this[Delay] ?: DefaultDelay

private object DefaultDelay : Delay {
    override val key: CoroutineContext.Key<*> = Delay

    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: Continuation<Unit>
    ) {
        Thread.sleep(timeMillis)
    }
}

internal class TaskDelay(
    private val task: TaskImpl<*>,
) : Delay {
    override val key: CoroutineContext.Key<*> = Delay

    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: Continuation<Unit>,
    ) {
        if (timeMillis <= 0 || task.isCompleted) return
        task.onReschedule?.invoke(timeMillis)
    }
}