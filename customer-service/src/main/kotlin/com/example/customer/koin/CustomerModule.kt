package com.example.customer.koin

import com.example.customer.repository.CustomerRepository
import com.example.customer.repository.CustomerRepositoryImpl
import com.example.customer.service.CustomerRequestValidator
import com.example.customer.service.CustomerRequestValidatorImpl
import com.example.customer.service.CustomerService
import com.example.customer.service.CustomerServiceImpl
import com.mongodb.MongoClient
import org.koin.dsl.module

val customerModule = module {

        single { CustomerRequestValidatorImpl() as CustomerRequestValidator }
        single {
                val dbHost = getProperty("CUSTOMER_DB_HOST", getProperty("customer.db.host", "localhost"))
                val dbPort = getProperty("CUSTOMER_DB_PORT", getProperty("customer.db.port", 27017))
                CustomerRepositoryImpl(MongoClient(dbHost, dbPort),"customer-service") as CustomerRepository
        }
        single { CustomerServiceImpl(get(), get()) as CustomerService }
}
