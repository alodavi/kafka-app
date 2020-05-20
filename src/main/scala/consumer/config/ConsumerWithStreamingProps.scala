package consumer.config

import org.apache.kafka.common.serialization.LongDeserializer

trait ConsumerWithStreamingProps {

  implicit val valueDeseriallizer: String = classOf[LongDeserializer].getName

}
