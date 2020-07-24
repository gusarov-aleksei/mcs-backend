package com.example.order

import com.example.order.dao.*
import com.example.order.test.ANOTHER_CREATE_EVENT_SAMPLE
import com.example.order.test.CREATE_EVENT_SAMPLE
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.util.*

/**
 * Action for test
 */
fun main() {

    val ds = HikariDataSource(HikariConfig(hikariProps()))
    val orderDao = OrderDao(ds)

    orderDao.createOrder(CREATE_EVENT_SAMPLE)
    orderDao.createOrder(ANOTHER_CREATE_EVENT_SAMPLE)

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