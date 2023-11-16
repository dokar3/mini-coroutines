import coroutines.mini.delay
import coroutines.mini.runBlocking

fun main(): Unit = runBlocking {
    val task1 = launch {
        println("Started task 1")
        delay(2000)
        println("Task 1 finished")
    }
    launch {
        delay(1000)
        task1.cancel()
    }
}