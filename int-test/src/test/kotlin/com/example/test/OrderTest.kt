package com.example.test

import com.example.test.utils.SqlDbFacade
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.fail
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.collections.shouldNotBeEmpty
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import kotlinx.serialization.ImplicitReflectionSerializer
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import java.util.*

/**
 * This test verifies order-service cases
 */
@ImplicitReflectionSerializer
class OrderTest : AbstractCartTest() {

    private val jsonMapper = jacksonObjectMapper()

    private val dbFacade = autoClose(SqlDbFacade())

    init {
        feature("Create Order") {
            scenario("Create Order with one product") {
                val ordersInDbBefore = dbFacade.countOfAllOrders()
                val customerId = UUID.randomUUID().toString()
                // init cart
                var cart = useCart("?customerId=$customerId")
                // add product to cart
                useCart("${cart.id}/add?productId=1")
                // place order
                useCart("${cart.id}/checkout")
                // order appears in storage asynchronously. need to wait some time till order be created in db
                if (!dbFacade.countOfOrdersChanged(customerId)) {
                    fail("Count of orders in db was not changed in 4 second(4 second is for enough for local test)")
                }
                // rest call to order-service http://localhost:8094/orders/customer/:customerId
                val orders = client.execute(HttpGet("${orderBaseUrl()}/customer/${cart.customerId}")).use {
                    it.statusLine.statusCode shouldBe 200
                    jsonMapper.readValue<List<Order>>(EntityUtils.toString(it.entity))
                }
                // verify results of call
                orders.shouldNotBeNull()
                orders.shouldHaveSize(1)
                with(orders[0]) {
                    orderId.shouldNotBeNull()
                    status shouldBe Status.CREATED
                    customerId shouldBe customerId
                    totalToPay shouldBe 10.32
                    details shouldBe listOf(Item(productId="1", quantity="1", price="10.32", total="10.32"))
                }
                // clean up database and verify its state
                val deletedOrders = dbFacade.deleteOrdersByCustomerId(customerId)
                deletedOrders shouldBe 1
                val ordersInDbAfter = dbFacade.countOfAllOrders()
                ordersInDbBefore shouldBe ordersInDbAfter
            }
        }
    }
}