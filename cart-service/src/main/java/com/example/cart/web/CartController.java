package com.example.cart.web;

import com.example.cart.model.Cart;
import com.example.cart.service.CartServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartServiceImpl cartService;

    @GetMapping("/")
    public ResponseEntity<Cart> initCart(@RequestParam(value = "customerId", required = false) String customerId) {
        //@Pattern(regexp = "^[a-zA-Z0-9_-]*$") - from zero to id
        return ResponseEntity.ok(cartService.initCart(customerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCartByCardId(@PathVariable String id) {
        //TODO to add validation and do it in code
        return toResponse(cartService.getCart(id));
    }

    @GetMapping("/{cartId}/add")
    public ResponseEntity<?> addProduct(@PathVariable String cartId,
                                        @RequestParam(value = "productId") String productId) {
        //TODO to add validation and do it in code
        return toResponse(cartService.addProduct(cartId, productId));
    }

    @GetMapping("/{id}/remove")
    public ResponseEntity<?> removeProduct(@PathVariable String id,
                                           @RequestParam(value = "productId") String productId) {
        //TODO to add validation and do it in code
        return toResponse(cartService.removeProduct(id, productId));
    }

    @GetMapping("/{id}/checkout")
    public ResponseEntity<?> placeOrder(@PathVariable String id) {
        //TODO to add validation and do it in code
        return toResponse(cartService.placeOrder(id));
    }

    /**
     * Wraps Cart with ResponseEntity.
     * HTTP 200 - body contains Cart
     * 4xx or 5xx - body contains information about error
     *
     * @param opCart
     * @return Response with Cart if OK or with error code if Not Found
     */
    protected ResponseEntity toResponse(Optional<Cart> opCart) {
        if (opCart.isPresent()) {
            return ResponseEntity.ok(opCart.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("ERR_CODE", "ERR-CART-003"));
        }
    }

    protected boolean validateInput(String s) {
        return s.matches("[a-zA-Z0-9_-]+");
    }

}
