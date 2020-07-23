package com.example.order.service

import com.example.order.dao.OrderDao
import com.example.order.model.OrderEvent
import com.example.order.test.CREATE_EVENT_SAMPLE
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class OrderServiceTest {

    @Test
    fun`should call create order dao for Create Order event`() {
        val mockOrderDao = mockk<OrderDao>()
        every { mockOrderDao.createOrder(CREATE_EVENT_SAMPLE) } returns Unit

        val orderService = OrderService(mockOrderDao)

        orderService.processOrderEvent(CREATE_EVENT_SAMPLE)

        verify(exactly = 1) { mockOrderDao.createOrder(CREATE_EVENT_SAMPLE) }
    }

    @Test
    fun`should not call create order dao for non Create Order event(Pay Order event)`() {
        val mockOrderDao = mockk<OrderDao>()
        every { mockOrderDao.createOrder(any()) } returns Unit

        val orderService = OrderService(mockOrderDao)

        orderService.processOrderEvent(OrderEvent.Pay(1))

        verify(exactly = 0) { mockOrderDao.createOrder(any()) }
    }

    @Test
    fun`should not call create order dao for non Create Order event(Complete Order event)`() {
        val mockOrderDao = mockk<OrderDao>()
        every { mockOrderDao.createOrder(any()) } returns Unit

        val orderService = OrderService(mockOrderDao)

        orderService.processOrderEvent(OrderEvent.Complete(1))

        verify(exactly = 0) { mockOrderDao.createOrder(any()) }
    }


}