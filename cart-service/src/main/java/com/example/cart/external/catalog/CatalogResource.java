package com.example.cart.external.catalog;

import com.example.cart.external.catalog.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CatalogResource {

    private RestTemplate restTemplate;

    @Autowired
    public CatalogResource(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${catalog.url}")
    private String catalogUrl;
    //TODO timeout check
    public Product getProduct(String id) {
        return restTemplate.getForObject(catalogUrl + id, Product.class);
    }

}
