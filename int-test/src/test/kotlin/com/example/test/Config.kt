package com.example.test

import java.io.FileInputStream
import java.util.*

object Config {

    val props = Properties()

    init{
        Config.javaClass.classLoader.getResource("application.properties")
        props.load(FileInputStream(Config.javaClass.classLoader.getResource("application.properties").file))
    }

    val catalogBaseUrl = "http://${props.getProperty("catalog.url")}:${props.getProperty("catalog.port")}/api/catalog"
    val cartBaseUrl = "http://${props.getProperty("cart.url")}:${props.getProperty("cart.port")}/api/cart"
    val customerBaseUrl = "http://${props.getProperty("customer.url")}:${props.getProperty("customer.port")}/v2/api/customer"
    val orderBaseUrl = "http://${props.getProperty("order.url")}:${props.getProperty("order.port")}/orders"
}

fun customerCreateUrl() = "http://${Config.props.getProperty("customer.url")}:${Config.props.getProperty("customer.port")}/v2/api/customer/create"

fun customerUpdateUrl() = "http://${Config.props.getProperty("customer.url")}:${Config.props.getProperty("customer.port")}/v2/api/customer/update"

fun customerBaseUrl() = Config.customerBaseUrl

fun catalogBaseUrl() = Config.catalogBaseUrl

fun cartBaseUrl() = Config.cartBaseUrl

fun orderBaseUrl() = Config.orderBaseUrl

fun productsFile() = Config.props.getProperty("products.fullFileName")!!

