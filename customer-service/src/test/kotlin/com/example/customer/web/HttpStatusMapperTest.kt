package com.example.customer.web

import com.example.customer.service.api.Customer
import com.example.customer.service.api.Status
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.forAll
import io.kotlintest.tables.table
import io.kotlintest.tables.row
import io.kotlintest.tables.headers
import io.ktor.http.HttpStatusCode

class HttpStatusMapperTest : StringSpec({

    "mapToHttpCode should map service result to http code" {
        table (
                headers("Operation result","Http code"),
                row(Status.CanNotReadRequest, HttpStatusCode.BadRequest),
                row(Status.CustomerIdInvalid("id"), HttpStatusCode.BadRequest),
                row(Status.FieldValueInvalid(mapOf("field name" to "some value")), HttpStatusCode.BadRequest),
                row(Status.NoFieldsToUpdate, HttpStatusCode.BadRequest),
                row(Status.EmailIsAlreadyUsed, HttpStatusCode.BadRequest),
                row(Status.MandatoryFieldNotFound(listOf("field name")), HttpStatusCode.BadRequest),
                row(Status.FieldNameInvalid(listOf("field name")), HttpStatusCode.BadRequest),
                row(Status.CustomerNotFound("id"), HttpStatusCode.NotFound),
                row(Status.ReadSuccess(Customer("some id", "some name", "some email", "1500-10-10")), HttpStatusCode.OK),
                row(Status.CreateSuccess(Customer("some id", "some name", "some email", "1500-10-10")), HttpStatusCode.OK),
                row(Status.UpdateSuccess, HttpStatusCode.OK),
                row(Status.Success, HttpStatusCode.OK),
                row(Status.Error("detail"), HttpStatusCode.InternalServerError)
        ).forAll {
                operationResult, httpCode -> mapToHttpCode(operationResult) shouldBe httpCode
        }
    }
})