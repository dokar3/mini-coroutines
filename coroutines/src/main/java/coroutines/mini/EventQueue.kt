package coroutines.mini

import java.util.PriorityQueue
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.coroutines.CoroutineContext

internal class EventQueue {
    // Used to unpark the polling
    private var parkedThread: Thread? = null

    private val eventComparator = Comparator<Event> { a, b ->
        a.time.toString().compareTo(b.time.toString())
    }

    // PriorityQueue + read write lock seems faster PriorityBlockingQueue or others
    private var queue = PriorityQueue(eventComparator)
    private val queueLock = ReentrantReadWriteLock()

    private var stopped = false

    fun poll(): Event? {
        while (!stopped) {
            val event = nearestEvent()
            if (event == null) {
                // Wait for new events
                park()
                continue
            } else {
                val now = System.nanoTime()
                val delay = event.time - now
                val elapsed = parkNanos(delay)
                if (elapsed >= delay) {
                    // Done, remove the event
                    removeEvent(event)
                    return event
                }
            }
        }
        return null
    }

    fun <T> enqueue(
        context: CoroutineContext,
        block: suspend () -> T,
    ): Task<T> {
        val task = TaskImpl(block = block, context = context)
        enqueue(Event(task = task))
        return task
    }

    fun enqueue(event: Event) = queueLock.write {
        queue.add(event)
        unpark()
    }

    private fun nearestEvent(): Event? = queueLock.read {
        return queue.firstOrNull()
    }

    private fun removeEvent(event: Event) = queueLock.write {
        queue.remove(event)
    }

    private fun park() {
        parkedThread = Thread.currentThread()
        LockSupport.park()
    }

    private fun parkNanos(nanos: Long): Long {
        if (nanos <= 0L) return 0
        parkedThread = Thread.currentThread()
        val start = System.nanoTime()
        LockSupport.parkNanos(nanos)
        return System.nanoTime() - start
    }

    private fun unpark() {
        if (parkedThread != null) {
            LockSupport.unpark(parkedThread)
        }
    }

    internal fun stopPolling() {
        stopped = true
        unpark()
    }
}
