# Mini Coroutines

A minimal Kotlin/JVM Coroutines runtime.

### How minimal?

```
======= .kt Line Counter =======
          Lines: 478
Non-empty lines: 392
```

### What's present?

- `runBlocking {}` Run coroutines in a coroutine scope. It starts an event loop internally
- `launch {}` Launch a new coroutine. It returns a task
- `await()`, `cancel()` Control launched coroutine tasks
- `withContext {}` Switch to another coroutine context. It just calls `launch` and `await`s the result
- `delay()` Delay the coroutine execution. It reschedules the current task to the event queue
- `Dispatchers` Decide the thread(pool) to run coroutines (`Main` dispatcher is not supported yet)

### What's missing?

- Flows and channels
- Exception handler
- Parent-children job support
- Concurrency primitives (Mutex, Semaphore, etc.)
- Start strategies
- Testing mechanisms
- ...

### Can I use it in my project?

**NO**. It's for learning purposes, it lacks features, can be buggy, has no tests, and isn't optimized. So don't use it for other purposes.

# Examples

### Launch 100K Coroutines

```kotlin
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
        tasks.forEach { it.join() }
    }
    check(done.get() == count)
    println("$count Coroutines finished in ${millis}ms")
}
```

### Await all

```kotlin
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
```

### Context (dispatcher) switching

```kotlin
fun main(): Unit = runBlocking {
    val thread = Thread.currentThread()
    withContext(Dispatchers.Default) {
        check(thread != Thread.currentThread())
    }
}
```
