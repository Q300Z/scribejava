package com.github.scribejava.core.exceptions;

/**
 * Exception representing a general OAuth protocol error (e.g. malformed JSON, unexpected parameters).
 */
public class OAuthProtocolException extends OAuthException {

    private static final long serialVersionUID = 1L;

    public OAuthProtocolException(String message) {
        super(message);
    }

    public OAuthProtocolException(String message, Exception cause) {
        super(message, cause);
    }
}
