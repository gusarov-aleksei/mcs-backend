package com.example.cart.external.catalog;

import org.springframework.http.HttpStatus;


public class ResourceException extends RuntimeException {

    public final HttpStatus status;

    public ResourceException(HttpStatus status, String body) {
        super(body);
        //this.body = body;
        this.status = status;
    }
}
