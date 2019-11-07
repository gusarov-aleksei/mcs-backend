package com.example.customer.repository

import com.example.customer.service.api.Customer
import com.example.customer.service.api.CustomerRequest
import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import com.mongodb.ServerCursor
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import io.mockk.every
import io.mockk.mockk
import org.bson.Document
import org.bson.types.ObjectId
import com.mongodb.client.MongoCursor


/**
 * Repository test using mock approach
 */
class CustomerRepositoryTest : FeatureSpec() {

    private val customerId = "5dbaff767d4ca12831865845"
    private val notExistedCustomerId = "5dbaff767d4ca12831865846"
    private val dbName = "customer-service"
    private val customerFields = mapOf("name" to "John Smith", "email" to "john.smith@mail.com","dateOfBirth" to "1302-07-09")
    private val customerPropertiesFromMongoDb = mapOf("_id" to ObjectId(customerId),"name" to "John Smith", "email" to "john.smith@mail.com","dateOfBirth" to "1302-07-09")
    private val updateOperationDocument = Document("\$set",  Document(customerFields))

    //this mock looks complicated. instead of this it is possible to use test with embedded DB using containers library
    private val mongoClientMock = mockk<MongoClient> { every { getDatabase(dbName) } returns
            mockk { every { getCollection("customer") } returns
                    mockk { every { find(Document("_id", ObjectId(customerId))) } returns
                                mockk { every { first() } returns Document(customerPropertiesFromMongoDb) }
                            every { find(org.bson.Document("_id", ObjectId(notExistedCustomerId)))} returns
                                mockk { every { first() } returns null }
                            every { insertOne(any()) } returns Unit
                            every { findOneAndUpdate(Document("_id", ObjectId(customerId)), updateOperationDocument) } returns Document(customerPropertiesFromMongoDb)
                            every { findOneAndUpdate(Document("_id", ObjectId(notExistedCustomerId)), updateOperationDocument) } returns null
                            every { find(Document("email", "john.smith@mail.com")) } returns
                                    mockk {
                                        every { limit(1) } returns mockk {
                                            every { iterator() } returns MongoCursorMock()
                                        }
                                    }
                            every { find(Document("email", "not_exists@mail.com")) } returns
                                mockk {
                                    every { limit(1) } returns mockk {
                                        every { iterator() } returns MongoCursorMock(0)
                                    }
                                }
                    }
            }
    }

    private val repository = CustomerRepositoryImpl(mongoClientMock, dbName)

    init {

        feature("Get Customer by id from repository"){

            scenario("if Customer exists in repository then mongodb driver returns document with Customer") {
                repository.getCustomerBy(customerId) shouldBe Customer(customerId,"John Smith","john.smith@mail.com","1302-07-09")
            }

            scenario("if Customer doesn't exist in repository then mongodb driver returns null") {
                repository.getCustomerBy(notExistedCustomerId) shouldBe null
            }
        }

        feature("Create Customer with parameters") {

            scenario("When create Customer is called then mongodb -> collection -> insertOne is performed and Customer id is generated") {
                val customer : Customer = repository.createCustomer(mapOf("name" to "John Smith", "email" to "john.smith@mail.com","dateOfBirth" to "1302-07-09"))

                customer.shouldNotBeNull()
                customer.name shouldBe "John Smith"
                customer.email shouldBe "john.smith@mail.com"
                customer.dateOfBirth shouldBe "1302-07-09"
            }
        }

        feature("Update Customer with parameters") {

            scenario("Update existed Customer with input parameters should call mongodb driver and return true") {
                val request = CustomerRequest(id = customerId, fields = customerFields)
                repository.updateCustomer(request) shouldBe true
            }

            scenario("Update non-existed Customer with input parameters should call mongodb driver and return false") {
                val request = CustomerRequest(id = notExistedCustomerId, fields = customerFields)
                repository.updateCustomer(request) shouldBe false
            }
        }

        feature("Check Customer existence by some parameter") {

            scenario("check Customer existing by its parameter name -> value. if it exists then true") {
                //existing customer is modeled in mongoClientMock
                repository.isCustomerExist("email", "john.smith@mail.com") shouldBe true
            }

            scenario("check Customer existing by its parameter name -> value. Customer not found") {
               repository.isCustomerExist("email", "not_exists@mail.com") shouldBe false
            }
        }
    }
}

class MongoCursorMock(var toVisitCount : Int = 1) : MongoCursor<Document> {

    override fun next(): Document {
        if (toVisitCount > 0) {
            toVisitCount = toVisitCount - 1
        }
        return Document() //mock Document element
    }

    override fun hasNext(): Boolean {
        return toVisitCount > 0
    }

    override fun tryNext(): Document? {
        TODO("not implemented")
    }

    override fun remove() {
        TODO("not implemented")
    }

    override fun close() {
        TODO("not implemented")
    }

    override fun getServerCursor(): ServerCursor? {
        TODO("not implemented")
    }

    override fun getServerAddress(): ServerAddress {
        TODO("not implemented")
    }

}