package com.example.customer.service

import com.example.customer.repository.CustomerRepository
import com.example.customer.service.api.Customer
import com.example.customer.service.api.CustomerRequest
import com.example.customer.service.api.Status
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec

class CustomerServiceTest : FeatureSpec() {

    private val customerService = CustomerServiceImpl(CustomerRepositoryMock(), CustomerRequestValidatorMock())

    init {

        feature("Get customer by id") {

            scenario("When id is invalid. Result should be CustomerIdInvalid") {
                customerService.getCustomerById("some-invalid-id") shouldBe Status.CustomerIdInvalid("some-invalid-id")
                customerService.getCustomerById(null) shouldBe Status.CustomerIdInvalid("null")
            }

            scenario("There is no customer in repository. Result should be CustomerNotFound") {
                customerService.getCustomerById("1") shouldBe Status.CustomerNotFound("1")
            }

            scenario("There is no customer in repository. Result should be  CustomerNotFound") {
                customerService.getCustomerById("1000") shouldBe
                        Status.ReadSuccess(Customer("1000", "Imaginable name", "imaginable@mail.com", "1200-12-12"))
            }
        }

        feature("Create customer") {

            scenario("Create customer request validation isn't passed") {
                customerService.createCustomer(CustomerRequest()) shouldBe Status.MandatoryFieldNotFound(listOf("email"))
            }

            scenario("Customer with email already exists. Result should be EmailIsAlreadyUsed") {
                customerService.createCustomer(CustomerRequest(mapOf("email" to "email@mail.com"))) shouldBe Status.EmailIsAlreadyUsed
            }

            scenario("There is no customer in repository. Result should be  CustomerNotFound") {
                customerService.createCustomer(CustomerRequest(mapOf("email" to "imaginable@mail.com"))) shouldBe Status.CreateSuccess(customer1000)
            }
        }

        feature("Update customer") {

            scenario("Update customer request validation isn't passed") {
                customerService.updateCustomer(CustomerRequest()) shouldBe Status.NoFieldsToUpdate
            }

            scenario("Customer with email already exists. Result should be EmailIsAlreadyUsed") {
                customerService.updateCustomer(CustomerRequest(id = "1", fields = mapOf("name" to "Updated name"))) shouldBe Status.CustomerNotFound("1")
            }

            scenario("There is no customer in repository. Result should be  CustomerNotFound") {
                customerService.updateCustomer(CustomerRequest(id = "1000", fields = mapOf("name" to "Updated name"))) shouldBe Status.UpdateSuccess
            }
        }
    }
}

class CustomerRequestValidatorMock : CustomerRequestValidator {
    override fun validateCreate(request: CustomerRequest): Status {
        return when(request) {
            CustomerRequest() -> Status.MandatoryFieldNotFound(listOf("email")) //suppose request with default params doesn't pass validation
            else -> Status.Success
        }
    }

    override fun validateUpdate(request: CustomerRequest): Status {
        return when(request) {
            CustomerRequest() -> Status.NoFieldsToUpdate //suppose request with default params doesn't pass validation
            else -> Status.Success
        }
    }

    override fun validateId(id: String?): Boolean {
        return when (id) {
            null -> false
            "some-invalid-id" -> false //assume this id is invalid
            else -> true
        }
    }
}

val customer1000 = Customer("1000", "Imaginable name", "imaginable@mail.com", "1200-12-12")

class CustomerRepositoryMock : CustomerRepository {

    override fun createCustomer(params: Map<String, String>): Customer {
        return customer1000
    }

    override fun getCustomerBy(id: String): Customer? {
        return when (id) {
            "1000" -> customer1000
            else -> null //customer not found case
        }
    }

    override fun updateCustomer(request: CustomerRequest): Boolean {
        return when(request.id) {
            "1000" -> true //assume customer with id 10 exists in repository and updated
            else -> false //otherwise customer not found
        }
    }

    override fun isCustomerExist(field: String, value: String): Boolean {
        return when(value) {
            "email@mail.com" -> true //customer with this value already exists. 'field' isn't considered here for simplification
            else -> false
        }
    }

}
