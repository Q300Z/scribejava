package com.github.scribejava.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.utils.Preconditions;
import java.io.IOException;

/**
 * Represents the response from a Pushed Authorization Request (PAR) endpoint.
 */
public class PushedAuthorizationResponse {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String requestUri;
    private final Long expiresIn;
    private final String rawResponse;

    public PushedAuthorizationResponse(String requestUri, Long expiresIn, String rawResponse) {
        Preconditions.checkEmptyString(requestUri, "request_uri cannot be null or empty");
        this.requestUri = requestUri;
        this.expiresIn = expiresIn;
        this.rawResponse = rawResponse;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public static PushedAuthorizationResponse parse(String responseBody) throws IOException {
        Preconditions.checkEmptyString(responseBody, "Response body is incorrect. Can't parse an empty string.");

        final JsonNode body = OBJECT_MAPPER.readTree(responseBody);
        final JsonNode requestUri = body.get("request_uri");
        final JsonNode expiresIn = body.get("expires_in");

        if (requestUri == null || requestUri.isNull()) {
            throw new OAuthException("Response body is incorrect. Missing 'request_uri' parameter. Raw response: "
                    + responseBody);
        }

        return new PushedAuthorizationResponse(requestUri.asText(),
                expiresIn == null || expiresIn.isNull() ? null : expiresIn.asLong(),
                responseBody);
    }
}
