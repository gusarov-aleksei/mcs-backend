package com.example.order

import com.example.order.dao.OrderDao
import com.example.order.external.kafka.KafkaClient
import com.example.order.external.kafka.OrderEventHandler
import com.example.order.external.kafka.consumerConfig
import com.example.order.model.OrderEvent
import com.example.order.service.OrderService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

@Volatile
var stop = false

fun main() {
    val ds = HikariDataSource(HikariConfig(hikariProps()))
    val dao = OrderDao(ds)
    val service = OrderService(dao)
    val orderEventHandler = OrderEventHandler(service)
    val consumer = KafkaClient<OrderEvent>(consumerConfig(),listOf("OrderEventTopic"))

    Runtime.getRuntime().addShutdownHook(Thread( Runnable { stop = true; consumer.close() }))

    while(!stop) {
        consumer.consume {
            orderEventHandler.handle(it)
        }
        println("ping")
    }
}