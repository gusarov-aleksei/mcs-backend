package com.example.order.test

import com.example.order.model.Item
import com.example.order.model.OrderEvent

val CREATE_EVENT_SAMPLE = OrderEvent.Create(
        "1bec138e-689e-485b-be91-4f05d7a13a55",
        92.1,
        listOf(Item(1,5,10.06,50.3), Item(2,10,4.18,41.8))
)

val ANOTHER_CREATE_EVENT_SAMPLE = OrderEvent.Create(
        "1bec138e-689e-485b-be91-4f05d7a13a56",
        50.88,
        listOf(Item(1,3,10.06,30.18), Item(3,4,5.2,20.8))
)

fun anotherCreateEventSample() : OrderEvent.Create {
    return OrderEvent.Create(
            "1bec138e-689e-485b-be91-4f05d7a13a56",
            50.88,
            listOf(Item(1,3,10.06,30.18), Item(3,4,5.2,20.8))
    )
}