package com.example.catalog.web

import com.example.catalog.model.Product
import com.example.catalog.service.CatalogService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*


@RestController
@RequestMapping("/api/catalog")
class CatalogController {


    val log = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var catalogService: CatalogService

    @GetMapping
    fun getProducts(): ResponseEntity<Collection<Product>> {
        return ResponseEntity.ok(catalogService.getProducts())
    }

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: String): ResponseEntity<Any> {
        return toResponse(catalogService.getProduct(id))
    }

    //example: curl -F 'file=@./catalog/src/main/resources/products.csv' http://localhost:8091/api/catalog/upload
    @PostMapping("/upload")
    fun uploadProducts(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        try {
            catalogService.loadProductsFromFile(file.inputStream)
            return ResponseEntity("Uploading finished", HttpStatus.OK) //TODO return number of loaded products
        } catch (e: Exception) {
            log.error("Error while uploading file: ", e)
            return ResponseEntity(mapOf("ERR_CODE" to "ERR-CATALOG-100"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    fun toResponse(opProduct : Optional<Product>): ResponseEntity<Any> = when { //TODO return as is instead of bad response
        opProduct.isPresent -> ResponseEntity.ok(opProduct.get())
        else -> ResponseEntity(mapOf("ERR_CODE" to "ERR-CATALOG-001"), HttpStatus.NOT_FOUND)
    }


}
