package core

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

private fun createConsumer(): Consumer<String, String> {
  val props = Properties()
  props["bootstrap.servers"] = "localhost:9092"
  props["group.id"] = "hello-world"
  props["key.deserializer"] = StringDeserializer::class.java
  props["value.deserializer"] = StringDeserializer::class.java
  return KafkaConsumer(props)
}

fun main() {
  val consumer = createConsumer()
  consumer.subscribe(listOf("Topic1"))

  while (true) {
    val records = consumer.poll(Duration.ofSeconds(1))
    println("Consumed ${records.count()} records")

    records.iterator().forEach {
      val message = it.value()
      println("Message: $message")
    }
  }
}