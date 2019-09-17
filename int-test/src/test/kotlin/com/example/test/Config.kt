package com.example.test

import java.io.FileInputStream
import java.util.*

object Config {

    val props = Properties()

    init{
        props.load(FileInputStream(Config.javaClass.classLoader.getResource("application.properties").file))
    }

    val catalogBaseUrl = "http://${props.getProperty("catalog.url")}:${props.getProperty("catalog.port")}/api/catalog"
    val cartBaseUrl = "http://${props.getProperty("cart.url")}:${props.getProperty("cart.port")}/api/cart"
}

fun catalogBaseUrl() = Config.catalogBaseUrl

fun cartBaseUrl() = Config.cartBaseUrl

fun productsFile() = Config.props.getProperty("products.fullFileName")!!