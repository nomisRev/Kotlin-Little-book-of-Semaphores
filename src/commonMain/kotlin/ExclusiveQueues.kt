import arrow.fx.coroutines.parTraverse
import arrow.fx.coroutines.parZip
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore

suspend fun ExclusiveQueues(total: Int) {
  val leaderQueue = Channel<Unit>(1000)
  val followerQueue = Channel<Unit>(1000)
  val leaderSignal = Semaphore(0)
  val followerSignal = Semaphore(0)

  val leaders = Leaders(total, leaderQueue, leaderSignal)
  val followers = Followers(total, followerQueue, followerSignal)
  val operator = Operator(total, leaderQueue, followerQueue, leaderSignal, followerSignal)

  parZip(
    { leaders.use() },
    { followers.use() },
    { operator.use() }
  ) { _, _, _ -> }
}

class Operator(
  val number: Int,
  val leaderQueue: Channel<Unit>,
  val followerQueue: Channel<Unit>,
  val leaderSignal: Semaphore,
  val followerSignal: Semaphore,
  console: Console = Console.Default,
  temporal: Temporal = Temporal.Default
) : Console by console, Temporal by temporal {

  suspend fun use() {
    (0..number).parTraverse { one() }
  }

  private suspend fun one(): Unit {
    parZip({ leaderQueue.receive() }, { followerQueue.receive() }) { _, _ -> }
    parZip({ leaderSignal.release() }, { followerSignal.release() }) { _, _ -> }
  }
}

class Leaders(
  val number: Int,
  val channel: Channel<Unit>,
  val signal: Semaphore,
  console: Console = Console.Default,
  temporal: Temporal = Temporal.Default
) : Console by console, Temporal by temporal {

  suspend fun use(): Unit {
    (0..number).parTraverse { one() }
  }

  private suspend fun one(): Unit {
    putStr("A leader joined")
    sleep(number.milliseconds)
    channel.send(Unit)
    signal.acquire()
    putStr("The leader started dancing")
  }
}

class Followers(
  val number: Int,
  val channel: Channel<Unit>,
  val signal: Semaphore,
  console: Console = Console.Default,
  temporal: Temporal = Temporal.Default
) : Console by console, Temporal by temporal {

  suspend fun use(): Unit {
    (0..number).parTraverse { one() }
  }

  private suspend fun one(): Unit {
    putStr("A follower joined")
    channel.send(Unit)
    signal.acquire()
    putStr("The follower started dancing")
  }
}
