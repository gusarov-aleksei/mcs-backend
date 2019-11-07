package com.example.customer.service

import io.kotlintest.specs.FeatureSpec

import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.shouldBe

import io.kotlintest.tables.forAll
import io.kotlintest.tables.headers
import io.kotlintest.tables.row
import io.kotlintest.tables.table


class CustomerRequestParametersValuesTest :  FeatureSpec({

    val validator = CustomerRequestValidatorImpl()

    feature("Customer fields validation") {

        scenario("no fields in input") {
            validator.validateCreateValues(mapOf()) shouldContainExactly mapOf()
        }

        scenario("all fields are valid") {
            table (
                    headers("input fields", "invalid fields"),
                    row(mapOf("name" to "Ivan", "dateOfBirth" to "2019-10-21", "email" to "correct@mail.com"), mapOf<String, String>()),
                    row(mapOf("name" to "Ivan _ - 123", "dateOfBirth" to "1000-01-01", "email" to "c@l"), mapOf())

            ).forAll {
                input, output -> validator.validateCreateValues(input) shouldContainExactly output
            }
        }

        scenario("some fields are invalid") {
            table (
                    headers("input fields", "invalid fields"),
                    row(mapOf("name" to "Ivan*", "dateOfBirth" to "2019-10-21", "email" to "'|wrong@mail.com"),
                            mapOf("name" to "Ivan*", "email" to "'|wrong@mail.com")),
                    row(mapOf("name" to "Ivan _ - 123", "dateOfBirth" to "1000-01-01", "email" to "wrong mail"),
                            mapOf("email" to "wrong mail")),
                    row(mapOf("name" to "Ivan _ - 123", "dateOfBirth" to "0000-01-01"),
                            mapOf("dateOfBirth" to "0000-01-01"))
            ).forAll {
                input, output -> validator.validateCreateValues(input) shouldContainExactly output
            }
        }

        scenario("all fields are invalid") {
            table (
                    headers("input fields", "invalid fields"),
                    row(mapOf("name" to "Ivan *", "dateOfBirth" to "201/-20-21"),mapOf("name" to "Ivan *", "dateOfBirth" to "201/-20-21")),
                    row(mapOf("name" to "Ivan &", "dateOfBirth" to "201~-20-21"),mapOf("name" to "Ivan &", "dateOfBirth" to "201~-20-21")),
                    row(mapOf("name" to "Ivan ^", "dateOfBirth" to "201|-20-21"),mapOf("name" to "Ivan ^", "dateOfBirth" to "201|-20-21")),
                    row(mapOf("name" to "", "email" to ""),mapOf("name" to "", "email" to ""))
            ).forAll {
                input, output -> validator.validateCreateValues(input) shouldContainExactly output
            }
        }

        scenario("some fields are not allowed") {
            validator.validateCreateValues(mapOf("unknown field" to "Ivan", "dateOfBirth" to "2019-10-21")) shouldContainExactly
                    mapOf("unknown field" to "Ivan")
        }
        
    }

    feature("Customer fields are validated using Regex patterns") {
        scenario("name field") {
            table (
                    headers("input", "output"),
                    row("Ivan",true),
                    row("Ivan _ - 123",true),
                    row("Ivan &",false),
                    row("Ivan *",false),
                    row("Ivan !",false),
                    row("Ivan |",false),
                    row("Ivan ~",false),
                    row("",false)
            ).forAll {
                input, output -> validator.fieldToRegexp.getValue("name").matches(input) shouldBe  output
            }
        }

        scenario("email field") {
            table (
                    headers("input", "output"),
                    row("Ivan@mail.com",true),
                    row("Ivan@domain",true),
                    row("Ivan_Gonzales@domain",true),
                    row("Petr.Sanches@domain",true),
                    row("incorrect",false),
                    row("-@domain",false),
                    row("Ivan_Gonzales!@domain",false),
                    row("",false)
            ).forAll {
                input, output -> validator.fieldToRegexp.getValue("email").matches(input) shouldBe  output
            }
        }

        scenario("dateOfBirth field") {
            table (
                    headers("input", "output"),
                    row("2019-10-21",true),
                    row("1000-01-01",true),
                    row("",false),
                    row("0001-01-01",false),
                    row("200a-01-01",false),
                    row("2000-01-32",false),
                    row("2019-13-01",false),
                    row("2019-01-0!",false),
                    row("2019-01-0a",false),
                    row("2019-0|-01",false),
                    row("201|-01-01",false),
                    row("20190101",false)

            ).forAll {
                input, output -> validator.fieldToRegexp.getValue("dateOfBirth").matches(input) shouldBe  output
            }
        }
    }

    feature("Object id validation") {

        scenario("valid id check should be true") {
            validator.validateId("5db16c79c1e0900776112a42") shouldBe true
        }

        scenario("invalid ids check should be false") {
            table (
                    headers("input", "output"),
                    row("123",false),
                    row("",false),
                    row(null,false)
            ).forAll {
                input, output -> validator.validateId(input) shouldBe output
            }
        }
    }

    feature("Validation if mandatory fields are present") {

        scenario("all mandatory fields are present") {
            val input = mapOf("name" to "Ivan", "dateOfBirth" to "2019-10-21", "email" to "correct@mail.com")
            validator.validateMandatory(input) shouldBe listOf()
        }

        scenario("mandatory field is absent") {
            val input = mapOf("dateOfBirth" to "2019-10-21", "email" to "correct@mail.com")
            validator.validateMandatory(input) shouldBe listOf("name")
        }

        scenario("no fields in input") {
            validator.validateMandatory(mapOf()) shouldBe listOf("name", "email")
        }

    }
})