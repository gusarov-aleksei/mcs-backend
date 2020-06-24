package com.example.cart.external.order.model;

import java.util.Objects;

public class Item {

    public final String productId;

    public final String quantity;

    public final String price;

    public final String total;

    public Item(final String productId, final String quantity, final String price, final String total) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.total = total;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return productId.equals(item.productId) &&
                quantity.equals(item.quantity) &&
                price.equals(item.price) &&
                total.equals(item.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, price, total);
    }
}
