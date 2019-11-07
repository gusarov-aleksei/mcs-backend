package com.example.customer.service

import com.example.customer.repository.CustomerRepository
import com.example.customer.service.api.*
import org.slf4j.LoggerFactory

interface CustomerService {

    fun getCustomerById(id : String?) : Status

    fun createCustomer(request : CustomerRequest) : Status

    fun updateCustomer(request : CustomerRequest) : Status

}

class CustomerServiceImpl(private val repository: CustomerRepository,
                          private val validator: CustomerRequestValidator) : CustomerService {

    private val log = LoggerFactory.getLogger(CustomerServiceImpl::class.java)

    override fun getCustomerById(id: String?): Status {
        log.debug("request get $id")
        if (!validator.validateId(id)) {
            return Status.CustomerIdInvalid(id?:"null")
        }
        return when (val customer = repository.getCustomerBy(id!!)) {
            null -> Status.CustomerNotFound(id)
            else -> Status.ReadSuccess(customer)
        }
    }

    override fun createCustomer(request: CustomerRequest): Status {
        log.debug("request create $request")
        val status = validator.validateCreate(request)
        return if (Status.Success != status) {
            status
        } else when (repository.isCustomerExist("email", request.fields["email"]!!)){
            true -> Status.EmailIsAlreadyUsed
            false -> Status.CreateSuccess(repository.createCustomer(request.fields))
        }
    }

    override fun updateCustomer(request: CustomerRequest): Status {
        log.debug("request update $request")
        val status = validator.validateUpdate(request)
        return if (Status.Success != status) {
            status
        } else when (repository.updateCustomer(request)){
            true -> Status.UpdateSuccess
            false -> Status.CustomerNotFound(request.id)
        }
    }

}