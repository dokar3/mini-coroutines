import coroutines.mini.delay
import coroutines.mini.runBlocking

fun main(): Unit = runBlocking {
    launch {
        println("Started task 1")
        delay(2000)
        println("Task 1 finished")
    }
    launch {
        cancel()
    }
}