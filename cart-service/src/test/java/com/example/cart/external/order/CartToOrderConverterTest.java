package com.example.cart.external.order;

import com.example.cart.external.order.model.Item;
import com.example.cart.external.order.model.OrderEvent;
import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CartToOrderConverterTest {

    private CartToOrderConverter cartToOrderConverter = new CartToOrderConverter();

    @Test
    public void testConvert_returnsOrderEventCreate_whenNonNullValidCartIsPassed() {
        Cart cart = new Cart();
        cart.setCustomerId("customer-1");
        cart.setId("cart-id");
        cart.setTotal(new BigDecimal("30.50"));
        cart.setCartItems(List.of(
                new CartItem("product-1", "Useful product 1", new BigDecimal("10.50")),
                new CartItem("product-2", "Useful product 2", new BigDecimal("10.00"), 2, new BigDecimal("20.00"))));
        OrderEvent.Create create = cartToOrderConverter.convert(cart);

        assertThat(create.customerId).isEqualTo("customer-1");
        assertThat(create.totalToPay).isEqualTo("30.50");
        assertThat(create.items).containsExactlyInAnyOrder(
                new Item("product-1", "1", "10.50", "10.50"),
                new Item("product-2", "2", "10.00", "20.00"));
    }

    @Test
    public void testConvertCartItem_returnsItem_whenNonNullValidCartItemIsPassed() {
        CartItem cartItem =
                new CartItem("product-2", "Useful product 2", new BigDecimal("10.00"), 2, new BigDecimal("20.00"));
        Item item = cartToOrderConverter.convertCartItem(cartItem);
        assertThat(item.price).isEqualTo("10.00");
        assertThat(item.total).isEqualTo("20.00");
        assertThat(item.productId).isEqualTo("product-2");
        assertThat(item.quantity).isEqualTo("2");
    }


}
