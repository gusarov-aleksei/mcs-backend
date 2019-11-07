package com.example.customer.web

import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.specs.StringSpec
import io.ktor.application.Application
import io.ktor.server.testing.withTestApplication

class HttpServerTest : AnnotationSpec() {

    fun beforeTest() {
        withTestApplication(Application::main){

        }
    }



}