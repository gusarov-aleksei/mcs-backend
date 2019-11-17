package com.example.customer.web.mock

import com.example.customer.service.CustomerService
import com.example.customer.service.api.Customer
import com.example.customer.service.api.CustomerRequest
import com.example.customer.service.api.Status

/**
 * Mock of main backend service class. It is used for server settings testing.
 */
class CustomerServiceMock : CustomerService {

    override fun getCustomerById(id: String?): Status {
        return Status.ReadSuccess(Customer(id="customer-id",name = "John Kotov", email="john.kotov@email.com", dateOfBirth = "1302-07-09"))
    }

    override fun createCustomer(request: CustomerRequest): Status {
        return Status.CreateSuccess(Customer(id="customer-id",name = "John Kotov", email="john.kotov@email.com", dateOfBirth = "1302-07-09"))
    }

    override fun updateCustomer(request: CustomerRequest): Status {
        return Status.UpdateSuccess
    }

}