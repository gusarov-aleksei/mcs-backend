package com.example.order.external.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.util.*

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