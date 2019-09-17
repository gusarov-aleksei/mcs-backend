package com.example.catalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class CatalogApplication

fun main(args: Array<String>) {
	runApplication<CatalogApplication>(*args)
}
