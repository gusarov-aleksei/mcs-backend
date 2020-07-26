package com.example.test

import com.example.test.utils.KafkaClient
import com.example.test.utils.orderConsumerProperties
import io.kotlintest.Spec
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FeatureSpec
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.parseMap
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.kafka.clients.consumer.ConsumerRecord
import redis.clients.jedis.Jedis
import java.io.File

/*
    Test performs and validates scenarios of Cart related features. That are http calls to remote Cart Service.
 */
@ImplicitReflectionSerializer
abstract class AbstractCartTest : FeatureSpec(){

    val client = autoClose(HttpClients.createDefault())//HttpClientUtils.closeQuietly(client)

    val json = Json(JsonConfiguration.Stable)

    val redis = autoClose(Jedis())

    //System.setProperty("jdk.httpclient.HttpClient.log", "all")

    private fun cleanUpCartRepository(id: String) {
        redis.del("Cart:$id")
    }

    private fun loadProducts() {
        val httpPost = HttpPost("${catalogBaseUrl()}/upload")
        httpPost.entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", File(productsFile()))
                .build()
        return client.execute(httpPost).use {
            resp -> resp.statusLine.statusCode shouldBe 200
            EntityUtils.toString(resp.entity)
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        println("beforeSpec carts in repository : ${redis.keys("Cart:*").size}\n")
        //println("beforeSpec products in repository : ${redis.keys("Product:*")}\n")
        cleanUpCatalog()
        //println("beforeSpec products in repository : ${redis.keys("Product:*")}\n")
        loadProducts()
    }

    private fun cleanUpCatalog() {
        redis.keys("Product:*").forEach {
            redis.del(it)
        }
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        println("afterSpec carts in repository : ${redis.keys("Cart:*").size}\n")
        //println("afterSpec products in repository : ${redis.keys("Product:*")}\n")
        cleanUpCatalog()
        //println("afterSpec products in repository : ${redis.keys("Product:*")}\n")
    }

    /**
     *     Perform GET call to Cart service, validate if OK and returns updated Cart. Method for reducing boilerplate code.
     *
     *     extendedUrl - additional part in url will be appended to url call.
     *     Depending on extendedUrl call will be routed to particular logic on server
     *
     */
    fun useCart(extendedUrl : String = "") : Cart {
        return client.execute(HttpGet("${cartBaseUrl()}/${extendedUrl}")).use {
            it.statusLine.statusCode shouldBe 200
            json.parse(Cart.serializer(), EntityUtils.toString(it.entity))
        }
    }
}

/*
    Test performs and validates scenarios of Cart related features. That are http calls to remote Cart Service.
 */
@ImplicitReflectionSerializer
class CartTest : AbstractCartTest() {

    private fun cleanUpCartRepository(id: String) {
        redis.del("Cart:$id")
    }

    private fun cleanUpCatalog() {
        redis.keys("Product:*").forEach {
            redis.del(it)
        }
    }

