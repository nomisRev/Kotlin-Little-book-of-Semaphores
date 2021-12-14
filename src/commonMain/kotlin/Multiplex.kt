import arrow.fx.coroutines.parTraverse
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.sync.Semaphore

/** Generalize the previous solution so that it allows multiple threads to run in the critical
 * section at the same time, but it enforces an upper limit on the number of concurrent threads. In
 * other words, no more than n threads can run in the critical section at the same time. This
 * pattern is called a multiplex.
 */
suspend fun multiplex(): Unit {
  val s = Semaphore(2)
  (1..6).parTraverse { count ->
    PreciousResource("R$count", s).use()
  }
}

class PreciousResource(
  val name: String,
  val s: Semaphore,
  console: Console = Console.Default,
  temporal: Temporal = Temporal.Default
) : Console by console, Temporal by temporal {

  suspend fun use(): Unit {
    val x = s.availablePermits
    putStr("$name >> Availability: $x")
    s.acquire()
    val y = s.availablePermits
    putStr("$name >> Started | Availability: $y")
    sleep(3.seconds)
    s.release()
    val z = s.availablePermits
    putStr("$name >> Done | Availability: $z")
  }
}
