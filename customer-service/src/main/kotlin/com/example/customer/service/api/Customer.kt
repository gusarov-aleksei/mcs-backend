package com.example.customer.service.api

//structure models Customer entity
data class Customer(val id: String, val name: String, val email: String, val dateOfBirth: String)

//structure carries input parameters for create and update request
data class CustomerRequest(val fields: Map<String, String> = mapOf(), val id: String = "empty")

//result of operation with Customer service
sealed class Status {
    object EmailIsAlreadyUsed : Status() {
         val desc = "Email is already used"
    }
    data class FieldValueInvalid(val fields : Map<String, Any>, val desc: String="Invalid field value") : Status()
    data class FieldNameInvalid(val fields : List<String>, val desc: String="Invalid field name") : Status()
    data class CustomerNotFound(val id : String, val desc: String="Customer with id not found") : Status()
    data class CustomerIdInvalid(val id : String, val desc: String="Invalid Customer id") : Status()
    data class MandatoryFieldNotFound(val fields : List<String>, val desc: String="Expected mandatory fields") : Status()
    data class CreateSuccess(val customer: Customer) : Status()
    data class ReadSuccess(val customer: Customer) : Status()
    object Success: Status()
    object UpdateSuccess : Status()
    object NoFieldsToUpdate : Status() {
        val desc = "No fields requested"
    }
    object CanNotReadRequest : Status() {
        val desc = "Can't process input request"
    }
    data class Error(val desc: String="Request was not processed") : Status()
}