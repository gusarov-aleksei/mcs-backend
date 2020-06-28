package com.example.test

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import org.apache.kafka.common.serialization.Deserializer


data class Product(val id: String, val name: String, val price: Double, val description: String)

@Serializer(forClass=Product::class)
object ProductSerializer

@Serializer(forClass=CartItem::class)
object CartItemSerializer

@Serializable
data class CartItem(val productId: String, val name: String, var price: Double, val quantity:Int, val total:Double )

data class Cart(val id: String, var customerId: String?, var total: Double, val cartItems: List<CartItem>)

@Serializer(forClass=Cart::class) /*Cart::javaClass*/
object CartSerializer

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

class OrderDeserializer: Deserializer<OrderEvent> {

    private val jsonMapper = jacksonObjectMapper()

    override fun deserialize(topic: String?, data: ByteArray?): OrderEvent? {
        return data?.let {
            jsonMapper.readValue(it, OrderEvent::class.java);
        }
    }
}
