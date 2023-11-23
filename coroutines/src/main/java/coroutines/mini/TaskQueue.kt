package coroutines.mini

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport
import kotlin.coroutines.CoroutineContext

internal class DelayedTask(
    val task: Task<*>,
    val time: Long = System.nanoTime(),
)

internal class TaskQueue {
    // Used to unpark the polling
    private var parkedThread: Thread? = null

    private val queue = ConcurrentLinkedQueue<Task<*>>()
    private val delayedTaskComparator = Comparator<DelayedTask> { a, b ->
        a.time.toString().compareTo(b.time.toString())
    }
    private val delayQueue = ConcurrentSkipListSet(delayedTaskComparator)

    private var stopped = false

    fun poll(): Task<*>? {
        while (!stopped) {
            val event = pollTask()
            if (event != null) {
                return event
            }
            val delayedEvent = pollDelayed()
            if (stopped) return null
            if (delayedEvent == null) {
                park()
                continue
            }
            val now = System.nanoTime()
            val delay = delayedEvent.time - now
            val elapsed = parkNanos(delay)
            if (elapsed >= delay) {
                // Done, remove the event
                removeDelayed(delayedEvent)
                return delayedEvent.task
            }
        }
        return null
    }

    fun <T> enqueue(
        context: CoroutineContext,
        block: suspend () -> T,
    ): Task<T> {
        val task = TaskImpl(
            block = block,
            context = context,
            onReschedule = ::enqueueDelayed,
        )
        queue.add(task)
        unpark()
        return task
    }

    private fun pollTask(): Task<*>? {
        return queue.poll()
    }

    private fun <T> enqueueDelayed(task: Task<T>, delayMillis: Long) {
        val delayedTask = DelayedTask(
            task = task,
            time = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delayMillis),
        )
        delayQueue.add(delayedTask)
        unpark()
    }

    private fun pollDelayed(): DelayedTask? = delayQueue.firstOrNull()

    private fun removeDelayed(delayedTask: DelayedTask) = delayQueue.remove(delayedTask)

    private fun park() = parkOperation { LockSupport.park() }

    private fun parkNanos(nanos: Long): Long {
        if (nanos <= 0L) return 0
        val start = System.nanoTime()
        parkOperation { LockSupport.parkNanos(nanos) }
        return System.nanoTime() - start
    }

    private inline fun parkOperation(block: () -> Unit) {
        parkedThread = Thread.currentThread()
        block()
        parkedThread = null
    }

    private fun unpark() = parkedThread?.let { LockSupport.unpark(it) }

    internal fun stopPolling() {
        stopped = true
        unpark()
    }
}
