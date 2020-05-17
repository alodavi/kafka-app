name := "kafka-app"

version := "0.1"

scalaVersion := "2.13.2"

// https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.5.0"

// https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams-scala
libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.5.0"
