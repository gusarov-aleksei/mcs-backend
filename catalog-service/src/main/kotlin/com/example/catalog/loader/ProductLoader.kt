package com.example.catalog.loader

import com.example.catalog.model.Entity
import com.example.catalog.model.Product
import org.springframework.stereotype.Service
import java.io.InputStream

interface Loader {

    /**
     * Parses csv input stream, creates entity from line using transformer function
     * @return Pair of map generated entities (id->entity) and list of failed, unprocessed csv lines
     */
    fun <T> loadEntities(inputStream: InputStream, validator: Regex, transformer: (List<String>) -> T):  Pair<Map<String, T>, List<String>>
            where T: Entity {
        val (validLines, invalid) = inputStream.use {
            it.bufferedReader().lineSequence().drop(1) .partition{ s -> s.matches(validator)}
        }
        val idToEntity = validLines.map { line -> line.split(";") }
                .map { props -> transformer(props) }
                .associateBy{e -> e.id}
        return Pair(idToEntity, invalid)
    }

}

@Service
class ProductLoader : Loader {

    fun loadProducts(inputStream: InputStream) : Pair<Map<String, Product>, List<String>> {
        return loadEntities(inputStream, lineTemplate, productTransformer)
    }

    companion object{
        val productTransformer = {props:List<String> -> Product(props[0], props[1], props[2].toDouble(), props[3])}
        const val idPattern = "[a-zA-Z0-9_-]+"
        const val namePattern = "[a-zA-Z0-9_ -/=]+"
        const val pricePattern = "[0-9]+([.][0-9]*)?"
        const val descPattern = "[a-zA-Z0-9_ -/=]*"
        val lineTemplate = Regex("^$idPattern;$namePattern;$pricePattern;$descPattern$")
    }

}