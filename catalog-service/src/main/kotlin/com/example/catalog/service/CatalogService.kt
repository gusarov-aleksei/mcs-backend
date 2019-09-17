package com.example.catalog.service

import com.example.catalog.model.Product
import java.io.InputStream
import java.util.*

interface CatalogService {
    fun getProduct(id: String): Optional<Product>
    fun getProducts(): Collection<Product>
    fun loadProductsFromFile(inputStream: InputStream)
}