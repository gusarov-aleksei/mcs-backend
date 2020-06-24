package com.example.cart.service;

import com.example.cart.external.catalog.CatalogResource;
import com.example.cart.external.catalog.ResourceException;
import com.example.cart.external.catalog.model.Product;
import com.example.cart.external.order.CartToOrderConverter;
import com.example.cart.external.order.OrderProducer;
import com.example.cart.external.order.model.Item;
import com.example.cart.external.order.model.OrderEvent;
import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;
import com.example.cart.repository.CartRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CatalogResource catalog;

    @Mock
    private Logger log;

    @Mock
    RestTemplate restTemplate;

    @Mock
    CartToOrderConverter cartToOrderConverter;

    @Mock
    OrderProducer orderProducer;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    public void testInitCart_whenInitCartIsCalled_thenCartIsCreatedAndPutIntoRepository() {
        Cart cart = cartService.initCart("1");

        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    public void testInitCart_whenInitCartIsCalled_thenCartIsCreatedWithSomeParameters() {
        Cart cart = cartService.initCart("100");

        assertThat(cart).isNotNull();
        assertThat(cart.getCustomerId()).isEqualTo("100");
        //assertThat(cart.getId()).isNotNull();
        assertThat(cart.getTotal()).isEqualTo(BigDecimal.ZERO);
        assertThat(cart.getCartItems()).isNotNull();
        assertThat(cart.getCartItems()).isEmpty();
    }

    @Test
    public void testInitCart_whenInitCartWithNullCustomerId_thenCartIsCreatedWithAnonymousCustomerId() {
        Cart cart = cartService.initCart(null);
        assertThat(cart.getCustomerId()).isEqualTo(CartServiceImpl.ANONYMOUS_ID);
    }

    @Test
    public void testInitCart_whenInitCartWithEmptyCustomerId_thenCartIsCreatedWithAnonymousCustomerId() {
        Cart cart = cartService.initCart("");
        assertThat(cart.getCustomerId()).isEqualTo(CartServiceImpl.ANONYMOUS_ID);
    }

    @Test
    public void testGetCart_whenGetCartIsCalled_thenMethodOfRepositoryIsCalled() {
        doReturn(Optional.of(new Cart())).when(cartRepository).findById("1");

        cartService.getCart("1");

        verify(cartRepository, times(1)).findById("1");
    }

    @Test
    public void testAddCartItem_whenCartNotExists_thenNothingHappensAndReturnsOptional() {
        doReturn(Optional.empty()).when(cartRepository).findById("1");

        Optional<Cart> opCart = cartService.addProduct("1", "2");

        verify(catalog, times(0)).getProduct("2");
        assertThat(opCart.isEmpty()).isTrue();
        //verify(log, times(1)).warn("Cart not found id {}", "1");
    }

    //TODO rewrite this test with exception throwing
    //@Test
    public void testAddCartItem_whenProductNotExists_thenErrorIsTranslatedToResponse() {
        //doThrow(ResourceException.class).when(restTemplate).getForObject(any(), Product.class);//here will be exception in real life.
        when(restTemplate.getForObject(any(), Product.class)).thenThrow(new ResourceException(HttpStatus.NOT_FOUND, "Body"));
        Cart cart = new Cart();
        doReturn(Optional.of(cart)).when(cartRepository).findById("1");

        Optional<Cart> opCart = cartService.addProduct("1", "2");

        verify(catalog, times(1)).getProduct("2");
        assertThat(opCart.isPresent()).isTrue();
        assertThat(opCart.get()).isEqualTo(cart);
        //verify(log, times(1)).warn("Product not found id {}", "2");
    }


    @Test
    public void testAddCartItem_whenCartAndProductExist_thenAddProductToCartLogicIsCalled() {
        Cart cart = new Cart();
        Product product = new Product("2", "Product 1", new BigDecimal("10.55"));
        doReturn(Optional.of(cart)).when(cartRepository).findById("1");
        doReturn(product).when(catalog).getProduct("2");
        var spyCartService = Mockito.spy(cartService);

        spyCartService.addProduct("1", "2");

        verify(spyCartService).addProductToCart(cart, product);
        verify(cartRepository).save(cart);
    }

    @Test
    public void testAddCartItem_whenCartItemNotExists_thenItIsCreatedAndFilledWithParametersFromCatalogEntity() {
        Cart cart = new Cart();
        Product product = new Product("2", "Product 1", new BigDecimal("10.55"));
        doReturn(Optional.of(cart)).when(cartRepository).findById("1");
        doReturn(product).when(catalog).getProduct("2");

        Optional<Cart> opCart = cartService.addProduct("1", "2");

        assertThat(opCart).hasValueSatisfying(actualCart -> {
            assertThat(actualCart.getCartItems()).containsExactlyInAnyOrder(new CartItem("2", "Product 1", new BigDecimal("10.55")));
            assertThat(actualCart.getTotal()).isEqualTo(new BigDecimal("10.55"));
        });
        verify(cartRepository).save(cart);
    }

    @Test
    public void testAddCartItem_whenCartItemExists_thenItIsUpdatedWithNewQuantityAndTotalPrice() {
        Cart cart = new Cart();
        cart.addCartItem(new CartItem("2", "Product 1", new BigDecimal(10.55)));
        Product p = new Product("2", "Product 1", new BigDecimal(10.55));
        doReturn(Optional.of(cart)).when(cartRepository).findById("1");
        doReturn(p).when(catalog).getProduct("2");

        Optional<Cart> opCart = cartService.addProduct("1", "2");

        assertThat(opCart).hasValueSatisfying(actualCart -> {
            assertThat(actualCart.getCartItems().size()).isEqualTo(1);
            CartItem actualItem = actualCart.getCartItems().get(0);
            assertThat(actualItem.getProductId()).isEqualTo("2");
            assertThat(actualItem.getQuantity()).isEqualTo(2);
            assertThat(actualItem.getTotal().compareTo(new BigDecimal("21.10")) == 0);
            assertThat(actualItem.getTotal().compareTo(new BigDecimal("21.10")) == 0);
        });
        verify(cartRepository).save(cart);
    }

    @Test
    public void testRemoveCartItem_whenCartNotExists_thenNothingHappensAndReturnsEmptyOptional() {
        doReturn(Optional.empty()).when(cartRepository).findById("1");

        Optional<Cart> opCart = cartService.removeProduct("1", "2");

        assertThat(opCart.isEmpty()).isTrue();
        verify(cartRepository, times(0)).save(any(Cart.class));
        //verify(log).warn("Cart not found id {}", "1");
    }

    @Test
    public void testRemoveCartItem_whenProductWithQuantityOne_thenCartItemIsRemoved() {
        Cart cart = new Cart();
        cart.addCartItem(new CartItem("2", "Product 1", new BigDecimal("10.55")));
        doReturn(Optional.of(cart)).when(cartRepository).findById("1");

        Optional<Cart> opCart = cartService.removeProduct("1", "2");

        assertThat(opCart).hasValueSatisfying(actualCart -> {
            assertThat(actualCart.getCartItems().size()).isEqualTo(0);
        });
        verify(cartRepository).save(cart);
    }

    @Test
    public void testRemoveCartItem_whenProductWasNotAdded_thenNothingIsRemoved() {
        Cart cart = new Cart();
        doReturn(Optional.of(cart)).when(cartRepository).findById("1");

        cartService.removeProduct("1", "2");

        verify(cartRepository, times(0)).save(cart);
    }

    @Test
    public void testRemoveCartItem_whenProductWithQuantityTwoOrMore_thenQuantityIsDecreased() {
        Cart cart = new Cart();
        CartItem item = new CartItem("2", "Product 1", new BigDecimal("10.55"));
        item.setQuantity(2);
        item.setTotal(new BigDecimal("21.10"));
        cart.addCartItem(item);

        doReturn(Optional.of(cart)).when(cartRepository).findById("1");

        Optional<Cart> opCart = cartService.removeProduct("1", "2");

        assertThat(opCart).hasValueSatisfying(actualCart -> {
            assertThat(actualCart.getCartItems().size()).isEqualTo(1);
            CartItem actualItem = actualCart.getCartItems().get(0);
            assertThat(actualItem.getQuantity()).isEqualTo(1);
            assertThat(actualItem.getTotal()).isEqualTo(BigDecimal.valueOf(10.55));
        });
    }

    /*
        {"id":"13b52036-62f4-4268-b215-acfd3818cb1b","customerId":"id-anonymous","total":78.82000000000001,"cartItems":[{"productId":"1","price":10.32,"quantity":6,"total":61.92},{"productId":"2","price":8.45,"quantity":2,"total":16.9}]}
    */
    @Test
    public void testAddProduct_whenProductsAddedManyTimes_thenPricesHaveTwoFractionalDigitsOnly() {
        //check if total not "total":78.82000000000001 as in case of double
        var cart = new Cart();
        Product p1 = new Product("1", "Hammer", BigDecimal.valueOf(10.32));
        Product p2 = new Product("2", "Screwdriver", BigDecimal.valueOf(8.45));

        doReturn(Optional.of(cart)).when(cartRepository).findById("1");
        doReturn(p1).when(catalog).getProduct("1");
        doReturn(p2).when(catalog).getProduct("2");

        cartService.addProduct("1", "2");
        for (int i = 0; i<6; i++ )
            cartService.addProduct("1", "1");

        Optional<Cart> opCart = cartService.addProduct("1", "2");

        var expected1 = new CartItem("2", "Screwdriver", BigDecimal.valueOf(8.45), 2, BigDecimal.valueOf(1690,2));
        var expected2 = new CartItem("1", "Hammer", BigDecimal.valueOf(10.32), 6, BigDecimal.valueOf(61.92));
        assertThat(opCart).hasValueSatisfying(actualCart -> {
            assertThat(actualCart.getCartItems()).containsExactlyInAnyOrder(expected1, expected2);
            assertThat(actualCart.getTotal()).isEqualTo(new BigDecimal("78.82"));
        });
        verify(cartRepository, times(8)).save(cart);
    }

    @Test
    public void testAddProductsAndRemoveProducts_whenAddAndRemoveProducts_thenCartIsReturnedInInitialState() {

        Cart cart = new Cart();
        Product p1 = new Product("1", "Hammer", new BigDecimal(10.32));
        Product p2 = new Product("2", "Screwdriver", new BigDecimal(8.45));

        doReturn(Optional.of(cart)).when(cartRepository).findById("1");
        doReturn(p1).when(catalog).getProduct("1");
        doReturn(p2).when(catalog).getProduct("2");

        for (int i = 0; i<100; i++ )
            cartService.addProduct("1", "1");

        for (int i = 0; i<50; i++ )
            cartService.addProduct("1", "2");

        for (int i = 0; i<100; i++ )
            cartService.removeProduct("1", "1");

        for (int i = 0; i<49; i++ )
            cartService.removeProduct("1", "2");

        Optional<Cart> opCart = cartService.removeProduct("1", "2");

        assertThat(opCart).hasValueSatisfying(actualCart -> {
            assertThat(actualCart.getCartItems()).isEmpty();
            assertThat(actualCart.getTotal().compareTo(new BigDecimal(0)) == 0);
        });
        verify(cartRepository, times(300)).save(cart);
    }

    private OrderEvent.Create createOrderEvent =
            new OrderEvent.Create("customer-1", "99.99",
                    List.of(new Item("product-1", "1", "99.99","99.99")));

    @Test
    public void testPlaceOrder_shouldCallCartToOrderConverter_whenCartExists() {
        Cart cart = new Cart("cart-1");
        doReturn(Optional.of(cart)).when(cartRepository).findById("cart-1");
        doReturn(createOrderEvent).when(cartToOrderConverter).convert(cart);

        cartService.placeOrder("cart-1");

        verify(cartToOrderConverter, times(1)).convert(cart);
    }

    @Test
    public void testPlaceOrder_doesNotCallCartToOrderConverter_whenNoCartExist() {
        doReturn(Optional.empty()).when(cartRepository).findById("cart-1");
        cartService.placeOrder("cart-1");

        verify(cartToOrderConverter, times(0)).convert(any(Cart.class));
    }

    @Test
    public void testPlaceOrder_shouldPlaceOrder_whenCartExists() {
        Cart cart = new Cart("cart-1");
        doReturn(Optional.of(cart)).when(cartRepository).findById("cart-1");
        doReturn(createOrderEvent).when(cartToOrderConverter).convert(cart);

        cartService.placeOrder("cart-1");

        verify(orderProducer, times(1)).placeOrder(createOrderEvent);
    }
}
