package com.example.customer.web

import com.example.customer.service.api.*
import com.example.customer.service.CustomerService
import com.fasterxml.jackson.databind.SerializationFeature
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
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import org.koin.ktor.ext.inject

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

    val customerEndpoint = environment.config.propertyOrNull("customer.endpoint")?.getString()?:"/v2/api/customer"

    routing {
        v2(customerEndpoint)
    }
}

fun Routing.v2(endpoint: String) {

    val service : CustomerService by inject()

    get("/health_check") {
        call.respondText("OK", ContentType.Text.Html)
        call.application.log.info("health_check!")
    }

    get("$endpoint/{id}") {
        val result = service.getCustomerById(call.parameters["id"])
        call.application.log.debug("result: $result")
        call.respond(mapToHttpCode(result), result)
    }

    post("$endpoint/create") {
        //TODO consider case when invalid request, to avoid exceptions like JsonParseException, UnrecognizedPropertyException
        //it would be performance improvement
        //possible solution: to call method call.receiveParameters() and validate input before transforming to CustomerRequest
        val status = call.processRequest(service::createCustomer)
        call.application.log.debug("result: $status")
        call.respond(mapToHttpCode(status), status)
    }

    post("$endpoint/update") {
        with(call) {
            val status = processRequest(service::updateCustomer)
            call.application.log.debug("result: $status")
            respond(mapToHttpCode(status), status)
        }
    }
}

suspend fun ApplicationCall.processRequest(operation: (CustomerRequest) -> Status ) : Status {
    return runCatching {
        receive<CustomerRequest>()
    }.map {
        operation(it)
    }.onFailure {
        application.log.error("Error in request reading: $it")
    }.getOrElse {
        Status.CanNotReadRequest
    }
}