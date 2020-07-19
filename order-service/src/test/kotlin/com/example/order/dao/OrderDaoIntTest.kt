package com.example.order.dao

import com.example.order.model.Item
import com.example.order.model.Order
import com.example.order.model.Status
import com.example.order.test.anotherCreateEventSample
import com.example.order.test.createEventSample
import com.zaxxer.hikari.HikariDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.*
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.junit.jupiter.api.Assertions.assertEquals
import javax.sql.DataSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Order DAO test")
class OrderDaoIntTest {

    private lateinit var db : GenericContainer<Nothing>
    private lateinit var orderDao: OrderDao
    private lateinit var ds: HikariDataSource

    @BeforeAll
    fun setUp() {
        db = GenericContainer<Nothing>("postgres:12.3-alpine").apply {
            withExposedPorts(5432)
            withEnv("POSTGRES_DB", "order_db")
            withEnv("POSTGRES_USER", "order_user")
            withEnv("POSTGRES_PASSWORD", "order_pass")
            withClasspathResourceMapping("/schema.sql","/docker-entrypoint-initdb.d/schema.sql", BindMode.READ_ONLY)
        }
        db.start()
        ds = initDataSource(db.containerIpAddress, db.firstMappedPort);
        orderDao = OrderDao(ds)

    }

    @AfterAll
    fun tearDown() {
        ds.close()
        db.stop()
    }

    @Test
    fun `should create order in data base`() {
        orderDao.createOrder(createEventSample())
        orderDao.createOrder(anotherCreateEventSample())
        val orders = selectAllOrdersWithoutDetails(ds)
        assertEquals(2, orders.count())
        val expected = listOf(
                Order(1,"1bec138e-689e-485b-be91-4f05d7a13a55",92.1, Status.CREATED, mutableListOf()),
                Order(2,"1bec138e-689e-485b-be91-4f05d7a13a56",50.88, Status.CREATED, mutableListOf())
        )
        assertEquals(expected, orders)
    }

    @Test
    fun `should find orders by customer id`() {
        val event = createEventSample()
        orderDao.createOrder(event)
        orderDao.createOrder(anotherCreateEventSample())

        val orders = orderDao.findOrdersWithDetailsByCustomerId(event.customerId)

        assertEquals(1, orders.count())
        val expected = listOf(
                Order(1,"1bec138e-689e-485b-be91-4f05d7a13a55",92.1, Status.CREATED,
                        mutableListOf(Item(1,5,10.06,50.3), Item(2,10,4.18,41.8)))
        )

        assertEquals(expected, orders)
    }

    @Test
    fun `should find order details by order id`() {
        val event = createEventSample()
        orderDao.createOrder(event)
        orderDao.createOrder(anotherCreateEventSample())

        val orderDetails = orderDao.findOrderDetailsByOrderId(1)

        assertEquals(2, orderDetails.count())
        val expected = mutableListOf(Item(1,5,10.06,50.3), Item(2,10,4.18,41.8))

        assertEquals(expected, orderDetails)
    }

    @AfterEach
    fun cleanDataBase() {
        truncateOrders(ds)
        //println("orders after delete ${selectAllOrdersWithoutDetails(ds)}")
        //println("order_details after delete ${selectAllOrderDetails(ds)}")
    }

}
fun initDataSource(host: String, port : Int) = HikariDataSource().apply {
    jdbcUrl = "jdbc:postgresql://$host:$port/order_db"
    username = "order_user"
    password = "order_pass"
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Order DAO close test")
class OrderDaoCloseTest {

    @Test
    fun `should be able to close its data source`() {
        val dsMock = mockk<HikariDataSource>()
        val orderDao = OrderDao(dsMock)
        every { dsMock.close() } returns Unit

        orderDao.close()

        verify(exactly = 1) { dsMock.close() }
    }


}