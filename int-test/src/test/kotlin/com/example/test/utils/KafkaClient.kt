package com.example.test.utils

import java.util.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import java.io.Closeable
import java.time.Duration

fun orderConsumerProperties() : Properties {
    val props = Properties()
    props.setProperty("bootstrap.servers", "localhost:9092")
    props.setProperty("group.id", "test")
    props.setProperty("enable.auto.commit", "true")
    props.setProperty("auto.commit.interval.ms", "1000")
    props.setProperty("reconnect.backoff.ms", "3000")
    props.setProperty("reconnect.backoff.max.ms", "4000")
    props.setProperty("request.timeout.ms", "2000")
    props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.setProperty("value.deserializer", "com.example.test.OrderDeserializer")
    //to consume topic from the beginning
    props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name.toLowerCase())
    return props;
}

class KafkaClient<T>(props: Properties, topics: List<String>) : Closeable  {
    private val consumer: KafkaConsumer<String, T> = KafkaConsumer(props)
    private val duration = Duration.ofMillis(1000)
    init {
        consumer.subscribe(topics)
    }

    fun consume(handler: (orderEvent: ConsumerRecord<String, T>) -> Unit) {
        //consumer.use {
        println("Inside consumer")
            consumer.poll(duration).forEach {
                handler(it)
            }
        //}
    }

    override fun close() {
        consumer.close()
    }
}