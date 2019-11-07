package com.example.customer.service

import com.example.customer.service.api.CustomerRequest
import com.example.customer.service.api.Status
import org.bson.types.ObjectId

/**
 * Validates possible input values came externally.
 */
interface CustomerRequestValidator {

    fun validateCreate(request : CustomerRequest) : Status

    fun validateUpdate(request : CustomerRequest) : Status

    fun validateId(id : String?) : Boolean
}

class CustomerRequestValidatorImpl : CustomerRequestValidator {
    //Approximately possible value. Probably not all security cases are covered.
    val fieldToRegexp = mapOf(
            /*"login" to Regex("[a-zA-Z0-9_.]+"),*/
            "name" to Regex("^[a-zA-Z0-9_ -]+$"),
            "email" to Regex("^[a-zA-Z0-9._]+@[a-zA-Z0-9.-]+$"), //approximate pattern
            "dateOfBirth" to Regex("^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$"))

    //all this fields must present in fieldToRegexp
    //allowed fields for Customer
    private val customerFields = setOf("name", "dateOfBirth", "email")
    //allowed for modification
    private val modifiableFields = setOf("name", "dateOfBirth")
    //mandatory for Customer
    private val requiredFields = setOf("name", "email")

    private fun validateFieldNames(allowedFields: Set<String>, fieldToValue: Map<String, String>) : List<String> {
        return fieldToValue.keys.filterNot {allowedFields.contains(it)}
    }

    private fun validateFieldName(fieldToValue: Map<String, String>) : List<String> {
        return validateFieldNames(customerFields, fieldToValue)
    }

    private fun validateFieldNamesOnUpdate(fields: Map<String, String>) : List<String> {
        return validateFieldNames(modifiableFields, fields)
    }

    private fun validateFieldValues(allowedFields: Set<String>, fieldToValue: Map<String, String>) : Map<String, String> {
        //get all (k -> v) that are not in allowedFields or not passed Regex matching
        return fieldToValue.filterNot {
            allowedFields.contains(it.key) && it.value.matches(fieldToRegexp[it.key]!!)
        }.toMap()
    }

    /**
     * Returns map of invalid fields(name -> value)
     */
    internal fun validateCreateValues(fields: Map<String, String>) : Map<String, String> {
        return validateFieldValues(customerFields, fields)
    }

    /**
     * Returns invalid fields
     */
    private fun validateUpdateValues(fields: Map<String, String>) : Map<String, String> {
        return validateFieldValues(modifiableFields, fields)
    }

    override fun validateId(id : String?) : Boolean {
        return id != null && ObjectId.isValid(id)
    }

    /**
     * Returns mandatory fields that don't present in fields map
     */
    fun validateMandatory(fields: Map<String, String>) : List<String> {
        return requiredFields.filterNot{ fields.containsKey(it) }
    }

    override fun validateCreate(request: CustomerRequest) : Status {
        val requiredNotFound = validateMandatory(request.fields)
        if (requiredNotFound.isNotEmpty()) {
            return Status.MandatoryFieldNotFound(requiredNotFound)
        }

        val invalidName = validateFieldName(request.fields)
        if (invalidName.isNotEmpty()) {
            return Status.FieldNameInvalid(invalidName)
        }
        val invalidValue = validateCreateValues(request.fields)
        if (invalidValue.isNotEmpty()) {
            return Status.FieldValueInvalid(invalidValue)
        }
        return Status.Success
    }

    override fun validateUpdate(request: CustomerRequest) : Status {
        if (!validateId(request.id)) {
            return Status.CustomerIdInvalid(request.id)
        }
        if (request.fields.isEmpty()) {
            return Status.NoFieldsToUpdate
        }
        //this part is the same as of validateCreate. refactor it
        val invalidName = validateFieldNamesOnUpdate(request.fields)
        if (invalidName.isNotEmpty()) {
            return Status.FieldNameInvalid(invalidName)
        }
        val invalidFields = validateUpdateValues(request.fields)
        return when (invalidFields.isNotEmpty()) {
            true -> Status.FieldValueInvalid(invalidFields)
            false -> Status.Success
        }
    }

}