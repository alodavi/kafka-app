package consumer

import java.time.Duration
import java.util.{Collections, Properties}

import org.apache.kafka.clients.consumer.{
  ConsumerConfig,
  KafkaConsumer
}
import org.apache.kafka.common.serialization.{
  LongDeserializer,
  StringDeserializer
}

import scala.jdk.CollectionConverters._

object Consumer extends App {

  val properties = new Properties

  val topic = "streams-uid-count-output"

  properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                         "127.0.0.1:9092")
  properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                         classOf[StringDeserializer].getName)
  properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                         classOf[LongDeserializer].getName)
  properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "streaming-app")
  properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val consumer = new KafkaConsumer[String, String](properties)

  consumer.subscribe(Collections.singleton(topic))

  //TODO replace this while loop
  while (true) {
    val records = consumer.poll(Duration.ofMillis(100)).asScala

    for (record <- records) {
      println(s"uid: ${record.key.stripPrefix("\"uid\":")}, count: ${record.value}")
    }

  }

}
