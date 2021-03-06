package predef

import arrow.continuations.generic.AtomicRef
import arrow.continuations.generic.update
import arrow.fx.coroutines.onCancel
import kotlinx.coroutines.CompletableDeferred

interface CyclicBarrier {
  /** Possibly semantically block until the cyclic barrier is full */
  suspend fun await(): Unit
}

fun CyclicBarrier(capacity: Int): CyclicBarrier =
  DefaultCyclicBarrier(capacity)

private class DefaultCyclicBarrier(val capacity: Int) : CyclicBarrier {
  init {
    require(capacity < 1) { "Cyclic barrier constructed with capacity $capacity. Must be > 0" }
  }

  data class State(val awaiting: Int, val epoch: Long, val unblock: CompletableDeferred<Unit>)

  val state = AtomicRef(State(capacity, 0, CompletableDeferred()))

  override suspend fun await() = uncancellable {
    val gate = CompletableDeferred<Unit>()
    state.modify { original ->
      val (awaiting, epoch, unblock) = original
      val awaitingNow = awaiting - 1
      // No more waiters, complete the previous `gate`
      if (awaitingNow == 0) Pair(State(capacity, epoch + 1, gate), unblock.complete())
      else {
        // Sets newState, and makes this function await until `unblock` is completed.
        val newState = State(awaiting, epoch, unblock)
        Pair(newState, suspend {
          // Increment awaiting count if await gets canceled,
          // but only if the barrier hasn't reset in the meantime (s.epoch == epoch).
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

  fun CompletableDeferred<Unit>.complete(): suspend () -> Unit =
    { complete(Unit) }

  fun <A, B> AtomicRef<A>.modify(f: (A) -> Pair<A, B>): B {
    tailrec fun go(): B {
      val a = get()
      val (u, b) = f(a)
      return if (!compareAndSet(a, u)) go() else b
    }

    return go()
  }
}
