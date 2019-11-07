package com.example.customer.web

import com.example.customer.service.api.*
import com.example.customer.repository.CustomerRepositoryImpl
import com.example.customer.service.CustomerRequestValidatorImpl
import com.example.customer.service.CustomerServiceImpl
import com.fasterxml.jackson.databind.SerializationFeature
import com.mongodb.MongoClient
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing

fun Application.main() {
    install(CallLogging) {}
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT) // json pretty printing
        }
    }
    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError, Status.Error())//Internal error customization
            throw it
        }
    }

    val dbHost = environment.config.propertyOrNull("customer.db.host")?.getString()?:"localhost"
    val dbPort = environment.config.propertyOrNull("customer.db.port")?.getString()?.toIntOrNull()?:27017
    val customerEndpoint = environment.config.propertyOrNull("customer.endpoint")?.getString()?:"/v2/api/customer"

    //TODO DI with Kodein or Koin library
    val service = CustomerServiceImpl(
            CustomerRepositoryImpl(MongoClient(dbHost, dbPort), "customer-service"),
            CustomerRequestValidatorImpl()
    )
    routing {

        get("/health_check") {
            call.respondText("OK", ContentType.Text.Html)
            log.info("health_check!")
        }

        get("$customerEndpoint/{id}") {
            val result = service.getCustomerById(call.parameters["id"])
            log.debug("result, $result")
            call.respond(mapToHttpCode(result), result)
        }

        post("$customerEndpoint/create") {
            //TODO consider case when invalid request to avoid exceptions like JsonParseException, UnrecognizedPropertyException
            //if request is invalid return then HttpStatusCode.BadRequest instead of exception throwing exception and 500 http-code
            //it would be performance improvement
            //possible solution: to call method call.receiveParameters() an validate input before transforming to CustomerRequest
            val result = service.createCustomer(call.receive())
            log.debug("result, $result")
            call.respond(mapToHttpCode(result), result)
        }

        post("$customerEndpoint/update") {
            val result = service.updateCustomer(call.receive())
            log.debug("result, $result")
            call.respond(mapToHttpCode(result), result)
        }
    }
}