    init {

        feature("New Cart initialization") {
            scenario("Call Cart service to init and get Cart representation") {
                val cart = useCart()

                cart shouldNotBe null
                cart.id shouldNotBe null
                cart.cartItems shouldBe emptyList()
                cart.total shouldBe  0.0
                cart.customerId shouldBe "id-anonymous"

                cleanUpCartRepository(cart.id)
            }

            scenario("Call Cart service to init and get Cart representation for non-anonymous") {
                val cart = useCart("?customerId=1")

                cart shouldNotBe null
                cart.id shouldNotBe null
                cart.cartItems shouldBe emptyList()
                cart.total shouldBe  0.0
                cart.customerId shouldBe "1"

                cleanUpCartRepository(cart.id)
            }
        }

        feature("Cart getting by id") {
            scenario("Cart getting by id (after Cart initialization)") {
                val cart = useCart()//init Cart

                val persistedCart = useCart("${cart.id}")//get Cart by id

                persistedCart shouldNotBe null
                persistedCart.id shouldBe cart.id
                persistedCart.cartItems shouldBe cart.cartItems
                persistedCart.total shouldBe cart.total
                persistedCart.customerId shouldBe cart.customerId

                cleanUpCartRepository(cart.id)
            }

            scenario("Cart getting by id if Cart doesn't exits") {
                val errors = client.execute(HttpGet("${cartBaseUrl()}/not-existing-id")).use {
                    it.statusLine.statusCode shouldBe 404
                    json.parseMap<String, String>(EntityUtils.toString(it.entity))
                }
                errors shouldContainExactly mapOf("ERR_CODE" to "ERR-CART-003")
            }
        }

        feature("Cart removing by id") {
            scenario("Cart removing from cart service") {
                //TODO not implemented yet
            }
        }

        feature("Add Product to Cart") {
            scenario("Add existing in catalog Product to Cart") {
                var cart = useCart()

                cart = client.execute(HttpGet("${cartBaseUrl()}/${cart.id}/add?productId=1")).use {
                    it.statusLine.statusCode shouldBe 200
                    val body = EntityUtils.toString(it.entity)
                    json.parse(Cart.serializer(), body)
                }

                cart.total shouldBe 10.32
                cart.cartItems shouldContainExactlyInAnyOrder
                    listOf(CartItem(productId="1", name="Hammer", price=10.32, quantity=1, total=10.32))
                cart.customerId shouldBe "id-anonymous"

                cleanUpCartRepository(cart.id)
            }

            scenario("Add some Products to Cart") {
                var cart = useCart()
                cart = useCart("${cart.id}/add?productId=1")
                cart = useCart("${cart.id}/add?productId=2")
                cart = useCart("${cart.id}/add?productId=2")

                cart.total shouldBe 27.22
                cart.cartItems shouldContainExactlyInAnyOrder
                        listOf(CartItem(productId="1", name="Hammer", price=10.32, quantity=1, total=10.32),
                                CartItem(productId="2", name="Screwdriver", price=8.45, quantity=2, total=16.9))
                cart.customerId shouldBe "id-anonymous"

                cleanUpCartRepository(cart.id)
            }

            scenario("Add non-existing in catalog Product to Cart") {
                val cart = useCart()
                val errors = client.execute(HttpGet("${cartBaseUrl()}/${cart.id}/add?productId=non-existing-id")).use {
                    it.statusLine.statusCode shouldBe 500 //TODO change to 404 because this is expected error
                    json.parseMap<String, String>(EntityUtils.toString(it.entity))
                }
                errors shouldContainExactly mapOf("ERR_CODE" to "ERR-CATALOG-001")

                cleanUpCartRepository(cart.id)
            }
        }

        feature("Remove Product from Cart") {
            scenario("Remove existing Product from Cart") {
                var cart = useCart() //init cart
                cart = useCart("${cart.id}/add?productId=1") //add product to cart
                cart = useCart("${cart.id}/remove?productId=1")//remove product from cart

                cart shouldNotBe null
                cart.cartItems shouldBe emptyList()
                cart.total shouldBe 0.0

                cleanUpCartRepository(cart.id)
            }

            scenario("Remove non-existing Product from Cart. No error expected") {
                var cart = useCart() //init cart

                val cart2 = useCart("${cart.id}/remove?productId=non-existing-id")

                cart2 shouldNotBe null
                cart2.cartItems shouldBe emptyList()
                cart2.total shouldBe 0.0

                cleanUpCartRepository(cart.id)
            }
        }
        //TODO to add test with timeout simulation at catalog service service side.
        // cart -> catalog -> catalog do something a long time ->  timeout exception at cart side
    }
}

@ImplicitReflectionSerializer
class CheckoutCartTest : AbstractCartTest() {
    init {
        feature("Checkout Cart") {
            scenario("Successful checkout of existing Cart with some Product") {
                var cart = useCart() //init cart
                cart = useCart("${cart.id}/add?productId=1") //add Product to Cart
                println(cart)
                useCart("${cart.id}/checkout") //check out : place an Order
                // validate OrderEvent in kafka topic
                val kafkaClient = KafkaClient<OrderEvent>(orderConsumerProperties(), listOf("OrderEventTopic"));
                // consume event and close connection to kafka
                var eventConsumed = false; // to check if event was consumed
                kafkaClient.use {
                    repeat(3) {
                        kafkaClient.consume {
                            assertOrderEvent(it)
                            eventConsumed = true;
                        }
                    }
                }
                eventConsumed shouldBe true
            }
        }
    }
}

fun assertOrderEvent(record: ConsumerRecord<String, OrderEvent>) {
    //println("offset = ${record.offset()}, key = ${record.key()}, value = ${record.value()}")
    when (val value = record.value()) {
        is OrderEvent.Create -> {
            value.shouldNotBeNull()
            value.customerId shouldBe "id-anonymous"
            value.totalToPay shouldBe "10.32"
            value.items shouldContainExactlyInAnyOrder listOf(Item("1", "1", "10.32", "10.32"))
        }
        is OrderEvent.Pay -> {}
        is OrderEvent.Complete -> {}
    }
}