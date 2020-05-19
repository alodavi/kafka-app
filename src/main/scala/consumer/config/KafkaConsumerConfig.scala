package consumer.config

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{
  LongDeserializer,
  StringDeserializer
}

trait KafkaConsumerConfig {

  def setProperties(): Properties => Properties = { properties =>
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                           "127.0.0.1:9092")
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                           classOf[StringDeserializer].getName)
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                           classOf[LongDeserializer].getName)
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "streaming-app")
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    properties
  }

}
