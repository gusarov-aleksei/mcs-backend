package com.example.order

import com.example.order.dao.OrderDao
import com.example.order.dao.initDataSource
import com.example.order.web.HttpServer
import com.example.order.web.OrderController

fun main(args: Array<String>) {
    //println("Print all environment ${System.getenv()}")
    val orderDao = OrderDao(initDataSource())
    val orderController = OrderController(orderDao)
    val httpServer = HttpServer(SERVER_PORT, orderController).init()

    // clean up resources when main thread is stopped
    Runtime.getRuntime().addShutdownHook(Thread( Runnable { httpServer.stop() }))
}
