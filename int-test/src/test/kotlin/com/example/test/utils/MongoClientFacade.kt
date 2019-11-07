package com.example.test.utils

import com.mongodb.MongoClient
import org.bson.Document
import org.bson.types.ObjectId
import java.io.Closeable

class MongoClientFacade : Closeable {
    //TODO move to config
    private val client = MongoClient("localhost", 27017)

    fun getCustomerById(id: String) : Document? {
        return client.getDatabase("customer-service")
                .getCollection("customer")
                .find(Document("_id", ObjectId(id))).first()
    }

    fun removeCustomerById(id: String) {
        client.getDatabase("customer-service")
                .getCollection("customer")
                .findOneAndDelete(Document("_id", ObjectId(id)))
    }

    fun removeAllCustomers() : Long {
        return client.getDatabase("customer-service")
                .getCollection("customer")
                .deleteMany(Document()).deletedCount
    }

    fun removeCustomersByIds(ids : List<String>) : Long {
        val query = Document("_id", Document("\$in", ids.map { id ->  ObjectId(id)}))
        return client.getDatabase("customer-service")
                .getCollection("customer")
                .deleteMany(query).deletedCount
    }

    fun getCountOfCustomers() : Long {
        return client.getDatabase("customer-service")
                .getCollection("customer")
                .countDocuments()
    }

    override fun close() {
        client.close()
    }
}