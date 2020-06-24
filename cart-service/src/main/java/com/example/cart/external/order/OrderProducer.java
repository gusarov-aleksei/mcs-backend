package com.example.cart.external.order;

import com.example.cart.external.order.model.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    private String topicName;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void placeOrder(final OrderEvent.Create orderEvent) {
        kafkaTemplate.send(topicName, orderEvent);
    }

    @Value("${order.topic}")
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

}
