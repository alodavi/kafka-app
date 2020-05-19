package consumer

import java.time.Duration
import java.util.{Collections, Properties}

import common.topics.KafkaTopics
import consumer.config.KafkaConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.jdk.CollectionConverters._

object Consumer extends App with KafkaConsumerConfig with KafkaTopics {

  val properties = new Properties

  setProperties()(properties)

  val consumer = new KafkaConsumer[String, String](properties)

  consumer.subscribe(Collections.singleton(OUTPUT_TOPIC))

  //TODO replace this while loop
  while (true) {
    val records = consumer.poll(Duration.ofMillis(100)).asScala

    for (record <- records) {
      println(s"uid: ${record.key}, count: ${record.value}")
    }

  }

}
