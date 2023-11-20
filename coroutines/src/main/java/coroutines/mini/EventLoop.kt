package coroutines.mini

internal class EventLoop(internal val queue: EventQueue) {
    private var quited = false

    fun loop() {
        while (!quited) {
            val event = queue.poll() ?: break
            runTask(event.task)
        }
        quited = true
    }

    private fun runTask(task: TaskImpl<*>) {
        val onCompletion: OnCompletion<*> = callback@{
            val error = it.exceptionOrNull() ?: return@callback
            // Quit the loop if any task failed
            queue.stopPolling()
            // Maybe this is the wrong position to throw it?
            throw error
        }
        // Listen for delay calls
        task.onReschedule = {
            task.removeOnCompletion(onCompletion)
            queue.enqueueDelayed(task, it)
        }
        task.invokeOnCompletion(onCompletion)
        if (!quited && !task.isCompleted) {
            // Start the task
            task.start()
        }
    }

    fun quit() {
        quited = true
        queue.stopPolling()
    }

    fun isQuited() = quited
}
