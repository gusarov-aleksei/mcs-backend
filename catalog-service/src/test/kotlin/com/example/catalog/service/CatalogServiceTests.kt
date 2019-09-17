package com.example.catalog.service

import com.example.catalog.loader.ProductLoader
import com.example.catalog.model.Product
import com.example.catalog.repository.ProductRepository
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.ByteArrayInputStream
import java.util.*

class CatalogServiceTests : FeatureSpec (){
    //Product Loader with will load defined map of Products idToProductMap
    val idToProductMap = mapOf("1" to Product("1", "Product 1", 5.09, "Description 1"),
            "2" to Product("2", "Product 2", 6.29, "Description 2"),
            "3" to Product("3", "Product 3", 7.82, "Description 3"))

    val productLoader = mockk<ProductLoader> {
        every { loadProducts(any())} returns Pair(idToProductMap, listOf())
    }

    val productRepository = mockk<ProductRepository> {
        every { findById("2") } returns Optional.of(Product("2", "Product 2", 6.29, "Description 2"))
        every { findById("5") } returns Optional.empty()
        every { saveAll(idToProductMap.values) } returns idToProductMap.values
        every { findAll() } returns idToProductMap.values
    }

    val inputStream = ByteArrayInputStream.nullInputStream()

    val catalogService = CatalogServiceImpl(productLoader, productRepository)

    init {

        feature("Product loading from csv file into repository") {

            scenario("should call inputStream to Products transformation and save Products to repository") {
                catalogService.loadProductsFromFile(inputStream)

                verify {
                    productLoader.loadProducts(inputStream)
                }
                verify {
                    productRepository.saveAll(idToProductMap.values)
                }
            }
        }

        feature("Providing information about all Products") {

            scenario("getProducts should get Products existing in catalog(repository)") {
                catalogService.getProducts() shouldContainExactly idToProductMap.values
                verify {
                    productRepository.findAll()
                }
            }
        }

        feature("Getting information about Product by id") {

            scenario("should get Optional.empty by id when Product doesn't exist in catalog") {
                val catalogService = CatalogServiceImpl(productLoader, productRepository)

                catalogService.getProduct("5") shouldBe Optional.empty()
                verify {
                    productRepository.findById("5")
                }
            }

            scenario("should get Optional(Product) by id when Product exists in catalog(repository)") {
                catalogService.getProduct("2") shouldBe Optional.of(Product("2", "Product 2", 6.29, "Description 2"))
                verify {
                    productRepository.findById("2")
                }
            }
        }
    }

}