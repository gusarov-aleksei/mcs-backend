package com.example.customer.repository

import com.example.customer.service.api.CustomerRequest
import com.mongodb.MongoClient
import io.kotlintest.Spec
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import org.junit.Ignore
import org.testcontainers.containers.GenericContainer

/**
 * Sort of integration test. Test sets up real data base instance in docker using testcontainers library
 * Treat it as integration test rather than unit-test
 */
@Ignore
class CustomerRepositoryIntTest : FeatureSpec() {

    private val dbContainer = autoClose(GenericContainer<Nothing>("mvertes/alpine-mongo"))

    init {
        initDatabaseContainer()
    }

    private fun initDatabaseContainer() {
        dbContainer.withExposedPorts(27017)
        dbContainer.start()
        println("container.containerIpAddress ${dbContainer.containerIpAddress}")
        println("container.firstMappedPort ${dbContainer.firstMappedPort}")
    }

    private val repository = CustomerRepositoryImpl(autoClose(MongoClient(dbContainer.containerIpAddress, dbContainer.firstMappedPort)), "customer-service")

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        dbContainer.stop()
    }

    private val customerFields = mapOf("name" to "John Smith", "email" to "john.smith@mail.com","dateOfBirth" to "1302-07-09")

    init {
        feature("Create Customer in repository") {

            scenario("Create Customer in repository with valid parameters") {
                val customer = repository.createCustomer(customerFields)
                customer.id.shouldNotBeNull()
                customer.name shouldBe "John Smith"
                customer.email shouldBe "john.smith@mail.com"
                customer.dateOfBirth shouldBe "1302-07-09"
            }
        }

        feature("Get Customer by id from repository") {
            scenario("Get Customer by id from repository when Customer found") {
                val customer = repository.createCustomer(customerFields)
                val persisted = repository.getCustomerBy(customer.id)
                persisted.shouldNotBeNull()
                persisted.id.shouldNotBeNull()
                persisted.name shouldBe "John Smith"
                persisted.email shouldBe "john.smith@mail.com"
                persisted.dateOfBirth shouldBe "1302-07-09"
            }

            scenario("Get Customer by id from repository when Customer not found") {
                val persisted = repository.getCustomerBy("5dbaff767d4ca12831865845")
                persisted.shouldBeNull()
            }
        }

        feature("Update Customer in repository with valid parameters") {
            scenario("should update with new parameters if Customer exists in repository") {
                val customer = repository.createCustomer(customerFields)

                val result = repository.updateCustomer(CustomerRequest(id = customer.id, fields = mapOf("dateOfBirth" to "1422-04-07")))
                result shouldBe true

                val persisted = repository.getCustomerBy(customer.id)
                //check all parameters value equals previous
                persisted.shouldNotBeNull()
                persisted.id.shouldNotBeNull()
                persisted.name shouldBe "John Smith"
                persisted.email shouldBe "john.smith@mail.com"
                //updated parameter should equal new value
                persisted.dateOfBirth shouldBe "1422-04-07"
            }

            scenario("should update with new parameters if Customer doesn't exist") {
                val result = repository.updateCustomer(CustomerRequest(id = "5dbaff767d4ca12831865845", fields = mapOf("dateOfBirth" to "1422-04-07")))
                result shouldBe false
            }
        }

        feature("Check customer existing in repository by some parameter") {
            scenario("should return true if Customer exists in repository") {
                repository.createCustomer(customerFields)
                repository.isCustomerExist("email","john.smith@mail.com") shouldBe true
            }

            scenario("should return false if Customer doesn't exist") {
                repository.isCustomerExist("email","john.smith@mail.com") shouldBe true
            }
        }
    }
}