package com.example.order.external.kafka

import com.example.order.model.OrderEvent
import com.example.order.service.OrderService
import com.example.order.test.CREATE_EVENT_SAMPLE
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderEventHandlerTest {

    @Test
    fun `should handle consumer record of order event topic`() {
        val service = mockk<OrderService>()
        every { service.processOrderEvent(CREATE_EVENT_SAMPLE) } returns Unit
        val handler  = OrderEventHandler(service)

        val record = ConsumerRecord<String, OrderEvent>("OrderEventTopic",0,0,null, CREATE_EVENT_SAMPLE)

        handler.handle(record)

        verify(exactly = 1) { service.processOrderEvent(CREATE_EVENT_SAMPLE) }

    }

    @Test
    fun `should throw IllegalStateException when null value in consumer record`() {
        val service = mockk<OrderService>()
        val handler  = OrderEventHandler(service)
        val record = ConsumerRecord<String, OrderEvent>("OrderEventTopic",0,0,null, null)

        val exception = assertThrows<IllegalStateException>("should throw IllegalStateException"){
            handler.handle(record)
        }
        assertEquals("record.value() must not be null", exception.message)

    }
    /*
    ConsumerRecord(topic = OrderEventTopic, partition = 0, leaderEpoch = 0, offset = 0, CreateTime = 1595367717149, serialized key size = -1, serialized value size = 174, headers = RecordHeaders(headers = [], isReadOnly = false), key = null, value = Create(customerId=1bec138e-689e-485b-be91-4f05d7a13a55, totalToPay=100.1, items=[Item(productId=1, quantity=1, price=1.2, total=1.2)]))
    */
}