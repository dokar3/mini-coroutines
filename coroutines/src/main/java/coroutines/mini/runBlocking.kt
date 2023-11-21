package coroutines.mini

import kotlin.coroutines.CoroutineContext

/**
 * Run coroutines and block the current thread until all tasks are completed.
 */
fun <T> runBlocking(
    context: CoroutineContext = Dispatchers.Unconfined,
    block: suspend CoroutineScope.() -> T
): T {
    val queue = TaskQueue()
    val eventLoop = EventLoop(queue = queue)
    val task = queue.enqueue(context = context) {
        val scope = CoroutineScopeImpl(eventLoop = eventLoop)
        val result = scope.block()
        scope.join()
        eventLoop.quit()
        result
    }
    eventLoop.loop()
    Dispatchers.shutdown()
    return task.blockingGet()
}
