package streaming

import java.util.Properties
import java.util.concurrent.CountDownLatch

import com.google.gson.JsonParser
import common.topics.KafkaTopics
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.{Produced, ValueMapper}
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder}
import streaming.config.KafkaStreamConfig

import scala.jdk.CollectionConverters._

object StreamingApp extends App with KafkaTopics with KafkaStreamConfig {

  def createUUIDCountStream(builder: StreamsBuilder): Unit = {
    val source = builder.stream(INPUT_TOPIC)

    def parseJson(jsonString: String) = {
      val uid = JsonParser
        .parseString(jsonString)
        .getAsJsonObject
        .get("uid")
        .getAsString()
      Array("uid", uid)
    }

    val counts = source
      .flatMapValues[String] {
        new ValueMapper[String, java.lang.Iterable[java.lang.String]]() {
          override def apply(
              value: String): java.lang.Iterable[java.lang.String] = {
            parseJson(value).toIterable.asJava
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

  val builder = new StreamsBuilder()
  createUUIDCountStream(builder)
  val streams = new KafkaStreams(builder.build, getStreamsConfig())
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
