package com.github.scribejava.core.exceptions;

import com.github.scribejava.core.model.OAuthResponseException;
import com.github.scribejava.core.model.Response;
import java.io.IOException;

/**
 * Exception thrown when the API server returns a 429 (Too Many Requests) or a rate limit error.
 */
public class OAuthRateLimitException extends OAuthResponseException {

    private static final long serialVersionUID = 1L;

    public OAuthRateLimitException(Response response) throws IOException {
        super(response);
    }

    @Override
    public String getMessage() {
        try {
            return "Rate limit exceeded. Status: " + getResponse().getCode() + ", Body: " + getResponse().getBody();
        } catch (IOException e) {
            return "Rate limit exceeded. Status: " + getResponse().getCode() + " (could not read body)";
        }
    }
}
