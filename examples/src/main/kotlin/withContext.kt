import coroutines.mini.Dispatchers
import coroutines.mini.runBlocking

fun main(): Unit = runBlocking {
    val thread = Thread.currentThread()
    withContext(Dispatchers.Default) {
        check(thread != Thread.currentThread())
    }
}