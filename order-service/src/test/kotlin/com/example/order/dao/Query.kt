package com.example.order.dao

import javax.sql.DataSource

const val TRUNCATE_ORDERS = "TRUNCATE TABLE orders RESTART IDENTITY CASCADE"

const val SELECT_ALL_ORDERS_WITHOUT_DETAILS =
        "SELECT o.id, o.customer_id, o.total_to_pay, o.order_status FROM orders o"

const val SELECT_ALL_ORDER_DETAILS ="SELECT * FROM order_details"

/*
    Queries for test maintenance
 */
fun selectAllOrdersWithoutDetails(ds:DataSource) = ds.connection.use {
    it.createStatement().use {
        it.executeQuery(SELECT_ALL_ORDERS_WITHOUT_DETAILS).use {
            transformResultToList(it) {
                transformResultToOrder(it)
            }
        }
    }
}

fun truncateOrders(ds:DataSource) = ds.connection.use {
    it.prepareStatement(TRUNCATE_ORDERS).use {
        it.executeUpdate()
    }
}

fun selectAllOrderDetails(ds:DataSource) = ds.connection.use {
    it.createStatement().use {
        it.executeQuery(SELECT_ALL_ORDER_DETAILS).use {
            transformResultToList(it) {
                transformResultToOrderDetail(it)
            }
        }
    }
}