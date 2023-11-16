package coroutines.mini

internal class Event(
    val task: TaskImpl<*>,
    val time: Long = System.nanoTime(),
)