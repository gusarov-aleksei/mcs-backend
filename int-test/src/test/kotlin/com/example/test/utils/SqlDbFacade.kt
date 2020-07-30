package com.example.test.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.Closeable
import java.util.*
import java.util.concurrent.TimeUnit

val DATA_SOURCE_PROPERTIES  = Properties().also {
    it["jdbcUrl"] = "jdbc:postgresql://localhost/order_db"
    it["username"] = "order_user"
    it["password"] = "order_pass"
}

const val DELETE_ORDERS_BY_CUSTOMER_ID = "DELETE FROM orders WHERE customer_id = uuid(?)"

const val SELECT_COUNT_OF_ALL_ORDERS = "SELECT COUNT(id) FROM orders"

const val SELECT_COUNT_OF_ORDERS_BY_CUSTOMER = "SELECT COUNT(id) FROM orders WHERE customer_id = uuid(?)"

class SqlDbFacade : Closeable {

    private val ds = HikariDataSource(HikariConfig(DATA_SOURCE_PROPERTIES))

    fun deleteOrdersByCustomerId(customerId : String) : Int = ds.connection.use {
        it.prepareStatement(DELETE_ORDERS_BY_CUSTOMER_ID).use {
            it.setString(1, customerId)
            it.executeUpdate()
        }
    }

    fun countOfAllOrders() : Int = ds.connection.use {
        it.prepareStatement(SELECT_COUNT_OF_ALL_ORDERS).use {
            it.executeQuery().use {
                it.next()
                it.getInt(1)
            }
        }
    }

    private fun countOfOrdersByCustomer(customerId : String) = ds.connection.use {
        it.prepareStatement(SELECT_COUNT_OF_ORDERS_BY_CUSTOMER).use {
            it.setString(1, customerId)
            it.executeQuery().use {
                it.next()
                it.getInt(1)
            }
        }
    }

    /**
     * To verify if order is created in db assuming that 4 second is enough for Order Event to reach through Kafka to consumer and db
     */
    fun countOfOrdersChanged(customerId : String) : Boolean {
        var count = 0
        while (countOfOrdersByCustomer(customerId) == 0 && count < 11) {
            TimeUnit.MILLISECONDS.sleep(400)
            count++
        }
        return count < 11
    }

    override fun close() {
        ds.close()
    }
}