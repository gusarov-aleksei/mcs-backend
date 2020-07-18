package com.example.order.dao

import com.example.order.model.Item
import com.example.order.model.Order
import com.example.order.model.Status
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import com.mockrunner.mock.jdbc.MockResultSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Transformation from ResultSet to objects")
class TransformationTest {

    @Test
    fun `should transform result set to Order Details`() {
        val resultSet = MockResultSet("resMock")
        resultSet.addColumn("product_id", arrayOf(1))
        resultSet.addColumn("quantity", arrayOf<Short>(2))
        resultSet.addColumn("price", arrayOf(10.53))
        resultSet.addColumn("total", arrayOf(21.06))
        resultSet.next()

        val item = transformResultToOrderDetail(resultSet);

        assertEquals(Item(1, 2, 10.53, 21.06), item)
    }

    @Test
    fun `should transform result set to list of Order Details`() {
        val resultSet = MockResultSet("resMock")
        resultSet.addColumn("product_id", arrayOf(1, 2))
        resultSet.addColumn("quantity", arrayOf<Short>(2, 10))
        resultSet.addColumn("price", arrayOf(10.53, 1.86))
        resultSet.addColumn("total", arrayOf(21.06, 18.6))

        val listOfItems = transformResultToList(resultSet) {
            transformResultToOrderDetail(resultSet)
        }

        val expected = listOf(Item(1, 2, 10.53, 21.06), Item(2, 10, 1.86, 18.6))
        assertEquals(expected, listOfItems)
    }


    @Test
    fun `should transform result set to Order`() {
        val resultSet = MockResultSet("resMock")
        resultSet.addColumn("id", arrayOf(1))
        resultSet.addColumn("customer_id", arrayOf("1bec138e-689e-485b-be91-4f05d7a13a55"))
        resultSet.addColumn("total_to_pay", arrayOf(21.06))
        resultSet.addColumn("order_status", arrayOf("CREATED"))
        resultSet.next() // to move iterator to first element

        val order = transformResultToOrder(resultSet)
        val expected = Order(1,"1bec138e-689e-485b-be91-4f05d7a13a55", 21.06, Status.CREATED, mutableListOf())
        assertEquals(expected, order)
    }

    @Test
    fun `should transform result set to list of Orders`() {
        val resultSet = MockResultSet("resMock")
        resultSet.addColumn("id", arrayOf(1, 2))
        resultSet.addColumn("customer_id", arrayOf("1bec138e-689e-485b-be91-4f05d7a13a55", "1bec138e-689e-485b-be91-4f05d7a13a56"))
        resultSet.addColumn("total_to_pay", arrayOf(21.06, 22.08))
        resultSet.addColumn("order_status", arrayOf("CREATED","CREATED"))

        val listOfOrders = transformResultToList(resultSet) {
            transformResultToOrder(resultSet)
        }
        val expected = listOf(
                Order(orderId=1, customerId="1bec138e-689e-485b-be91-4f05d7a13a55", totalToPay=21.06, status=Status.CREATED, details=mutableListOf()),
                Order(orderId=2, customerId="1bec138e-689e-485b-be91-4f05d7a13a56", totalToPay=22.08, status=Status.CREATED, details=mutableListOf())
        )
        assertEquals(expected, listOfOrders)
    }

    @Test
    fun `should transform result set to list of Orders with Order Details`() {
        // result set as result of two tables join
        val resultSet = MockResultSet("resMock")
        resultSet.fetchSize = 2
        resultSet.addColumn("id", arrayOf(1, 1))
        resultSet.addColumn("customer_id", arrayOf("1bec138e-689e-485b-be91-4f05d7a13a55", "1bec138e-689e-485b-be91-4f05d7a13a55"))
        resultSet.addColumn("total_to_pay", arrayOf(39.12, 39.12))
        resultSet.addColumn("order_status", arrayOf("CREATED","CREATED"))
        resultSet.addColumn("product_id", arrayOf(1, 2))
        resultSet.addColumn("quantity", arrayOf<Short>(2, 10))
        resultSet.addColumn("price", arrayOf(10.53, 1.86))
        resultSet.addColumn("total", arrayOf(21.06, 18.6))

        val listOfOrders = transformResultToOrderList(resultSet)

        val expected = listOf(
                Order(
                        orderId=1,
                        customerId="1bec138e-689e-485b-be91-4f05d7a13a55",
                        totalToPay=39.12,
                        status=Status.CREATED,
                        details=mutableListOf(Item(1, 2, 10.53, 21.06), Item(2, 10, 1.86, 18.6))
                )
        )
        assertEquals(expected, listOfOrders)
    }

}
