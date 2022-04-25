package core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import utils.calculateDelayTime
import java.time.LocalDateTime

class KafkaTask(
  time: LocalDateTime,
  content: List<String>,
  private val host: String,
  private val port: Int,
  private val topic: String,
) : Task(time, content) {

  override suspend fun run(onfinish: (content: String) -> Unit) {
    withContext(Dispatchers.IO) {
      val baseTime = LocalDateTime.now()
      val delays = calculateDelayTime(baseTime, time)
      println("basetime $baseTime task $time delay $delays")
      delay(delays)
      if (KafkaProducer.isProducerExist()) {
        KafkaProducer.createProducer("$host:$port")
      }
      KafkaProducer.sendMsg(topic, time, content)
    }
  }

}