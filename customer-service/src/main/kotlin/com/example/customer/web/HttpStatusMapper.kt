package com.example.customer.web

import com.example.customer.service.api.Status
import io.ktor.http.HttpStatusCode

fun mapToHttpCode(resp : Status) : HttpStatusCode  {
    return when (resp) {
        is Status.CustomerIdInvalid,
        is Status.FieldValueInvalid,
        is Status.NoFieldsToUpdate,
        is Status.EmailIsAlreadyUsed,
        is Status.MandatoryFieldNotFound,
        is Status.FieldNameInvalid -> HttpStatusCode.BadRequest
        is Status.CustomerNotFound ->  HttpStatusCode.NotFound
        is Status.Error -> HttpStatusCode.InternalServerError
        is Status.ReadSuccess,
        is Status.UpdateSuccess -> HttpStatusCode.OK
        else -> HttpStatusCode.OK
    }
}