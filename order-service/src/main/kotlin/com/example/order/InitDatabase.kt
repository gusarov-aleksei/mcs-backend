package com.example.order

import com.example.order.dao.*
import com.example.order.test.anotherCreateEventSample
import com.example.order.test.createEventSample
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.util.*

/**
 * Action for test
 */
fun main() {

    val ds = HikariDataSource(HikariConfig(hikariProps()))
    val orderDao = OrderDao(ds)

    orderDao.createOrder(createEventSample())
    orderDao.createOrder(anotherCreateEventSample())

    Runtime.getRuntime().addShutdownHook(Thread(Runnable { ds.close()}))
}

fun hikariProps() : Properties {
    val hikariProps = Properties()
    with(hikariProps) {
        put("dataSourceClassName","org.postgresql.ds.PGSimpleDataSource")
        put("dataSource.user","order_user")
        put("dataSource.password","order_pass")
        put("dataSource.databaseName","order_db")
        put("dataSource.portNumber","5432")
        put("dataSource.serverName","localhost")
    }
    return hikariProps;
}