import coroutines.mini.delay
import coroutines.mini.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

fun main(): Unit = runBlocking {
    val count = 100_000
    val done = AtomicInteger(0)
    val millis = measureTimeMillis {
        val tasks = List(count) {
            launch {
                delay(3000)
                done.incrementAndGet()
            }
        }
        tasks.forEach { it.await() }
    }
    check(done.get() == count)
    println("$count coroutines finished in ${millis}ms")
}