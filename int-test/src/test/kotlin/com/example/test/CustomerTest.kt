package com.example.test

import com.example.test.utils.HttpClientFacade
import com.example.test.utils.MongoClientFacade
import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec

import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.types.shouldNotBeNull
import org.apache.http.HttpStatus
import org.bson.types.ObjectId


class CustomerTest : FeatureSpec(){

    private val httpClient = autoClose(HttpClientFacade())

    private val mongoClient = autoClose(MongoClientFacade())

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        println("Total customer in DB after test ${mongoClient.getCountOfCustomers()}")
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        println("Total customer in DB before test ${mongoClient.getCountOfCustomers()}")
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        super.afterTest(testCase, result)
        println("result $result")
        //each test store customer id to created data using putMetaData. need to remove it after each Success or Failure
        result.metaData["customerId"]?.let {
            println("Customer id to clean up DB $it")
            mongoClient.removeCustomerById(it as String)
        }
    }

    private val prerequisiteCustomerParameters =
            mapOf("name" to "Bill Ivanov", "email" to "bill.ivanov@mail.io", "dateOfBirth" to "1980-12-31")

    //TODO refactor return customer and new mail
    /**
     * Creates Customer
     * @return Customer id
     */
    private fun createPrerequisite() : String {
        val created = httpClient.sendPost(customerCreateUrl(),
                mapOf("id" to "empty", "fields" to mapOf("name" to "Bill Ivanov", "email" to "bill.ivanov@mail.io", "dateOfBirth" to "1980-12-31")))
        println("created log $created")
        created.status shouldBe HttpStatus.SC_OK
        val customer = created.body["customer"]
        @Suppress("Unchecked_cast")
        return (customer as Map<String, Any>)["id"] as String
    }

    private fun assertEntityInRepository(id : String, expected : Map<String, Any>) {
        val persisted = mongoClient.getCustomerById(id)
        persisted.shouldNotBeNull()
        persisted["_id"].shouldNotBeNull()
        expected.forEach {
            persisted[it.key] shouldBe it.value
        }
    }

    init {

        feature("New Customer create (POST /api/customer/create)") {
            scenario("Call Customer service to create Customer") {
                //action
                val resp = httpClient.sendPost(customerCreateUrl(),
                        mapOf("fields" to mapOf("name" to "John Smith", "email" to "john@smith.com", "dateOfBirth" to "2000-01-01")))
                //validate response
                resp.status shouldBe HttpStatus.SC_OK
                resp.body["customer"].shouldNotBeNull()
                //validate customer in response
                val customer = resp.body["customer"] as Map<String, Any>
                customer["id"].shouldNotBeNull()
                val id =  customer["id"] as String
                putMetaData("customerId", id)
                val expected = mapOf("name" to "John Smith", "email" to "john@smith.com", "dateOfBirth" to "2000-01-01")
                customer shouldContainAll expected
                //validate customer in storage
                assertEntityInRepository(id, expected)
            }

            scenario("Call Customer service to create Customer when another Customer with the same email exists") {
                val customerId = createPrerequisite()
                val resp = httpClient.sendPost(customerCreateUrl(),
                        mapOf("fields" to mapOf("name" to "Bill Ivanov", "email" to "bill.ivanov@mail.io", "dateOfBirth" to "1980-12-31")))
                resp.status shouldBe HttpStatus.SC_BAD_REQUEST
                resp.body["desc"] shouldBe "Email is already used"
                putMetaData("customerId", customerId)
            }

            //redundant
            scenario("Call Customer service to create Customer without non-mandatory parameter 'dateOfBirth'") {
                val resp = httpClient.sendPost(customerCreateUrl(),
                        mapOf("fields" to mapOf("name" to "Bill Ivanov", "email" to "bill.ivanov@mail.io")))
                //validate response
                resp.status shouldBe HttpStatus.SC_OK
                resp.body["customer"].shouldNotBeNull()
                //validate customer in response
                val customer = resp.body["customer"] as Map<String, Any>
                customer["id"].shouldNotBeNull()
                val id =  customer["id"] as String
                putMetaData("customerId", id)
                //TODO may be "null" value string isn't correct. dont' put "null" at server
                val expected = mapOf("name" to "Bill Ivanov", "email" to "bill.ivanov@mail.io", "dateOfBirth" to "null")
                customer shouldContainAll expected
            }

            scenario("Call Customer service to create Customer with all empty required parameters in request") {
                val resp = httpClient.sendPost(customerCreateUrl(), mapOf("fields" to mapOf<String, String>()))
                //validate response
                resp.status shouldBe HttpStatus.SC_BAD_REQUEST
                resp.body shouldBe
                        mapOf("fields" to listOf("name", "email"), "desc" to "Expected mandatory fields")
            }

            scenario("Call Customer service to create Customer with one empty required parameter in request (no email)") {
                val resp = httpClient.sendPost(customerCreateUrl(),
                        mapOf("fields" to mapOf("name" to "Bill Ivanov")))
                //validate response
                resp.status shouldBe HttpStatus.SC_BAD_REQUEST
                resp.body shouldBe
                        mapOf("fields" to listOf("email"), "desc" to "Expected mandatory fields")
            }

            scenario("Call Customer service to create Customer with unknown parameter") {
                val resp = httpClient.sendPost(customerCreateUrl(),
                        mapOf("fields" to mapOf("name" to "Bill Ivanov", "email" to "bill.ivanov@mail.io", "unexpectedField" to "some value")))
                //validate response
                resp.status shouldBe HttpStatus.SC_BAD_REQUEST
                resp.body shouldBe
                        mapOf("fields" to listOf("unexpectedField"), "desc" to "Invalid field name")
            }

            scenario("Call customer create service with empty 'dateOfBirth'") {
                val resp = httpClient.sendPost(customerCreateUrl(),
                        "{\"fields\":{\"name\":\"John Smith\",\"email\":\"john@smith.com\",\"dateOfBirth\":\"\"}}")
                resp.status shouldBe HttpStatus.SC_BAD_REQUEST
                resp.body shouldBe
                        mapOf("fields" to mapOf("dateOfBirth" to ""), "desc" to "Invalid field value")
            }

            scenario("Call customer create service with incorrect request body") {
                val resp = httpClient.sendPost(customerCreateUrl(), "some random text!")
                resp.status shouldBe HttpStatus.SC_BAD_REQUEST
                resp.body shouldBe mapOf("desc" to "Can't process input request")
            }

        }

        feature("Existing Customer getting by id (GET /api/customer/{id})") {
            scenario("Call Customer service to get by id") {
                val id = createPrerequisite()
                putMetaData("customerId", id)
                val response = httpClient.sendGet("${customerBaseUrl()}/$id")
                //validate response with Customer
                response.body["customer"].shouldNotBeNull()
                val customer = response.body["customer"] as Map<String, Any>
                customer.keys shouldContainExactly setOf("id", "name", "email", "dateOfBirth")
                customer["id"].shouldNotBeNull()
                customer shouldContainAll prerequisiteCustomerParameters
                //validate data in persistence storage
                assertEntityInRepository(id, prerequisiteCustomerParameters)
            }

            scenario("Call Customer service to get by id if customer doesn't exist") {
                val (status, body) = httpClient.sendGet("${customerBaseUrl()}/${ObjectId.get()}")
                status shouldBe HttpStatus.SC_NOT_FOUND
                body["id"].shouldNotBeNull()
                body["desc"] shouldBe "Customer with id not found"
            }

            scenario("Call Customer service to get by id invalid id") {
                val (status, body) = httpClient.sendGet("${customerBaseUrl()}/invalid-id")
                status shouldBe HttpStatus.SC_BAD_REQUEST
                body["id"].shouldNotBeNull()
                body["desc"] shouldBe "Invalid Customer id"
            }

            /*scenario("Call Customer service to get by id with empty id parameter") {
            }*/
        }

        feature("Customer updating (POST /api/customer/update)") {
            scenario("Update Customer with some allowed values") {
                //prerequisites
                val id = createPrerequisite()
                putMetaData("customerId", id)
                //test case
                val (status, body) = httpClient.sendPost(customerUpdateUrl(),
                        mapOf("id" to id, "fields" to mapOf("name" to "Bill Ivanov 2", "dateOfBirth" to "1980-12-30")))
                //validation
                status shouldBe HttpStatus.SC_OK
                body shouldBe mapOf()//TODO or return current DB state?
                //validate in DB
                assertEntityInRepository(id, mapOf("name" to "Bill Ivanov 2", "email" to "bill.ivanov@mail.io", "dateOfBirth" to "1980-12-30"))
            }

            scenario("Update Customer when Customer doesn't exist") {
                //don't create customer before update, just generate customer id
                val id = ObjectId.get().toHexString()
                val params = mapOf("id" to id, "fields" to mapOf("name" to "Bill Ivanov", "dateOfBirth" to "1980-12-30"))
                val response = httpClient.sendPost(customerUpdateUrl(), params)
                response.status shouldBe HttpStatus.SC_NOT_FOUND
                response.body shouldBe mapOf("id" to id, "desc" to "Customer with id not found")
            }

            scenario("Update Customer when 'id' parameter isn't defined") {
                val (status, body) = httpClient.sendPost(customerUpdateUrl(),
                        mapOf("fields" to mapOf("name" to "Bill Ivanov", "dateOfBirth" to "1980-12-30")))
                //result validation
                status shouldBe HttpStatus.SC_BAD_REQUEST
                body shouldBe mapOf("id" to "empty", "desc" to "Invalid Customer id")
            }

            scenario("Update Customer with request without 'fields' parameter") {
                //prerequisites
                val id = createPrerequisite()
                putMetaData("customerId", id)
                //action
                val (status, body) = httpClient.sendPost(customerUpdateUrl(),
                        mapOf("id" to id))//no fields k->v here
                //result validation
                status shouldBe HttpStatus.SC_BAD_REQUEST
                body shouldBe mapOf("desc" to "No fields requested")
                assertEntityInRepository(id, mapOf())
            }

            scenario("Update Customer with request with empty 'fields' parameter") {
                //prerequisites
                val id = createPrerequisite()
                putMetaData("customerId", id)
                //action
                val (status, body) = httpClient.sendPost(customerUpdateUrl(), mapOf("id" to id, "fields" to mapOf<String, String>()))
                //result validation
                status shouldBe HttpStatus.SC_BAD_REQUEST
                body shouldBe mapOf("desc" to "No fields requested")
            }

            scenario("Call Customer service to update Customer when field name isn't allowed") {
                //prerequisites
                val id = createPrerequisite()
                //save created customer id to clear after test
                putMetaData("customerId", id)
                //action
                val (status, body) = httpClient.sendPost(customerUpdateUrl(), mapOf("id" to id,
                        "fields" to mapOf("name 2" to "Customer name 2")))
                //result validation
                status shouldBe HttpStatus.SC_BAD_REQUEST
                body shouldBe mapOf("fields" to listOf("name 2"), "desc" to "Invalid field name")
            }

            scenario("Call Customer service to update Customer when field value isn't allowed") {
                //prerequisites
                val customerId = createPrerequisite()
                putMetaData("customerId", customerId)
                //action
                val (status, body) = httpClient.sendPost(customerUpdateUrl(),
                        mapOf("id" to customerId, "fields" to mapOf("name" to "select * from", "dateOfBirth" to "1980-12-31"))
                )
                //result validation
                status shouldBe HttpStatus.SC_BAD_REQUEST
                body shouldBe mapOf("desc" to "Invalid field value", "fields" to mapOf("name" to "select * from"))
                //check updated in database parameters
                assertEntityInRepository(customerId, prerequisiteCustomerParameters)
            }

            scenario("Call customer update service with incorrect request body") {
                val resp = httpClient.sendPost(customerUpdateUrl(), "some random text")
                resp.status shouldBe HttpStatus.SC_BAD_REQUEST
                resp.body shouldBe mapOf("desc" to "Can't process input request")
            }

            //TODO consider scenario with not running db
        }
    }
}