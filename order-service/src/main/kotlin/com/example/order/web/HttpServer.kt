package com.example.order.web

import com.example.order.dao.OrderRepository
import com.example.order.test.ANOTHER_CREATE_EVENT_SAMPLE
import com.example.order.test.CREATE_EVENT_SAMPLE
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import java.lang.Exception
import io.javalin.http.Context
import org.eclipse.jetty.http.HttpStatus

class HttpServer(private val port : Int, private val orderController: OrderController) {

    fun init() : Javalin {
        val app = Javalin.create().apply{
            exception(Exception::class.java) { e, _ -> e.printStackTrace() }
            error(404) { ctx -> ctx.json("Requested resource not found") }
        }.start(port)

        app.routes {
            ApiBuilder.get("/orders/details/:order-id") {
                orderController.findOrderDetailsByOrderId(it)
            }

            ApiBuilder.get("/orders/customer/:customer-id") {
                orderController.findOrdersWithDetailsByCustomerId(it)
            }

            ApiBuilder.get("/orders/:order-id") {
                orderController.findOrderById(it)
            }

            ApiBuilder.get("/init-test-data") {
                orderController.initTestData(it)
            }
        }

        app.events {
            it.serverStopped {
                // call data source cleaning
                orderController.close()
                println("Clean up resources")
            }
        }
        return app
    }
}

class OrderController(private val orderDao: OrderRepository) : AutoCloseable  {

    fun findOrderDetailsByOrderId(context: Context) {
        val orderId = context.pathParam("order-id").toInt()
        context.json(orderDao.findOrderDetailsByOrderId(orderId))
    }

    fun findOrdersWithDetailsByCustomerId(context: Context) {
        val customerId = context.pathParam("customer-id")
        context.json(orderDao.findOrdersWithDetailsByCustomerId(customerId))
    }

    fun findOrderById(context: Context) {
        val orderId = context.pathParam("order-id").toInt()
        orderDao.findOrderById(orderId)?.let{
            context.json(it)
        } ?: run {
            context.status(HttpStatus.NOT_FOUND_404)
        }
    }

    fun initTestData(context: Context) {
        context.json(orderDao.createOrder(CREATE_EVENT_SAMPLE))
        context.json(orderDao.createOrder(ANOTHER_CREATE_EVENT_SAMPLE))
        context.result("Test data generated")
    }

    override fun close() {
        orderDao.close()
    }
}