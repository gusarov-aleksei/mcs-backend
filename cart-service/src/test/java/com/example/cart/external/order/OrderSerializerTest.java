package com.example.cart.external.order;

import com.example.cart.external.order.model.Item;
import com.example.cart.external.order.model.OrderEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class OrderSerializerTest {

    @Test
    public void testSerialize_shouldReturnArrayOfBytes_whenCreateEvent() {
        OrderSerializer orderSerializer = new OrderSerializer();
        OrderEvent.Create create = new OrderEvent.Create("customer-1", "100.10",
                List.of(new Item("product-1", "1","1000.10","100.10")));
        byte[] result = orderSerializer.serialize("", create);
        //JSON representation {"@c":".OrderEvent$Create","customerId":"customer-1","totalToPay":"100.10","items":[{"productId":"product-1","quantity":"1","price":"1000.10","total":"100.10"}]}
        byte[] expected = new byte[]{123, 34, 64, 99, 34, 58, 34, 46, 79, 114, 100, 101, 114, 69, 118, 101,
                110, 116, 36, 67, 114, 101, 97, 116, 101, 34, 44, 34, 99, 117, 115, 116, 111, 109, 101, 114,
                73, 100, 34, 58, 34, 99, 117, 115, 116, 111, 109, 101, 114, 45, 49, 34, 44, 34, 116, 111, 116,
                97, 108, 84, 111, 80, 97, 121, 34, 58, 34, 49, 48, 48, 46, 49, 48, 34, 44, 34, 105, 116, 101, 109,
                115, 34, 58, 91, 123, 34, 112, 114, 111, 100, 117, 99, 116, 73, 100, 34, 58, 34, 112, 114, 111, 100,
                117, 99, 116, 45, 49, 34, 44, 34, 113, 117, 97, 110, 116, 105, 116, 121, 34, 58, 34, 49, 34, 44,
                34, 112, 114, 105, 99, 101, 34, 58, 34, 49, 48, 48, 48, 46, 49, 48, 34, 44, 34, 116, 111, 116,
                97, 108, 34, 58, 34, 49, 48, 48, 46, 49, 48, 34, 125, 93, 125};

        Assert.assertArrayEquals(expected, result);
    }

    @Test
    public void testSerialize_shouldReturnArrayOfBytes_whenCreateEventIsNull() {
        OrderSerializer orderSerializer = new OrderSerializer();
        OrderEvent.Create create = null;
        byte[] result = orderSerializer.serialize("", create);
        byte[] expected = null;

        Assert.assertArrayEquals(expected, result);
    }

}
