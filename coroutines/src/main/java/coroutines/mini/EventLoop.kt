package coroutines.mini

internal class EventLoop(internal val queue: EventQueue) {
    private var quited = false

    fun loop() {
        while (!quited) {
            // Poll events
            val event = queue.poll() ?: break
            val task = event.task
            // Listen for delay calls
            task.onReschedule = { queue.enqueueDelayed(task, it) }
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
        quited = true
    }

    fun quit() {
        quited = true
        queue.stopPolling()
    }

    fun isQuited() = quited
}
