package com.example.catalog.service

import com.example.catalog.loader.ProductLoader
import com.example.catalog.model.Product
import com.example.catalog.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.InputStream
import java.util.*

@Service
class CatalogServiceImpl: CatalogService {

    val log = LoggerFactory.getLogger(CatalogServiceImpl::class.java)

    @Autowired
    var repository: ProductRepository

    @Autowired
    var loader: ProductLoader

    constructor(loader: ProductLoader, repository: ProductRepository) {
        this.loader = loader
        this.repository = repository
    }

    override fun getProduct(id: String):Optional<Product> {
        return repository.findById(id)
    }

    override fun getProducts(): Collection<Product> {
        return repository.findAll().toList()
    }

    override fun loadProductsFromFile(inputStream: InputStream) {
        val (processed, failed) = loader.loadProducts(inputStream)
        repository.saveAll(processed.values)
        log.warn("Failed csv lines {}", failed)
        log.info("Results: {}", processed)
    }
}