package com.example.cart.external.order.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, visible = true)
public class OrderEvent { //like sealed data class in Kotlin
    public static class Create extends OrderEvent {

        public final String customerId;

        public final String totalToPay;

        public final List<Item> items;

        public Create(final String customerId, final String totalToPay, final List<Item> items) {
            this.customerId = customerId;
            this.totalToPay = totalToPay;
            this.items = items;
        }
    }

    public static class Pay extends OrderEvent {
        public final String orderId;

        public Pay(final String orderId) {
            this.orderId = orderId;
        }
    }

    public static class Complete extends OrderEvent {
        public final String orderId;

        public Complete(final String orderId) {
            this.orderId = orderId;
        }
    }
}
