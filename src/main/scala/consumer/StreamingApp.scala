package consumer

import java.util.Properties
import java.util.concurrent.CountDownLatch
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}
import org.apache.kafka.streams.kstream.{Produced, ValueMapper}

import scala.jdk.CollectionConverters._

object StreamingApp extends App {

  val INPUT_TOPIC = "streams-app-input"
  val OUTPUT_TOPIC = "streams-uid-count-output"

  def getStreamsConfig: Properties = {
    val props = new Properties
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0)
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
              Serdes.String.getClass.getName)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
              Serdes.String.getClass.getName)
    // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
    // Note: To re-run the demo, you need to use the offset reset tool:
    // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props
  }

  def createWordCountStream(builder: StreamsBuilder): Unit = {
    val source = builder.stream(INPUT_TOPIC)

    def processData(string: String) = {
      string
        .slice(1, string.length - 1)
        .split(",")
        .filter(s => s.startsWith("\"uid\""))
    }

    val counts = source
      .flatMapValues[String] {
        new ValueMapper[String, java.lang.Iterable[java.lang.String]]() {
          override def apply(
              value: String): java.lang.Iterable[java.lang.String] = {
            processData(value).toIterable.asJava
          }
        }
      }
      .groupBy { (key: String, uid: String) =>
        uid
      }
      .count

    counts.toStream
      .to(OUTPUT_TOPIC, Produced.`with`(Serdes.String(), Serdes.Long))
  }

  val props = getStreamsConfig
  val builder = new StreamsBuilder()
  createWordCountStream(builder)
  val streams = new KafkaStreams(builder.build, props)
  val latch = new CountDownLatch(1)
  // attach shutdown handler to catch control-c
  Runtime.getRuntime.addShutdownHook(
    new Thread("streams-uid-count-shutdown-hook") {
      override def run(): Unit = {
        streams.close
        latch.countDown()
      }
    })
  try {
    streams.start
    latch.await()
  } catch {
    case e: Throwable =>
      System.exit(1)
  }
  System.exit(0)

}
