package com.example.order

import java.util.*

val SERVER_PORT = System.getenv("ORDER_SERVER_PORT")?.run { toInt() } ?: 8094

val DATA_SOURCE_PROPERTIES  = Properties().also {
    val host = getenv("ORDER_DB_HOST", "localhost")
    val db_name = getenv("ORDER_DB_NAME", "order_db")

    it["jdbcUrl"] = "jdbc:postgresql://$host/$db_name"
    it["username"] = getenv("ORDER_DB_USERNAME", "order_user")
    it["password"] = getenv("ORDER_DB_PASS", "order_pass")
}

fun getenv(name: String, default: String) = System.getenv(name) ?: default

val ORDER_EVENT_KAFKA_CONSUMER_PROPERTIES = Properties().also {
    it["bootstrap.servers"] = getenv("KAFKA_BOOTSTRAP_SERVERS","localhost:9092")
    it["key.deserializer"] = "org.apache.kafka.common.serialization.StringDeserializer"
    it["value.deserializer"] = "com.example.order.model.OrderDeserializer"
    it["group.id"] = "my_group"
    it["auto.offset.reset"] = "earliest"
    it["enable.auto.commit"] = "false"
}