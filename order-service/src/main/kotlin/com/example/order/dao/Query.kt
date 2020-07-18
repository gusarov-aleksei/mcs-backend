package com.example.order.dao

const val INSERT_ORDER_QUERY =
        "INSERT INTO orders(customer_id, total_to_pay, order_status) " +
        "VALUES(uuid(?), ?, CAST(? AS order_statuses)) RETURNING id"

const val INSERT_ORDER_DETAILS_QUERY =
        "INSERT INTO order_details(order_id, product_id, quantity, price, total) VALUES(?,?,?,?,?)"

const val SELECT_ORDERS_WITH_DETAILS_BY_CUSTOMER =
        "SELECT o.id, o.customer_id, o.total_to_pay, o.order_status, d.product_id, d.quantity, d.price, d.total "+
                "FROM orders o JOIN order_details d on o.id = d.order_id WHERE o.customer_id = uuid(?)";

const val SELECT_DETAILS_BY_ORDER_ID =
        "SELECT product_id, quantity, price, total FROM order_details WHERE order_id = ?";