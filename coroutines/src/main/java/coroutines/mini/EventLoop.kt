package coroutines.mini

internal class EventLoop(internal val queue: TaskQueue) {
    private var quited = false

    fun loop() {
        while (!quited) {
            val task = queue.poll() as? TaskImpl<*> ?: break
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
