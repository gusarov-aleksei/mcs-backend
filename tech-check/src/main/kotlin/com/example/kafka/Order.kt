package com.example.kafka

import com.fasterxml.jackson.annotation.*

data class Item(
        val productId: String,
        val quantity: String,
        val price: String,
        val total: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, visible = true)
sealed class OrderEvent {
    data class Create(val customerId: String, val totalToPay: String, val items: Collection<Item>) : OrderEvent()
    data class Pay(val orderId:String) : OrderEvent()
    data class Complete(val orderId:String) : OrderEvent()
}