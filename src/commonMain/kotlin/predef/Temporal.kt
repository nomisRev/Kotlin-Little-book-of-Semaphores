package predef

import kotlin.time.Duration
import kotlinx.coroutines.delay

interface Temporal {
  suspend fun sleep(duration: Duration): Unit

  companion object {
    val Default = object : Temporal {
      override suspend fun sleep(duration: Duration) = delay(duration)
    }
  }
}
