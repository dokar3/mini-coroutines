# Mini Coroutines

A minimal Kotlin/JVM Coroutines runtime.

### How minimal?

```
======= .kt Line Counter =======
          Lines: 475
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

**NO**. It's for learning purposes, it lacks features, can be buggy, has no tests, and isn't performant. So don't use it for other purposes.

# Examples

```kotlin
// Launch 100K Coroutines
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
```