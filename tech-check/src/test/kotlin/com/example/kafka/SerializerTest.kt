package com.example.kafka

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*

/**
 * Tool for bytes converting: https://onlineutf8tools.com/convert-bytes-to-utf8
 */
class OrderSerializerTest : StringSpec({
    "serialize method returns array of bytes in case of OrderEvent.Pay" {
        val bytes = OrderSerializer().serialize("any", OrderEvent.Pay("order-id-1"))
        // byte representation of JSON: {"@c":".OrderEvent$Pay","orderId":"order-id-1"}
        val orderEventPayInBytes = listOf<Byte>(123, 34, 64, 99, 34, 58, 34, 46, 79, 114, 100, 101, 114, 69, 118, 101, 110, 116,
                36, 80, 97, 121, 34, 44, 34, 111, 114, 100, 101, 114, 73, 100, 34, 58, 34, 111, 114, 100, 101, 114, 45,
                105, 100, 45, 49, 34, 125).toByteArray()

        bytes shouldBe orderEventPayInBytes
        println(OrderSerializer().serialize("any", OrderEvent.Pay("order-id-1")))
    }

    "deserialize method returns object of OrderEvent.Pay type" {
        // JSON: {"@c":".OrderEvent$Pay","orderId":"order-id-1"}
        val orderEventPayInBytes = listOf<Byte>(123, 34, 64, 99, 34, 58, 34, 46, 79, 114, 100, 101, 114, 69, 118, 101, 110, 116,
                36, 80, 97, 121, 34, 44, 34, 111, 114, 100, 101, 114, 73, 100, 34, 58, 34, 111, 114, 100, 101, 114, 45,
                105, 100, 45, 49, 34, 125).toByteArray()

        val pay = OrderDeserializer().deserialize("any", orderEventPayInBytes)
        pay shouldBe OrderEvent.Pay("order-id-1")
    }

    "serialize method returns array of bytes in case of OrderEvent.Create" {
        val createOrder = OrderEvent.Create(
                "order-id-1",
                "10.10",
                listOf(Item("product-id-2", "2", "3.50", "7.00"))
        )
        val bytes = OrderSerializer().serialize("any", createOrder)
        // byte representation of JSON: {"@c":".OrderEvent$Create","customerId":"order-id-1","totalToPay":"10.10","items":[{"productId":"product-id-2","quantity":"2,"price":"3.50","total":"7.00"}]}
        val orderEventCreateInBytes = listOf<Byte>(123,34,64,99,34,58,34,46,79,114,100,101,114,69,118,101,110,116,36,67,114,101,97,116,101,34,44,34,99,117,115,116,111,109,101,114,73,100,34,58,34,111,114,100,101,114,45,105,100,45,49,34,44,34,116,111,116,97,108,84,111,80,97,121,34,58,34,49,48,46,49,48,34,44,34,105,116,101,109,115,34,58,91,123,34,112,114,111,100,117,99,116,73,100,34,58,34,112,114,111,100,117,99,116,45,105,100,45,50,34,44,34,113,117,97,110,116,105,116,121,34,58,34,50,34,44,34,112,114,105,99,101,34,58,34,51,46,53,48,34,44,34,116,111,116,97,108,34,58,34,55,46,48,48,34,125,93,125).toByteArray()

        bytes shouldBe orderEventCreateInBytes
    }

    "deserialize method returns object of OrderEvent.Create type" {
        // byte representation of JSON: {"@c":".OrderEvent$Create","customerId":"order-id-1","totalToPay":"10.10","items":[{"productId":"product-id-2","quantity":"2,"price":"3.50","total":"7.00"}]}
        val orderEventCreateInBytes = listOf<Byte>(123,34,64,99,34,58,34,46,79,114,100,101,114,69,118,101,110,116,36,67,114,101,97,116,101,34,44,34,99,117,115,116,111,109,101,114,73,100,34,58,34,111,114,100,101,114,45,105,100,45,49,34,44,34,116,111,116,97,108,84,111,80,97,121,34,58,34,49,48,46,49,48,34,44,34,105,116,101,109,115,34,58,91,123,34,112,114,111,100,117,99,116,73,100,34,58,34,112,114,111,100,117,99,116,45,105,100,45,50,34,44,34,113,117,97,110,116,105,116,121,34,58,34,50,34,44,34,112,114,105,99,101,34,58,34,51,46,53,48,34,44,34,116,111,116,97,108,34,58,34,55,46,48,48,34,125,93,125).toByteArray()

        val pay = OrderDeserializer().deserialize("any", orderEventCreateInBytes)
        pay shouldBe OrderEvent.Create(
                "order-id-1",
                "10.10",
                listOf(Item("product-id-2", "2", "3.50", "7.00"))
        )
    }
})