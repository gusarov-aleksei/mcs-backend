package com.example.customer.repository

import com.example.customer.service.api.Customer
import com.example.customer.service.api.CustomerRequest
import com.mongodb.MongoClient
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface CustomerRepository {

    fun createCustomer(params: Map<String, String>) : Customer
    fun getCustomerBy(id: String) : Customer?
    fun updateCustomer(request: CustomerRequest) : Boolean
    fun isCustomerExist(field: String, value: String) : Boolean
    //fun deleteCustomerById(id: String)

}

//TODO check how it behaves when no connection to db. how to close resources correctly
class CustomerRepositoryImpl(client: MongoClient, dbName: String) : CustomerRepository {
    private val db = client.getDatabase(dbName)
    private val log = LoggerFactory.getLogger(CustomerRepositoryImpl::class.java)

    override fun getCustomerBy(id: String) : Customer? {
        val res = db.getCollection("customer").find(Document("_id", ObjectId(id)))
        return res.first()?.let {mapDocumentToCustomer(it)}
    }

    /**
     * Input parameters must be pre-validated before method calling
     */
    override fun createCustomer(params: Map<String, String>) : Customer {
        val document = Document(params)
        document["createdDate"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        db.getCollection("customer").insertOne(document)
        log.debug("result create ${document["_id"]}")
        return mapDocumentToCustomer(document)
    }

    override fun updateCustomer(request: CustomerRequest) : Boolean {
        //or try collection.updateOne(Bson filter, Bson update)
        //to check which is more lightweight
        val updateOperationDocument = Document("\$set",  Document(request.fields))
        val res = db.getCollection("customer")
                .findOneAndUpdate(Document("_id", ObjectId(request.id)), updateOperationDocument)
        log.debug("result update $res")
        return res != null
    }

    override fun isCustomerExist(field: String,  value: String) : Boolean {
        return db.getCollection("customer").find(Document(field, value)).limit(1).count() > 0
    }

}