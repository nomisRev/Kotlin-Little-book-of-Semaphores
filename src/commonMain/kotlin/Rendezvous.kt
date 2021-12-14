import arrow.fx.coroutines.parZip
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import predef.debug

/** Redezvous: The idea is that two threads redezvous at a point of execution and neither is allowed
 * to proceed until both have arrived.
 *
 * We want two guarantee that a1 happen before b2 and b1 happen before a2
 */
suspend fun rendezvous() {
  suspend fun threadA(a: Semaphore, b: Semaphore): Unit {
    "statement a1".debug()
    a.release()
    b.acquire()
    "statement a2".debug()
  }

  suspend fun threadB(a: Semaphore, b: Semaphore): Unit {
    delay(1.seconds)
    "statement b1".debug()
    b.release()
    a.acquire()
    "statement b2".debug()
  }

  val a = Semaphore(0)
  val b = Semaphore(0)
  parZip({ threadB(a, b) }, { threadA(a, b) }) { _, _ -> }
}
