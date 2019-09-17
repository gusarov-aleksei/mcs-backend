package com.example.cart;

import com.example.cart.external.catalog.ResourceErrorHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class CartApplication {

    @Bean
    public WebClient.Builder getWebClientBuilder() {
        return WebClient.builder();
    }
    //TODO check name of method
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate template = new RestTemplate();
        template.setErrorHandler(new ResourceErrorHandler());
        return template;
    }

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }

}
