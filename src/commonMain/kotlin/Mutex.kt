import arrow.fx.coroutines.parTraverse
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import predef.debug

/** Often the code that needs to be protected is called the critical section, I suppose because it
 * is critically important to prevent concurrent access.
 */
suspend fun mutex(): Unit {
  var count = 0

  suspend fun increase(s: Semaphore): Unit =
    s.withPermit {
      delay(1.milliseconds)
      count += 1 // critical section
    }

  suspend fun increaseBy(s: Semaphore, repeat: Int) =
    (0..repeat).parTraverse { increase(s) }

  val s = Semaphore(1)
  increaseBy(s, 1000)
  count.debug()
}
