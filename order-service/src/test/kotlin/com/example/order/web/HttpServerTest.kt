package com.example.order.web

import com.example.order.dao.OrderRepository
import com.example.order.model.Item
import com.example.order.model.Order
import com.example.order.model.OrderEvent
import com.example.order.model.Status
import io.javalin.Javalin
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.responseObject
import org.eclipse.jetty.http.HttpStatus
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Order HTTP API")
class HttpServerTest {

    private lateinit var httpServer: Javalin

    @BeforeAll
    fun setUp() {
        val orderDao = OrderDaoStub() // mock repository for decoupling from database
        val orderController = OrderController(orderDao)
        httpServer = HttpServer(8080, orderController).init()
        FuelManager.instance.basePath = "http://localhost:${httpServer.port()}/"
    }

    @AfterAll
    fun tearDown() {
        httpServer.stop()
    }

    @Test
    fun `should get order details for existing order`() {
        val (_, _, result) = "/orders/details/1".httpGet().responseObject<Collection<Item>>()
        assertEquals(listOf(Item(1,2,32.55, 65.1)), result.get())
    }

    @Test
    fun `should get empty details for non-existing order`() {
        val (_, _, result) = "/orders/details/2".httpGet().responseObject<Collection<Item>>()
        assertEquals(emptyList<Item>(), result.get())
    }

    @Test
    fun `should get orders for existing customer`() {
        val (_, _, result) = "/orders/customer/1bec138e-689e-485b-be91-4f05d7a13a55".httpGet().responseObject<Collection<Order>>()
        assertEquals(listOf(Order(1, "1bec138e-689e-485b-be91-4f05d7a13a55", 65.1, Status.CREATED, mutableItems)), result.get())
    }

    @Test
    fun `should get empty orders if orders are not existing`() {
        val (_, _, result) = "/orders/customer/1bec138e-689e-485b-be91-4f05d7a13a54".httpGet().responseObject<Collection<Order>>()
        assertEquals(emptyList<Order>(), result.get())
    }

    @Test
    fun `should get order by id if order exists`() {
        val (_, _, result) = "/orders/1".httpGet().responseObject<Order>()
        assertEquals(
                Order(1, "1bec138e-689e-485b-be91-4f05d7a13a55", 65.1, Status.CREATED, mutableItems),
                result.get()
        )
    }

    @Test
    fun `should get 'not found' response if order doesn't exist`() {
        val (_, _, result) = "/orders/2".httpGet().responseObject<Order>()
        when(result) {
            is com.github.kittinunf.result.Result.Failure -> assertEquals(HttpStatus.NOT_FOUND_404, result.error.response.statusCode)
            else -> fail("Expected Failure")
        }
    }

}

class OrderDaoStub : OrderRepository {
    override fun findOrdersWithDetailsByCustomerId(customerId: String): Collection<Order> {
        if ("1bec138e-689e-485b-be91-4f05d7a13a55" == customerId) {
            return listOf(Order(1, "1bec138e-689e-485b-be91-4f05d7a13a55", 65.1, Status.CREATED, mutableItems))
        }
        return emptyList()
    }

    override fun findOrdersByCustomerId(customerId: String): Collection<Order> {
        if ("1bec138e-689e-485b-be91-4f05d7a13a55" == customerId) {
            return listOf(Order(1, "1bec138e-689e-485b-be91-4f05d7a13a55", 65.1, Status.CREATED, mutableListOf()))
        }
        return emptyList()
    }

    override fun findOrderDetailsByOrderId(orderId: Int): Collection<Item> {
        if (orderId == 1) {
            return mutableItems
        }
        return emptyList()
    }

    override fun createOrder(createEvent: OrderEvent.Create) {
        TODO("Not yet implemented")
    }

    override fun updateStatus(orderId: String, status: Status) {
        TODO("Not yet implemented")
    }

    override fun findOrderById(id: Int): Order? = if (id == 1) {
            Order(1, "1bec138e-689e-485b-be91-4f05d7a13a55", 65.1, Status.CREATED, mutableItems)
        } else {
            null
    }
}

val mutableItems = mutableListOf(Item(1,2,32.55, 65.1))