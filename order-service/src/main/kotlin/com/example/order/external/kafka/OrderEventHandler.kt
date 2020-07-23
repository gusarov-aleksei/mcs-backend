package com.example.order.external.kafka

import com.example.order.model.OrderEvent
import com.example.order.service.OrderService
import org.apache.kafka.clients.consumer.ConsumerRecord

class OrderEventHandler(private val orderService: OrderService) {

    fun handle(record: ConsumerRecord<String, OrderEvent>) {
        orderService.processOrderEvent(record.value())
    }

}