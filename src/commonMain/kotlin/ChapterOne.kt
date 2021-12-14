import arrow.fx.coroutines.Atomic
import arrow.fx.coroutines.parZip
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import predef.debug

suspend fun mutaulExclusion() {
  suspend fun thread(count: Atomic<Int>): Unit =
    repeat(100) { count.update(Int::inc) }

  val count = Atomic(0)
  parZip({ thread(count) }, { thread(count) }) { _,_ -> }
  val result = count.get()
  println(result)
}

suspend fun nonDeterminism() {
  suspend fun yes(): String = "yes".debug()
  suspend fun no(): String = "no".debug()
  parZip({ yes() }, { no() }) { _,_ -> }
}

suspend fun serializationWithMessages() {
  suspend fun alice(call: CompletableDeferred<Unit>) {
    delay(1.seconds)
      "Alice eats breakfast".debug()
      "Work".debug()
      "Alice eats lunch".debug()
      call.complete(Unit)
  }

  suspend fun bob(call: Deferred<Unit>) {
    "Bob eats breakfast".debug()
    call.await()
    "Bob eats lunch".debug()
  }

  val call = CompletableDeferred<Unit>()
  parZip({ alice(call) }, { bob(call) }) { _,_ -> }
}
