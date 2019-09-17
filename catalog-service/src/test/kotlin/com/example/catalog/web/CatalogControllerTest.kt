package com.example.catalog.web

import com.example.catalog.model.Product
import com.example.catalog.service.CatalogService
import io.kotlintest.Spec
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.spring.SpringListener
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import java.io.InputStream
import java.util.*

//@SpringBootTest
//@RunWith(MockJUnitRunner::class)
class CatalogControllerTest : StringSpec(){

    override fun listeners(): List<TestListener> {
        return listOf(SpringListener)
    }

    @InjectMockKs
    private lateinit var catalogController: CatalogController

    @MockK
    lateinit var catalogService: CatalogService

    //@MockK
    //lateinit var log: Logger

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        MockKAnnotations.init(this)
    }

    init{
        "calls service and returns OK if no exception was thrown" {

            val mockFile = mockk<MockMultipartFile>()
            val mockInputStream = mockk<InputStream>()
            every { mockFile.inputStream } returns mockInputStream

            every {
                catalogService.loadProductsFromFile(mockInputStream)
            } returns Unit

            catalogController.uploadProducts(mockFile) shouldBe
                    ResponseEntity("Uploading finished", HttpStatus.OK)

            verify {
                catalogService.loadProductsFromFile(mockInputStream)
            }
        }

        "calls service and returns 500 if exception was thrown while loading" {

            val mockFile = mockk<MockMultipartFile>()
            val mockInputStream = mockk<InputStream>()
            every { mockFile.inputStream } returns mockInputStream
            every { catalogService.loadProductsFromFile(any()) } throws Exception("Expected exception! Don't worry..")

            //TODO verify log exception
            catalogController.uploadProducts(mockFile) shouldBe
                    ResponseEntity(mapOf("ERR_CODE" to "ERR-CATALOG-100"), HttpStatus.INTERNAL_SERVER_ERROR)

            verify {
                catalogService.loadProductsFromFile(mockInputStream)
            }
            /*verify {
                log.error("Error while uploading file: ", any())
            }*/
        }

        "get Product by id should return Product when it exits in catalog" {

            every{
                catalogService.getProduct("1")
            } returns Optional.of(Product("1", "Hammer", 10.32, "A tool with a heavy metal head"))

            catalogController.getProduct("1") shouldBe
                    ResponseEntity(
                            Product("1", "Hammer", 10.32, "A tool with a heavy metal head"),
                            HttpStatus.OK)
        }

        "get Product by id should not return Product when it doesn't exist in catalog" {

            every{
                catalogService.getProduct("some id")
            } returns Optional.empty()

            catalogController.getProduct("some id") shouldBe
                    ResponseEntity(mapOf("ERR_CODE" to "ERR-CATALOG-001"), HttpStatus.NOT_FOUND)
        }

    }

}