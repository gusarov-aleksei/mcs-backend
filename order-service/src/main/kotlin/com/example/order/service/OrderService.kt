package com.example.order.service

import com.example.order.dao.OrderRepository
import com.example.order.model.OrderEvent

class OrderService(private val orderDao: OrderRepository) {
    fun processOrderEvent(event : OrderEvent) = when(event) {
        is OrderEvent.Create -> {
            println(event)
            orderDao.createOrder(event)
            //send notification that order successfully created
        }
        is OrderEvent.Pay -> {
            println(event)
        }
        is OrderEvent.Complete -> {
            println(event)
        }
    }
}