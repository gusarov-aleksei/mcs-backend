package com.example.order.dao

import com.example.order.model.Item
import com.example.order.model.Order
import com.example.order.model.Status
import java.sql.ResultSet

fun transformResultToOrderDetail(res : ResultSet) : Item = with(res) {
    Item(
            getInt("product_id"),
            getShort("quantity"),
            getDouble("price"),
            getDouble("total")
    )
}

fun transformResultToOrder(res : ResultSet) : Order = with(res) {
    Order(
            getInt("id"),
            getString("customer_id"),
            getDouble("total_to_pay"),
            Status.valueOf(getString("order_status")),
            mutableListOf()
    )
}

fun transformResultToOrderList(res : ResultSet) : List<Order> {
    /*if (res.fetchSize == 0) {
        println(res.fetchSize)
        return emptyList()
    }*/
    //TODO don't create map if no result
    val orders = mutableMapOf<Int, Order>()
    while (res.next()) {
        val order = orders.computeIfAbsent(res.getInt("id")) {
            _ -> transformResultToOrder(res)
        }
        order.details.add(transformResultToOrderDetail(res))
    }
    return orders.values.toList()
}

fun <T : Any> transformResultToList(res : ResultSet, transform : (ResultSet) -> T) : List<T> = generateSequence {
    if (res.next()) transform(res) else null
}.toList()