package com.example.customer.service

import com.example.customer.service.api.CustomerRequest
import com.example.customer.service.api.Status
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec

class CustomerRequestValidatorTest : FeatureSpec(){

    private val validator = CustomerRequestValidatorImpl()

    init {
        feature("Customer request overall validation on create") {
            scenario("All fields conform expectation. Result should be Success") {
                val request = CustomerRequest(mapOf("name" to "John Smith", "email" to "john.smith@mail.com"))
                validator.validateCreate(request) shouldBe Status.Success
            }

            scenario("Mandatory field email doesn't present in request. Result should be MandatoryFieldNotFound") {
                validator.validateCreate(CustomerRequest(mapOf("name" to "John Smith"))) shouldBe Status.MandatoryFieldNotFound(listOf("email"))
                validator.validateCreate(CustomerRequest(mapOf())) shouldBe  Status.MandatoryFieldNotFound(listOf("name", "email"))
            }

            scenario("Field name is not allowed. Result should be FieldNameInvalid") {
                val request = CustomerRequest(mapOf("name" to "John Smith", "email" to "john.smith@mail.com", "suddenness" to "sudden value"))
                validator.validateCreate(request) shouldBe Status.FieldNameInvalid(listOf("suddenness"))
            }

            scenario("Field value is not allowed. Result should be FieldValueInvalid") {
                val request = CustomerRequest(mapOf("name" to "John Smith", "email" to "john.smith@mail.com","dateOfBirth" to "12345"))
                validator.validateCreate(request) shouldBe Status.FieldValueInvalid(mapOf("dateOfBirth" to "12345"))
            }
        }

        feature("Customer request overall validation on update") {
            scenario("All parameters in request is valid. Result should be Success") {
                val request = CustomerRequest(mapOf("name" to "John Smith"), "5dbaff767d4ca12831865845")
                validator.validateUpdate(request) shouldBe Status.Success
            }

            scenario("Customer id is is valid. Result should be CustomerIdInvalid") {
                val request = CustomerRequest(mapOf("name" to "John Smith"), "invaid-id")
                validator.validateUpdate(request) shouldBe Status.CustomerIdInvalid("invaid-id")
            }

            scenario("Request has no parameters. Result should be NoFieldsToUpdate") {
                val request = CustomerRequest(mapOf(), "5dbaff767d4ca12831865845")
                validator.validateUpdate(request) shouldBe Status.NoFieldsToUpdate
            }

            scenario("Request has not allowed to update parameter. Result should be FieldNameInvalid") {
                val request = CustomerRequest(mapOf("email" to "john.smith@mail.com"), "5dbaff767d4ca12831865845")
                validator.validateUpdate(request) shouldBe Status.FieldNameInvalid(listOf("email"))
            }

            scenario("Request has not allowed to update parameter. Result should be FieldValueInvalid") {
                val request = CustomerRequest(mapOf("name" to "John Smith", "dateOfBirth" to "1700-10-*"), "5dbaff767d4ca12831865845")
                validator.validateUpdate(request) shouldBe Status.FieldValueInvalid(mapOf("dateOfBirth" to "1700-10-*"))
            }
        }
    }
}