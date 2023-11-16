package coroutines.mini

import java.util.concurrent.TimeUnit

internal class EventLoop(internal val queue: EventQueue) {
    private var quited = false

    fun loop() {
        while (!quited) {
            // Poll events
            val event = try {
                queue.poll()
            } catch (e: EventQueue.PollingStoppedException) {
                quited = true
                break
            }
            val task = event.task
            // Listen for delay calls
            task.onReschedule = { rescheduleDelayedTask(task, it) }
            task.invokeOnCompletion {
                if (it.isFailure) {
                    // Quit the loop if any task failed
                    queue.stopPolling()
                    // Maybe this is the wrong position to throw it?
                    throw it.exceptionOrNull()!!
                }
            }
            if (!quited && !task.isCompleted) {
                // Start the task
                task.start()
            }
        }
    }

    private fun rescheduleDelayedTask(task: TaskImpl<*>, delayMillis: Long) {
        if (delayMillis <= 0) return
        val now = System.nanoTime()
        val delayedEvent = Event(
            task = task,
            time = now + TimeUnit.MILLISECONDS.toNanos(delayMillis),
        )
        queue.enqueue(delayedEvent)
    }

    fun quit() {
        quited = true
        queue.stopPolling()
    }

    fun isQuited() = quited
}
