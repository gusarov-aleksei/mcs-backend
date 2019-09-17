package com.example.cart.service;

import com.example.cart.external.catalog.CatalogResource;
import com.example.cart.external.catalog.model.Product;
import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;
import com.example.cart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ListIterator;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository repository;

    @Autowired
    private CatalogResource catalog;

    static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    static final String ANONYMOUS_ID = "id-anonymous";

    public Cart initCart(String customerId) {
        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        initCustomerId(cart, customerId);
        repository.save(cart);
        return cart;
    }

    private void initCustomerId(Cart cart, String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            cart.setCustomerId(ANONYMOUS_ID);
        } else {
            cart.setCustomerId(customerId);
        }
    }

    public Optional<Cart> getCart(String cardId) {
        return repository.findById(cardId);
    }

    public Optional<Cart> addProduct(String cartId, String productId) {
        Optional<Cart> opCart = repository.findById(cartId);
        opCart.ifPresentOrElse(
                cart -> addProductToCart(cart, retrieveProductTemplate(productId)),
                () -> log.warn("Cart not found id {}", cartId));
        return opCart;
    }

    protected void addProductToCart(final Cart cart, final Product product) {
        cart.getCartItems().stream()
                .filter(i -> i.getProductId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        i -> updateTotalItem(i, i.getQuantity() + 1),
                        () -> cart.addCartItem(newCartItem(product))
                );
        increaseTotalCart(cart, product);
        repository.save(cart);
    }

    private CartItem newCartItem(Product product) {
        return new CartItem(product.getId(), product.getName(), product.getPrice());
    }

    private void updateTotalItem(CartItem item, int newQuantity) {
        item.setQuantity(newQuantity);
        item.setTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
//        item.setTotal(item.getPrice().setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2,RoundingMode.HALF_UP));

    }

    public Optional<Cart> removeProduct(String cartId, String productId) {
        Optional<Cart> opCart = repository.findById(cartId);
        opCart.ifPresentOrElse(cart -> removeProduct(cart, productId), () -> log.warn("Cart not found id {}", cartId));
        return opCart;
    }

    private void decreaseTotalCart(Cart cart, CartItem cartItem) {
        cart.setTotal(cart.getTotal().subtract(cartItem.getPrice()));
    }

    private void increaseTotalCart(Cart cart, Product product) {
        cart.setTotal(cart.getTotal().add(product.getPrice()));
    }

    protected void removeProduct(Cart cart, String productId) {
        ListIterator<CartItem> it = cart.getCartItems().listIterator();
        while(it.hasNext()) {
            CartItem item = it.next();
            if (productId.equals(item.getProductId())) {
                decreaseTotalCart(cart, item);
                if (item.getQuantity() == 1) {
                    it.remove();
                } else {
                    updateTotalItem(item, item.getQuantity() - 1);
                }
                repository.save(cart);
                break;
            }
        }
    }

    private Product retrieveProductTemplate(String productId) {
        return catalog.getProduct(productId);
    }

    public Optional<Cart> placeOrder(String id) {
        Optional<Cart> opCart = repository.findById(id);
        //if cart exists
        //check if customer created if not then ask to create (customer creation will update customer id in cart )
        //create order based on cart and submit
        //update cart as checked out or remove cart from redis repository.delete(opCart.get());
        return opCart;
    }


}
