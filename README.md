# Kafka App

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

- Start the `StreamingApp` application

- Inspect the the output of the application or run the `Consumer` application.
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
