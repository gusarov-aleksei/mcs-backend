package com.example.test

import io.kotlintest.Spec
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import redis.clients.jedis.Jedis
import java.io.File


class CatalogTest : FeatureSpec() {

    private val client = autoClose(HttpClients.createDefault())

    val json = Json(JsonConfiguration.Stable)

    val redis = autoClose(Jedis())

    //System.setProperty("jdk.httpclient.HttpClient.log", "all")

    fun cleanUpCatalog() {
        redis.keys("Product:*").forEach {
            redis.del(it)
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        println("beforeSpec ${redis.keys("Product:*").size}\n")
        cleanUpCatalog()
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        //cleanUpCatalog()
        println("afterSpec ${redis.keys("Product:*").size}\n")
    }

    fun printKeysProducts() {
        println("${redis.keys("Product:*")}\n")
    }

    //catalog ids are hardcoded in csv for test simplicity
    init {
        feature("Uploading Products to Catalog service") {
            scenario("should upload via POST request to '/api/catalog/upload' with valid csv file of Products") {

                val httpPost = HttpPost("${catalogBaseUrl()}/upload")
                httpPost.entity = MultipartEntityBuilder.create()
                        .addBinaryBody("file", File(productsFile()))
                        .build()
                val response = client.execute(httpPost).use {
                    it.statusLine.statusCode shouldBe 200
                    EntityUtils.toString(it.entity)
                }
                response shouldBe "Uploading finished"
                redis.keys("Product:*").size shouldBe 3 //cvs file contains 3 Products
                printKeysProducts()
            }
        }

        //TODO test empty file
        //TODO test with corrupted file

        //add pagination to request
        feature("Getting Products from Catalog service") {
            scenario("get all available Products through GET /api/catalog/") {

                val products = client.execute(HttpGet("${catalogBaseUrl()}/")).use {
                    it.statusLine.statusCode shouldBe 200
                    json.parse(ProductSerializer.list,  EntityUtils.toString(it.entity))
                }

                products shouldContainExactlyInAnyOrder listOf(
                        Product("1", "Hammer", 10.32,"A tool with a heavy metal head"),
                        Product("2", "Screwdriver", 8.45,"A tool with a flattened, cross-shaped, or star-shaped tip"),
                        Product("3", "Pliers", 17.28,"Pincers with parallel, flat, and typically serrated surfaces"))
            }

            scenario("get Product by id through GET /api/catalog/id") {

                val product = client.execute(HttpGet("${catalogBaseUrl()}/1")).use {
                    it.statusLine.statusCode shouldBe 200
                    json.parse(ProductSerializer,  EntityUtils.toString(it.entity))
                }

                product shouldBe Product("1", "Hammer", 10.32,"A tool with a heavy metal head")
            }

        }
    }

}