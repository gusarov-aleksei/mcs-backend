package com.example.cart.model;

import java.math.BigDecimal;
import java.util.Objects;

public class CartItem {

    private String productId;

    private String name;

    private BigDecimal price;

    private int quantity;

    private BigDecimal total;

    public CartItem() {
    }

    public CartItem(String productId, String name, BigDecimal price) {
        this.price = price;
        this.name = name;
        this.productId = productId;
        this.total = price;
        this.quantity = 1;
    }

    public CartItem(String productId, String name, double price) {
        this.price = BigDecimal.valueOf(price);
        this.name = name;
        this.productId = productId;
        this.total = this.price;
        this.quantity = 1;
    }

    public CartItem(String productId, String name, BigDecimal price, int quantity, BigDecimal total) {
        this.price = price;
        this.name = name;
        this.productId = productId;
        this.total = total;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", total=" + total +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return quantity == cartItem.quantity &&
                productId.equals(cartItem.productId) &&
                name.equals(cartItem.name) &&
                price.equals(cartItem.price) &&
                total.equals(cartItem.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, price, quantity, total);
    }
}
