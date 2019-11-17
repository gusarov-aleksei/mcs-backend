package com.example.customer.web

import com.example.customer.service.*
import com.example.customer.web.mock.CustomerServiceMock
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.Application
import io.ktor.http.*
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import org.junit.Before
import org.junit.jupiter.api.Assertions.*
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest

/**
 *  Test validates routing and some configuration settings of Ktor server.
 */
class HttpServerTest : AutoCloseKoinTest() {

    private val jsonMapper = jacksonObjectMapper()

    private val separator = 10.toChar()//new line
    //server adds symbols like spaces and new lines for pretty printing. this expected content is enriched with spaces and new lines
    private val expectedPrettyPrinted ="{$separator" +
            "  \"customer\" : {$separator" +
            "    \"id\" : \"customer-id\",$separator" +
            "    \"name\" : \"John Kotov\",$separator" +
            "    \"email\" : \"john.kotov@email.com\",$separator" +
            "    \"dateOfBirth\" : \"1302-07-09\"$separator" +
            "  }$separator" +
            "}"

    private val expectedError = "{$separator" +
        "  \"desc\" : \"Can't process input request\"$separator" +
        "}"

    private val mockModule = module {
        single{ CustomerServiceMock() as CustomerService }
    }

    @Before
    fun initMocks() {
        startKoin{
            modules(mockModule)
        }
    }

    @Test
    fun testGetHealthCheck_whenRequestIsValid_shouldRespondOK() = withTestApplication(Application::main){
        with(handleRequest(HttpMethod.Get, "/health_check")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("OK", response.content)
        }
    }

    @Test
    fun testGetMethodGetCustomerById_whenRequestIsValid_shouldRespondOK() = withTestApplication(Application::main){
        with(handleRequest(HttpMethod.Get, "/v2/api/customer/customer-id")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(expectedPrettyPrinted, response.content)
        }
    }

    @Test
    fun testPostMethodCustomerCreate_whenRequestIsValid_shouldRespondOK() = withTestApplication(Application::main){
        val body =  jsonMapper.writeValueAsString(
                mapOf("fields" to  mapOf("name" to "John Kotov", "email" to "john.kotov@email.com", "dateOfBirth" to "1302-07-09")))
        handleRequest(HttpMethod.Post, "/v2/api/customer/create") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(body)
        }.run {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(expectedPrettyPrinted, response.content)
        }
    }

    @Test
    fun testPostMethodCustomerCreate_whenBodyRequestIsNotJsonFormat_shouldRespondBadRequest() = withTestApplication(Application::main){
        handleRequest(HttpMethod.Post, "/v2/api/customer/create") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("some random string")
        }.run {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals(expectedError, response.content)
        }
    }

    @Test
    fun testPostMethodCustomerUpdate_whenRequestIsValid_shouldRespondOK() = withTestApplication(Application::main){
        val body =  jsonMapper.writeValueAsString(
                mapOf("fields" to  mapOf("name" to "John Kotov", "email" to "john.kotov@email.com", "dateOfBirth" to "1302-07-09")))
        handleRequest(HttpMethod.Post, "/v2/api/customer/update") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(body)
        }.run {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun testPostMethodCustomerUpdate_whenBodyRequestIsNotJsonFormat_shouldRespondBadRequest() = withTestApplication(Application::main){
        handleRequest(HttpMethod.Post, "/v2/api/customer/update") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("some random string")
        }.run {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals(expectedError, response.content)
        }
    }
}