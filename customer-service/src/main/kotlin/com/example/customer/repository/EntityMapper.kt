package com.example.customer.repository

import com.example.customer.service.api.Customer
import org.bson.Document

fun mapDocumentToCustomer(doc: Document) : Customer {
    return Customer(doc["_id"].toString(),
            doc["name"].toString(),
            doc["email"].toString(),
            doc["dateOfBirth"].toString())
}