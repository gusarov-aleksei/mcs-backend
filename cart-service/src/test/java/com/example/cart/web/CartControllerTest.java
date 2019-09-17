package com.example.cart.web;

import com.example.cart.model.Cart;
import com.example.cart.service.CartServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CartControllerTest {

    @Mock
    CartServiceImpl cartService;

    @InjectMocks
    CartController cartController;

    Cart expectedCart = new Cart("10");//TODO why id=10 and not 1?

    @Before
    public void setup() {
        expectedCart.setCustomerId("1");
        doReturn(Optional.of(expectedCart)).when(cartService).getCart("1");
        doReturn(Optional.of(expectedCart)).when(cartService).addProduct("1","2");
    }

    @Test
    public void testInitCart_whenMethodCalled_thenReturnsResponseEntityWithCart() {
        doReturn(expectedCart).when(cartService).initCart("1");

        ResponseEntity responseEntity = cartController.initCart("1");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expectedCart);
    }

    @Test
    public void testGetCardById_whenCartExists_thenReturnsResponseEntityWithCart() {
        ResponseEntity responseEntity = cartController.getCartByCardId("1");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expectedCart);
    }

    @Test
    public void testGetCardById_whenCartDoesNotExist_thenReturnsResponseWith404() {
        ResponseEntity responseEntity = cartController.getCartByCardId("2");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isEqualTo(Map.of("ERR_CODE", "ERR-CART-003"));//check map
    }

    @Test
    public void testAddProduct_whenCartExists_thenReturnsResponseEntityWithCart() {
        ResponseEntity responseEntity = cartController.addProduct("1","2");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expectedCart);
    }

    @Test
    public void testAddProduct_whenCartDoesNotExist_thenReturnsResponseWith404() {
        ResponseEntity responseEntity = cartController.addProduct("2","2");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isEqualTo(Map.of("ERR_CODE", "ERR-CART-003"));//check map
    }

    @Test
    public void testRemoveProduct_whenCartExists_thenReturnsResponseEntityWithCart() {
        //given
        doReturn(Optional.of(expectedCart)).when(cartService).removeProduct("1","2");
        var spyCartController = Mockito.spy(cartController);
        //when
        spyCartController.removeProduct("1","2");
        //then
        verify(cartService).removeProduct("1", "2");
        verify(spyCartController).toResponse(Optional.of(expectedCart));
    }

    @Test
    public void testRemoveProduct_whenCartDoesNotExist_thenReturnsResponseWith404() {
        //given
        var spyCartController = Mockito.spy(cartController);
        //when
        spyCartController.removeProduct("2","2");
        //then
        verify(cartService).removeProduct("2", "2");
        verify(spyCartController).toResponse(Optional.empty());
    }

    @Test
    public void testToResponse_whenCartExists_thenReturnsResponseWithOK() {
        var responseEntity = cartController.toResponse(Optional.of(expectedCart));

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expectedCart);
    }

    @Test
    public void testToResponse_whenCartDoesNotExist_thenReturnsResponseWith404() {
        var responseEntity = cartController.toResponse(Optional.empty());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isEqualTo(Map.of("ERR_CODE", "ERR-CART-003"));//check map
    }

    @Test
    public void testValidate_whenInputAllowed_thenReturnsTrue() {
        var res = cartController.validateInput("88991f05-1f02-4ddb-ac61-15c1a66b5e91");
        assertThat(res).isEqualTo(true);
    }

    @Test
    public void testValidate_whenInputNotAllowed_thenReturnsFalse() {
        var res = cartController.validateInput("select * from customers");
        assertThat(res).isEqualTo(false);
    }

}
