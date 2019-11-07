package com.example.customer.repository

import com.example.customer.service.api.Customer
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import org.bson.Document
import org.bson.types.ObjectId

class EntityMapperTest : FeatureSpec() {

    val customerPropertiesFromMongoDb = mapOf(
            "_id" to ObjectId("5dbaff767d4ca12831865845"),
            "name" to "John Smith",
            "email" to "john.smith@mail.com",
            "dateOfBirth" to "1302-07-09")

    init {
        feature("Map Document to Customer entity") {
            scenario("should create Customer entity from Document object") {
                mapDocumentToCustomer(Document(customerPropertiesFromMongoDb)) shouldBe
                        Customer("5dbaff767d4ca12831865845", "John Smith", "john.smith@mail.com","1302-07-09")
            }
        }
    }

}