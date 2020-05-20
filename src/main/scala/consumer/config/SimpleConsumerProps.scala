package consumer.config

import org.apache.kafka.common.serialization.StringDeserializer

trait SimpleConsumerProps {

  implicit val valueDeseriallizer: String = classOf[StringDeserializer].getName

}
