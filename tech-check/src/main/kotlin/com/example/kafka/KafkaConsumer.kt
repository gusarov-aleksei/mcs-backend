package com.example.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy

import java.time.Duration
import java.util.Properties

class OrdersConsumer(private val topics: List<String>) {
    private val consumer: KafkaConsumer<String, OrderEvent>
    private val duration = Duration.ofMillis(3000)
    init {
        val props = Properties()
        props.setProperty("bootstrap.servers", "localhost:9092")
        props.setProperty("group.id", "test")
        props.setProperty("enable.auto.commit", "true")
        props.setProperty("auto.commit.interval.ms", "1000")
        props.setProperty("reconnect.backoff.ms", "3000")
        props.setProperty("reconnect.backoff.max.ms", "4000")
        props.setProperty("request.timeout.ms", "2000")
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.setProperty("value.deserializer", "com.example.kafka.OrderDeserializer")
        //to consume topic from the beginning
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name.toLowerCase())
        consumer = KafkaConsumer(props)
        consumer.subscribe(topics)
    }

    /**
     * Consume topic record one by one
     */
    fun consume(handler: (orderEvent: ConsumerRecord<String, OrderEvent>) -> Unit) {
        println("consume".wrapWithDate())
        consumer.poll(duration).forEach {
            handler(it)
        }
    }

}

fun consumeOrderEvent(record: ConsumerRecord<String, OrderEvent>) {
    println("offset = ${record.offset()}, key = ${record.key()}, value = ${record.value()}")
}

fun main(args:Array<String>){
    println("start Kafka consumer".wrapWithDate())
    val ordersConsumer = OrdersConsumer(listOf("OrderEventTopic"))
    while(true) {
        ordersConsumer.consume {
            consumeOrderEvent(it)
        }
        println("ping".wrapWithDate())
    }
    //println("end Kafka consumer".wrapWithDate())
}
