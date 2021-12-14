import arrow.fx.coroutines.parSequence
import arrow.fx.coroutines.parTraverse
import predef.CyclicBarrier

/** By using CyclicBarrier we can implement Reusable Barrier problem and preloaded turnstile at the same time. */
suspend fun CyclicBarrier(total: Int) {
  val barrier1 = CyclicBarrier(total)
  val barrier2 = CyclicBarrier(total)
  (0..total).parTraverse {
    CTask(it, total, barrier1, barrier2).use()
  }
}

class CTask(
  val number: Int,
  val repeat: Int,
  val barrier1: CyclicBarrier,
  val barrier2: CyclicBarrier,
  console: Console = Console.Default
) : Console by console {

  suspend fun use (): Unit =
    repeat(repeat) { one() }

  suspend fun one(): Unit {
    putStr("Task $number started")
    barrier1.await()
    putStr("Task $number after critical point")
    barrier2.await()
  }

}
