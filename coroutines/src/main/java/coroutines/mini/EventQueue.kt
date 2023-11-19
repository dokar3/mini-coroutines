package coroutines.mini

import java.util.LinkedList
import java.util.PriorityQueue
import java.util.concurrent.TimeUnit
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

    private val queue = LinkedList<Event>()
    private val queueLock = Any()

    // PriorityQueue + read write lock seems faster PriorityBlockingQueue or others
    private val delayQueue = PriorityQueue(eventComparator)
    private val delayQueueLock = ReentrantReadWriteLock()

    private var stopped = false

    fun poll(): Event? {
        while (!stopped) {
            val event = pollEvent()
            if (event != null) {
                return event
            }
            val delayedEvent = delayedEvent()
            if (delayedEvent == null) {
                // Wait for new events
                park()
                continue
            } else {
                val now = System.nanoTime()
                val delay = delayedEvent.time - now
                val elapsed = parkNanos(delay)
                if (elapsed >= delay) {
                    // Done, remove the event
                    removedDelayed(delayedEvent)
                    return delayedEvent
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
        val event = Event(task = task)
        synchronized(queueLock) {
            queue.add(event)
            unpark()
        }
        return task
    }

    private fun pollEvent(): Event? = synchronized(queueLock) {
        return queue.removeFirstOrNull()
    }

    internal fun <T> enqueueDelayed(task: TaskImpl<T>, delayMillis: Long) {
        val event = Event(
            task = task,
            time = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delayMillis),
        )
        delayQueueLock.write {
            delayQueue.add(event)
            unpark()
        }
    }

    private fun delayedEvent(): Event? = delayQueueLock.read {
        delayQueue.firstOrNull()
    }

    private fun removedDelayed(event: Event) = delayQueueLock.write {
        delayQueue.remove(event)
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
