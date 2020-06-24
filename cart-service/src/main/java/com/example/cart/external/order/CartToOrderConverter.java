package com.example.cart.external.order;

import com.example.cart.external.order.model.OrderEvent.Create;
import com.example.cart.external.order.model.Item;
import com.example.cart.external.order.model.OrderEvent;
import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;
import org.springframework.stereotype.Component;


import java.util.stream.Collectors;

@Component
public class CartToOrderConverter {

    public Create convert(final Cart cart) {
        return new OrderEvent.Create(
                cart.getCustomerId(),
                cart.getTotal().toString(),
                cart.getCartItems().stream().map(this::convertCartItem).collect(Collectors.toUnmodifiableList()));
    }

    protected Item convertCartItem(final CartItem cartItem) {
        return new Item(
                cartItem.getProductId(),
                String.valueOf(cartItem.getQuantity()),
                String.valueOf(cartItem.getPrice()),
                String.valueOf(cartItem.getTotal()));
    }
}
