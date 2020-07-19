package com.example.order

import com.example.order.dao.OrderDao
import com.example.order.dao.initDataSource
import com.example.order.web.HttpServer
import com.example.order.web.OrderController

fun main(args: Array<String>) {

    val dataSource = initDataSource()
    val orderDao = OrderDao(dataSource)
    val orderController = OrderController(orderDao)
    val httpServer = HttpServer(8080, orderController).init()

    // clean up resources when main thread is stopped
    Runtime.getRuntime().addShutdownHook(Thread( Runnable { httpServer.stop() }))
}
