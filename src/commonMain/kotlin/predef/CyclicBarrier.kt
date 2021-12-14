package predef

import arrow.fx.coroutines.Atomic
import arrow.fx.coroutines.onCancel
import kotlinx.coroutines.CompletableDeferred

interface CyclicBarrier {
  /** Possibly semantically block until the cyclic barrier is full */
  suspend fun await(): Unit
}

private data class State(val awaiting: Int, val epoch: Long, val unblock: CompletableDeferred<Unit>)

private fun CompletableDeferred<Unit>.complete(): suspend () -> Unit =
  { complete(Unit) }

suspend fun CyclicBarrier(capacity: Int): CyclicBarrier {
  require(capacity < 1) { "Cyclic barrier constructed with capacity $capacity. Must be > 0" }
  val state = Atomic(State(capacity, 0, CompletableDeferred()))
  return object : CyclicBarrier {
    override suspend fun await() = uncancellable {
      val gate = CompletableDeferred<Unit>()
      state.modify { original ->
        val (awaiting, epoch, unblock) = original
        val awaitingNow = awaiting - 1
        if (awaitingNow == 0) Pair(State(capacity, epoch + 1, gate), unblock.complete())
        else {
          val newState = State(awaiting, epoch, unblock)
          // reincrement count if this await gets canceled, but only if the barrier hasn't reset in the meantime
          Pair(newState, suspend {
            onCancel({ cancellable { unblock.await() } }) {
              state.update { s ->
                if (s.epoch == epoch) s.copy(awaiting = s.awaiting + 1)
                else s
              }
            }
          })
        }
      }.invoke()
    }
  }
}
