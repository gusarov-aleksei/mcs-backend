package com.example.kafka

import org.apache.kafka.common.serialization.Serializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Deserializer

class OrderSerializer : Serializer<OrderEvent> {

    private val jsonMapper = jacksonObjectMapper()

    override fun serialize(topic: String?, data: OrderEvent?): ByteArray? {
        //data ?: return
        return data?.let {
            jsonMapper.writeValueAsBytes(it)
        }
    }
}

class OrderDeserializer: Deserializer<OrderEvent> {

    private val jsonMapper = jacksonObjectMapper()

    override fun deserialize(topic: String?, data: ByteArray?): OrderEvent? {
        return data?.let {
            jsonMapper.readValue(it, OrderEvent::class.java);
        }
    }
}

fun main() {
    val payEvent = OrderEvent.Pay("order-10")
    val payEventBytes = jacksonObjectMapper().writeValueAsBytes(payEvent)
    val deserialized1 = jacksonObjectMapper().readValue(payEventBytes, OrderEvent.Pay::class.java)
    println("deserialized = $deserialized1")

    val order = Order(listOf(Item("1", "1", "1.20", "1.20")))
    val createOrder = OrderEvent.Create("customer 1", "100.10", order)
    val bytes = jacksonObjectMapper().writeValueAsBytes(createOrder)
    val deserialized  = jacksonObjectMapper().readValue(bytes, OrderEvent::class.java)
    println("deserializedOrderEvent = $deserialized")
}