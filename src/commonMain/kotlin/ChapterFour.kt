import arrow.fx.coroutines.parZip
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import predef.Console
import predef.Temporal

/**
 * In one common pattern, some threads are producers and some are consumers. Producers create
 * items of some kind and add them to a data structure; consumers remove the items and process
 * them.
 *
 * There are several synchronization constraints that we need to enforce to make this system work
 * correctly:
 *
 *   - While an item is being added to or removed from the buffer, the buffer is in an inconsistent
 *     state. Therefore, threads must have exclusive access to the buffer.
 *
 * If a consumer thread arrives while the buffer is empty, it blocks until a producers adds a new
 * item.
 */
suspend fun consumerProducer(
  total: Int = 10,
  console: Console = Console.Default,
  temporal: Temporal = Temporal.Default
) {
  suspend fun produce(events: Channel<Unit>): Unit {
    console.putStr("producing")
    temporal.sleep(1.milliseconds)
    events.send(Unit)
    console.putStr("done producing")
  }

  suspend fun consume(events: Channel<Unit>): Unit {
    console.putStr("consuming")
    temporal.sleep(500.milliseconds)
    events.receive()
    console.putStr("done consuming")
  }

  val channel = Channel<Unit>(3)
  parZip({
    repeat(total) { produce(channel) }
  }, {
    repeat(total) { consume(channel) }
  }) { _, _ -> }
}
