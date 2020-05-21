package consumer

import java.time.Duration
import java.util.Collections

import common.topics.KafkaTopics
import consumer.config.{ConsumerWithStreamingProps, KafkaConsumerConfig}
import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.jdk.CollectionConverters._

object ConsumerWithStreaming
    extends App
    with KafkaConsumerConfig
    with ConsumerWithStreamingProps
    with KafkaTopics {

  setProperties

  val consumer = new KafkaConsumer[String, String](properties)

  consumer.subscribe(Collections.singleton(OUTPUT_TOPIC))

  while (true) {
    val records = consumer.poll(Duration.ofMillis(100)).asScala

    for (record <- records) {
      println(s"uid: ${record.key}, count: ${record.value}")
    }

  }

}
