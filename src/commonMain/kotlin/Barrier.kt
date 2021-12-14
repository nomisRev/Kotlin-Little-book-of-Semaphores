import arrow.core.prependTo
import arrow.fx.coroutines.parSequence
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Semaphore
import predef.Console
import predef.Temporal

/**
 * Consider again the Rendezvous problem. A limitation of the solution we presented is that it does
 * not work with more than two threads. Generalize the rendezvous solution. Every thread should run
 * the following code:
 *
 * rendezvous critical point
 *
 * The synchronization requirement is that no thread executes critical point until after all
 * threads have executed rendezvous.
 *
 * Our solution is also called preloaded turnstile which allows all threads run concurrently
 */
suspend fun program(total: Int = 10): Unit {
  val mutex = CompletableDeferred<Unit>()
  val signal = Semaphore(0)
  val barrier = Barrier(total, mutex, signal)
  val tasks = List(total) { n -> bTask(n, mutex, signal) }
  (barrier::use prependTo tasks).parSequence()
}

fun bTask(
  number: Int,
  barrier: CompletableDeferred<Unit>,
  signal: Semaphore,
  console: Console = Console.Default
): suspend () -> Unit = suspend {
  console.putStr("Task $number started")
  signal.release()
  val y = signal.availablePermits
  console.putStr("Availability: $y")
  barrier.await()
  console.putStr("Task $number after critical point")
}

class Barrier(
  val tasks: Int,
  val mutex: CompletableDeferred<Unit>,
  val signal: Semaphore,
  console: Console = Console.Default,
  temporal: Temporal = Temporal.Default
) : Console by console, Temporal by temporal {

  suspend fun use(): Unit {
    putStr("Barrier started")
    // No acquireN available for KotlinX
    repeat(tasks) { signal.acquire() }
    mutex.complete(Unit)
    putStr("Barrier completed")
  }
}
