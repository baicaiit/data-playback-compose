package core

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.time.LocalDateTime
import java.util.*

object KafkaProducer {

  private var producer: Producer<String, String>? = null

  fun isProducerNotExist() = producer == null

  fun createProducer(server: String) {
    val props = Properties()
    props["bootstrap.servers"] = server
    props["key.serializer"] = StringSerializer::class.java
    props["value.serializer"] = StringSerializer::class.java
    producer = KafkaProducer(props)
  }

  fun sendMsg(topic: String, key: LocalDateTime, msg: List<String>) {
    producer?.send(ProducerRecord(topic, key.toString(), msg.toString()))
  }

}


private fun createProducer(): Producer<String, String> {
  val props = Properties()
  props["bootstrap.servers"] = "localhost:9092"
  props["key.serializer"] = StringSerializer::class.java
  props["value.serializer"] = StringSerializer::class.java
  return KafkaProducer(props)
}

fun main() {
  val producer = createProducer()
  for (i in 0..4) {
    val future = producer.send(ProducerRecord("Topic1", i.toString(), "Hello world $i"))
    future.get()
  }
}