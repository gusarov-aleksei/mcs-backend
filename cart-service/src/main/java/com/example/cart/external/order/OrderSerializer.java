package com.example.cart.external.order;

import com.example.cart.external.order.model.OrderEvent;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class OrderSerializer implements Serializer<OrderEvent> {

    private final ObjectMapper jsonMapper;

    public OrderSerializer() {
        jsonMapper = new ObjectMapper();
        jsonMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }


    @Override
    public void configure(final Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(final String s, final OrderEvent createOrderEvent) {
        if (createOrderEvent == null) {
            return  null;
        }
        try {
            return jsonMapper.writeValueAsBytes(createOrderEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

    }
}
