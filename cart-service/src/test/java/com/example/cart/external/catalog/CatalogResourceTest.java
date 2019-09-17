package com.example.cart.external.catalog;

import com.example.cart.external.catalog.model.Product;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CatalogResourceTest {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CatalogResource catalogResource;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void testGetProduct_whenRemoteServerReturnsProductBody_thenProductEntityIsCreated() {

        String productBody = "{\"id\":\"1\",\"name\":\"Hammer\",\"price\":10.32,\"description\":\"A tool with a heavy metal head\"}";

        mockServer.expect(once(), requestTo("http://localhost:8091/api/catalog/1"))
                .andRespond(withSuccess(productBody, MediaType.APPLICATION_JSON));

        Product product = catalogResource.getProduct("1");
        Product expected = new Product("1", "Hammer", new BigDecimal("10.32"));

        mockServer.verify();
        assertEquals(expected, product);
    }

    @Test
    public void testGetProduct_whenRemoteServerReturnsError_thenExceptionIsThrown() {
        String errorBody = "{\"ERR_CODE\":\"ERR-CAT-0001\",\"ERR_DETAIL\":\"Product 5 not found\"}";
        mockServer.expect(once(), requestTo("http://localhost:8091/api/catalog/5"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body(errorBody).contentType(MediaType.APPLICATION_JSON));
        try {
            catalogResource.getProduct("5");
        }catch (RuntimeException ex) {
            assertEquals(ResourceException.class, ex.getClass());
            assertEquals(errorBody, ex.getMessage());
            assertEquals(HttpStatus.NOT_FOUND, ((ResourceException)ex).status);
        }

        mockServer.verify();
    }

    @Test
    public void testGetProduct_whenRemoteServerReturnsGeneric404_thenExceptionIsThrown() {
        String errorBody = "{\"timestamp\":\"2019-09-01T18:57:21.201+0000\",\"status\":404,\"error\":\"Not Found\",\"message\":\"No message available\",\"path\":\"/api/cart/42771ea9-aaa6-4305-9fe3-825c93dff0bb/add/1\"}";
        mockServer.expect(once(), requestTo("http://localhost:8091/api/catalog/5"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body(errorBody).contentType(MediaType.APPLICATION_JSON));
        try {
            catalogResource.getProduct("5");
        }catch (RuntimeException ex) {
            assertEquals(ResourceException.class, ex.getClass());
            assertEquals(errorBody, ex.getMessage());
            assertEquals(HttpStatus.NOT_FOUND, ((ResourceException)ex).status);
        }

        mockServer.verify();
    }
    //TODO add test: timeout validation
}
