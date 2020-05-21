# Kafka App
[![Build Status](https://travis-ci.com/alodavi/kafka-app.svg?branch=master)](https://travis-ci.com/alodavi/kafka-app)

## Architecture

This app is composed of a `Producer` (to be run from the command line) and two `Consumers`. The `SimpleConsumer` just 
fetches data from the Kafka broker and returns the sum of the unique ids (that can be used e.g. to calculate how many 
unique users are using the app at a given point in time). The `ConsumerWithStream` app is connected to the `StreamingApp`
and calculates how many times the single ids appear in the stream (that could be used e.g. to recognize the "power users", 
the users that use the app the most).

While the `SimpleConsumer` subscribe to the same topic to which the `Producer` is sending the data - `streams-app-input` - 
the `ConsumerWithStream` subscribes to the output topic to which the `StreamingApp` is sending the data - `streams-uid-count-output`.

Hereby is the structure of the project: 

```$xslt
src
├── main
│   └── scala
│       ├── common
│       │   └── topics
│       │       └── KafkaTopics.scala
│       ├── consumer
│       │   ├── config
│       │   │   ├── ConsumerWithStreamingProps.scala
│       │   │   ├── KafkaConsumerConfig.scala
│       │   │   └── SimpleConsumerProps.scala
│       │   ├── ConsumerWithStreaming.scala
│       │   └── SimpleConsumer.scala
│       └── streaming
│           ├── config
│           │   └── KafkaStreamConfig.scala
│           └── StreamingApp.scala
└── test
    └── scala

```

## Instructions

- Install kafka
- Run `zookeeper` from command line:

```$xslt
zookeeper-server-start.sh /opt/kafka/config/zookeeper.properties
```

- Run `kafka` from command line:

```$xslt
kafka-server-start.sh /opt/kafka/config/server.properties
```

- Prepare an input topic for the kafka producer

```$xslt
> kafka-topics.sh --create \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 1 \
    --topic streams-app-input
```

- Prepare an output topic for the kafka consumer

```$xslt
> kafka-topics.sh --create \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 1 \
    --topic streams-uid-count-output \
    --config cleanup.policy=compact
```

- Send data to the producer
```$xslt
 zcat stream.jsonl.gz | kafka-console-producer.sh --bootstrap-server localhost:9092 --topic streams-app-input
```

- Start the `SimpleConsumer` application

### Starting the Kafka Streams app

- Start the `StreamingApp` application

- Inspect the the output of the application or run the `ConsumerWithStreaming` application.
```$xslt
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic streams-uid-count-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```

## Performance Tuning

When it comes to performance testing we have to consider 2 important metrics:

- latency: how long it takes to process one event
- throughput: how many events arrive within a specific amount of time

Kafka has high throughput: if you have a 3 node cluster you should be able to handle 100 K events per second.
For what concerns latency it should take 1 to 2 ms to process one single event.

### Tuning Kafka Producers

Kafka uses an asynchronous publish/subscribe model. When your producer calls the `send() ` command, the result returned 
is a future. The future provides methods to let you check the status of the information in process. When the batch is 
ready, the producer sends it to the broker. The Kafka broker waits for an event, receives the result, and then responds 
that the transaction is complete.

When you use `Producer.send()`, you fill up buffers on the producer. When a buffer is full, the producer sends the
buffer to the Kafka broker and begins to refill the buffer.

Two parameters are particularly important for latency and throughput: batch size and linger time.

### Tuning Brokers

Topics are divided into partitions. Each partition has a leader. Most partitions are written into leaders with multiple 
replicas. When the leaders are not balanced properly, one might be overworked, compared to others.

### Tuning Kafka Consumer

Consumers can create throughput issues on the other side of the pipeline. The maximum number of consumers for a topic 
is equal to the number of partitions. You need enough partitions to handle all the consumers needed to keep up with the 
producers.

Consumers in the same consumer group split the partitions among them. Adding more consumers to a group can enhance 
performance. 

Using this as a [reference](https://engineering.linkedin.com/kafka/benchmarking-apache-kafka-2-million-writes-second-three-cheap-machines)
we see that the test with a single consumer scores pretty well with `940,521 records/sec` and `89.7 MB/sec`

```$xslt
bin/kafka-consumer-perf-test.sh --topic streams-app-input --bootstrap-server localhost:9092 --messages 100000 --threads 1
```

| MB/sec        | Messages/sec   |
| :-------------: |:-------------:|
| 199.0840      | 100394 |

Increasing the number of threads to 3 doesn't affect performance:
```$xslt
bin/kafka-consumer-perf-test.sh --topic streams-app-input --bootstrap-server localhost:9092 --messages 100000 --threads 3
```
| MB/sec        | Messages/sec   |
| :-------------: |:-------------:|
| 199.0840      | 100394 |
