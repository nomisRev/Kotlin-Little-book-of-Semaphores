import arrow.fx.coroutines.parZip
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore

/**
 * Possibly the simplest use for a semaphore is signaling, which means that one thread send a
 * signal to another thread to indicate that something has happened.
 *
 * Signaling makes it possible to guarantee that a section of code in one thread will run before a
 * section of code in another thread; in other words, it solves the serialization problem.
 */
suspend fun signaling() {
  suspend fun threadA(signal: Semaphore): Unit {
    println("Happen before b1")
    delay(1.seconds)
    signal.release()
  }

  suspend fun threadB(signal: Semaphore): Unit {
    signal.acquire()
    println("Happen after a1")
  }

  val s = Semaphore(0)
  parZip({ threadB(s) }, { threadA(s) }) { _, _ -> }
}
