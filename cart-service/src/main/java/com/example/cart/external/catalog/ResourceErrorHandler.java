package com.example.cart.external.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.Charset;


public class ResourceErrorHandler implements ResponseErrorHandler {

    Logger log = LoggerFactory.getLogger(ResourceErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        return clientHttpResponse.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        if (clientHttpResponse.getStatusCode().isError()) {
            String body = StreamUtils.copyToString(clientHttpResponse.getBody(), Charset.defaultCharset());
            throw new ResourceException(clientHttpResponse.getStatusCode(), body);
        }
    }
}
