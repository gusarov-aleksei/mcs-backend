package com.example.order.external.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.util.*

// creating the consumer using map config
fun consumerConfig() : Properties {
    var config = Properties()
    config["bootstrap.servers"] = "localhost:9092"
    config["key.deserializer"] = "org.apache.kafka.common.serialization.StringDeserializer"
    config["value.deserializer"] = "com.example.order.model.OrderDeserializer"
    config["group.id"] = "my_group"
    config["auto.offset.reset"] = "earliest"
    config["enable.auto.commit"] = "false"
    return config
}

class KafkaClient<T>(config: Properties, topics: List<String>) : AutoCloseable {
    private val duration = Duration.ofMillis(3000)
    private val consumer = KafkaConsumer<String, T>(config)
    init {
        consumer.subscribe(topics)
    }

    /**
     * Consume topic record one by one
     */
    fun consume(handler: (orderEvent: ConsumerRecord<String, T>) -> Unit) {
        consumer.poll(duration).forEach {
            handler(it)
        }
    }

    override fun close() {
        consumer.close()
    }
}