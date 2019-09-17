package com.example.cart.service;

import com.example.cart.model.Cart;

import java.util.Optional;

public interface CartService {

    Cart initCart(String customerId);

    Optional<Cart> getCart(String cardId);

    Optional<Cart> addProduct(String cartId, String productId);

    Optional<Cart> removeProduct(String cartId, String productId);

    Optional<Cart> placeOrder(String id);

}
