package com.example.test.utils


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.io.Closeable
//TODO check timeout
class HttpClientFacade : Closeable {

    private val jsonMapper = jacksonObjectMapper()

    private val client = HttpClients.createDefault()

    override fun close() {
        client.close()
    }

    /**
     * Sends http request and simply retrieves http response as map of key -> value.
     * @return Pair of response status and its body
     */
    fun sendPost(url: String,  params: Map<String, Any>) : HttpResponse {
        val httpPost = HttpPost(url)
        httpPost.entity = StringEntity(jsonMapper.writeValueAsString(params), ContentType.APPLICATION_JSON)
        return client.execute(httpPost).use {
            HttpResponse(it.statusLine.statusCode, jsonMapper.readValue(EntityUtils.toString(it.entity)))
        }
    }

    fun sendPost(url: String,  params: String) : HttpResponse {
        val httpPost = HttpPost(url)
        httpPost.entity = StringEntity(params, ContentType.APPLICATION_JSON)
        return client.execute(httpPost).use {
            HttpResponse(it.statusLine.statusCode, jsonMapper.readValue(EntityUtils.toString(it.entity)))
        }
    }

    fun sendGet(url: String) : HttpResponse {
        return client.execute(HttpGet(url)).use {
            HttpResponse(it.statusLine.statusCode, jsonMapper.readValue(EntityUtils.toString(it.entity)))
        }
    }


}

data class HttpResponse(val status: Int, val body : Map<String, Any>)