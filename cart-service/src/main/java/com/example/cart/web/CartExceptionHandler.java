package com.example.cart.web;

import com.example.cart.external.catalog.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.ConnectException;
import java.util.Map;

@ControllerAdvice
public class CartExceptionHandler {

    Logger log = LoggerFactory.getLogger(CartExceptionHandler.class);

    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<Map> handleRuntimeException(ConnectException e) {
        log.error("Error on connection to resource: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("ERR_CODE", "ERR-CART-001", "ERR_DETAIL", e.getMessage()));
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<Map> handleRuntimeException(RedisConnectionFailureException e) {
        log.error("ERR-CART-002", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("ERR_CODE", "ERR-CART-002", "ERR_DETAIL", e.getMessage()));
    }

    @ExceptionHandler(ResourceException.class)
    public ResponseEntity<String> handleResourceException(ResourceException e) {
        log.error("Error on getting resource: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }


}
