package com.example.order.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Deserializer


data class Item(
        val productId: Int,
        val quantity: Short,
        val price: Double,
        val total: Double
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, visible = true)
sealed class OrderEvent {
    data class Create(val customerId: String, val totalToPay: Double, val items: Collection<Item>) : OrderEvent()
    data class Pay(val orderId : Int) : OrderEvent()
    data class Complete(val orderId : Int) : OrderEvent()
}

class OrderDeserializer: Deserializer<OrderEvent> {

    private val jsonMapper = jacksonObjectMapper()

    override fun deserialize(topic: String?, data: ByteArray?): OrderEvent? {
        return data?.let {
            jsonMapper.readValue(it, OrderEvent::class.java);
        }
    }
}

enum class Status {
    CREATED, PAYED, COMPLETED
}

data class Order(
        val orderId: Int,
        val customerId: String,
        val totalToPay: Double,
        val status: Status = Status.CREATED,
        val details: MutableCollection<Item>
)
//OrderDetails