import coroutines.mini.awaitAll
import coroutines.mini.delay
import coroutines.mini.runBlocking
import kotlin.system.measureTimeMillis

fun main(): Unit = runBlocking {
    val tasks = listOf(
        launch {
            delay(1000)
            "Hello"
        },
        launch {
            delay(1500)
            "World"
        }
    )
    val elapsed = measureTimeMillis {
        println(tasks.awaitAll().joinToString())
    }
    check(elapsed <= 1600)
}