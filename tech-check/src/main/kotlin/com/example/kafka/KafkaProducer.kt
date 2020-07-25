package com.example.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.Future

class OrderProducer(private val topic : String) {
    private val producer: Producer<String, OrderEvent>
    init {
        //org.apache.kafka.common.serialization.StringSerializer
        val props = Properties()
        props.load(FileInputStream(ClassLoader.getSystemClassLoader().getResource("producer.properties").file))
        producer = KafkaProducer<String, OrderEvent>(props)
    }

    fun send(message : OrderEvent): Future<RecordMetadata> =
            producer.send(ProducerRecord(topic, message))

    fun close() = producer.close()
}

fun main(args: Array<String>) {
    println("start Kafka producer".wrapWithDate())
    val producer = OrderProducer("OrderEventTopic")
    val result = producer.send(OrderEvent.Create("1bec138e-689e-485b-be91-4f05d7a13a55", "100.10", listOf(Item("1", "1", "1.20", "1.20"))))
    println("cancelled = ${result.isCancelled} done = ${result.isDone}".wrapWithDate())
    println("result get ${result.get()}".wrapWithDate())
    producer.send(OrderEvent.Pay("1"))
    producer.send(OrderEvent.Complete("1"))
    producer.close()
    println("end Kafka producer".wrapWithDate())
}