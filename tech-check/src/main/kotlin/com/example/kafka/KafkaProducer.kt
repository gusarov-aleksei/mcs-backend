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
    val order = Order(listOf(Item("product-1", "1", "1.20", "1.20")))
    val result = producer.send(OrderEvent.Create("customer-1", "100.10", order))
    println("cancelled = ${result.isCancelled} done = ${result.isDone}".wrapWithDate())
    println("result get ${result.get()}".wrapWithDate())
    producer.send(OrderEvent.Pay("order-1"))
    producer.send(OrderEvent.Complete("order-1"))
    producer.close()
    println("end Kafka producer".wrapWithDate())
}