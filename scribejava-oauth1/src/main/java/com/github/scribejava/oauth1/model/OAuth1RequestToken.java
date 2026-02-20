package com.github.scribejava.oauth1.model;

/**
 * OAuth 1.0a Request Token.
 */
public class OAuth1RequestToken extends OAuth1Token {

    public OAuth1RequestToken(String token, String tokenSecret) {
        this(token, tokenSecret, null);
    }

    public OAuth1RequestToken(String token, String tokenSecret, String rawResponse) {
        super(token, tokenSecret, rawResponse);
    }
}
