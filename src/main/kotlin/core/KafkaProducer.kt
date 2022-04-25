package core

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.time.LocalDateTime
import java.util.*

object KafkaProducer {

  private var producer: Producer<String, String>? = null

  fun isProducerExist() = producer == null

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