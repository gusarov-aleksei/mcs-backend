package com.example.order.dao

import com.example.order.model.Item
import com.example.order.model.Order
import com.example.order.model.OrderEvent
import com.example.order.model.Status
import com.zaxxer.hikari.HikariDataSource
import java.lang.Exception
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource

interface OrderRepository : AutoCloseable {

    fun findOrdersWithDetailsByCustomerId(customerId : String) : Collection<Order>

    fun findOrdersByCustomerId(customerId: String) : Collection<Order>

    fun findOrderDetailsByOrderId(orderId : Int) : Collection<Item>

    fun createOrder(createEvent : OrderEvent.Create)

    fun updateStatus(orderId: String, status: Status)

    fun findOrderById(id: Int) : Order?
}


class OrderDao(private val dataSource: HikariDataSource) : OrderRepository {
    /**
     * Retrieve Orders with Details by customer Id
     */
    override fun findOrdersWithDetailsByCustomerId(customerId : String) : Collection<Order> {
        return selectOrdersByCustomerId(dataSource.connection, customerId)
    }

    /**
     * Retrieve Orders without Details by customer Id
     */
    override fun findOrdersByCustomerId(customerId: String) : Collection<Order> {
        TODO()
    }

    private fun selectOrdersByCustomerId(conn: Connection, customerId: String) = conn.use {
        it.prepareStatement(SELECT_ORDERS_WITH_DETAILS_BY_CUSTOMER).use {
            it.setString(1, customerId)
            it.executeQuery().use {
                transformResultToOrderList(it)
            }
        }
    }

    override fun findOrderDetailsByOrderId(orderId : Int) : Collection<Item> {
        return selectDetailsByOrderId(dataSource.connection, orderId)
    }

    private fun selectDetailsByOrderId(conn: Connection, orderId: Int) : Collection<Item> = conn.use {
        it.prepareStatement(SELECT_DETAILS_BY_ORDER_ID).use {
            it.setInt(1, orderId)
            it.executeQuery().use {
                generateSequence {
                    if (it.next()) transformResultToOrderDetail(it) else null
                }.toList()
            }
        }
    }

    override fun createOrder(createEvent : OrderEvent.Create) {
        //TODO return order_id
        dataSource.connection.use {
            performTransactional(it) {
                val orderId = insertOrder(it, createEvent)
                insertOrderDetails(it, orderId, createEvent.items)
            }
        }
    }

    private fun performTransactional(conn: Connection, block: () -> Unit) = try {
        conn.autoCommit = false
        block()
        conn.commit()
    } catch (e : Exception) {
        conn.rollback()
    } finally {
        conn.autoCommit = true
    }

    /**
     *  Insert order into DB and return its id.
     *  Method doesn't close connection.
     */
    private fun insertOrder(conn: Connection, createEvent : OrderEvent.Create) : Int = conn.run {
        this.prepareStatement(INSERT_ORDER_QUERY).use {
            it.setString(1,createEvent.customerId)
            it.setDouble(2,createEvent.totalToPay)
            it.setString(3, Status.CREATED.toString())
            it.execute()
            val res = it.resultSet
            res.next()
            res.getInt(1)
        }
    }

    /**
     * Insert order details for Order.
     * Method doesn't close connection.
     */
    private fun insertOrderDetails(conn: Connection, orderId: Int, items : Collection<Item>) : Unit = conn.run {
        this.prepareStatement(INSERT_ORDER_DETAILS_QUERY).use {
            state -> items.forEach {
                state.setInt(1, orderId)
                state.setInt(2, it.productId)
                state.setShort(3, it.quantity)
                state.setDouble(4, it.price)
                state.setDouble(5, it.total)
                state.addBatch()
            }
            state.executeBatch()
        }
    }

    /**
     * Update Ordee status
     */
    override fun updateStatus(orderId: String, status: Status) {
        TODO()
    }

    override fun findOrderById(id: Int) : Order? {
        return null;
        //TODO()
    }

    override fun close() {
        dataSource.close()
    }
}

fun initDataSource() = HikariDataSource().also {
    it.jdbcUrl = "jdbc:postgresql://localhost/order_db"
    it.username = "order_user"
    it.password = "order_pass"
}