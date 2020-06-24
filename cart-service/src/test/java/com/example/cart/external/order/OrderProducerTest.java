package com.example.cart.external.order;

import com.example.cart.external.order.model.Item;
import com.example.cart.external.order.model.OrderEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static reactor.core.publisher.Mono.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderProducerTest {

    @Mock
    KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @InjectMocks
    OrderProducer orderProducer;

    @Test
    public void testPlaceOrder_shouldSendToKafkaOrderEvent_inAnyCase() {
        orderProducer.setTopicName("OrderEventTopic");
        //OrderEvent.Create order
        OrderEvent.Create createOrderEvent =
                new OrderEvent.Create("customer-1", "99.99",
                        List.of(new Item("product-1", "1", "99.99","99.99")));

        doReturn(new SettableListenableFuture<SendResult<String, OrderEvent>>())
                .when(kafkaTemplate).send("OrderEventTopic", createOrderEvent);

        orderProducer.placeOrder(createOrderEvent);

        verify(kafkaTemplate).send("OrderEventTopic", createOrderEvent);
    }



}
