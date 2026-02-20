package com.github.scribejava.oauth1.model;


/**
 * OAuth 1.0a Access Token.
 */
public class OAuth1AccessToken extends OAuth1Token {

    public OAuth1AccessToken(String token, String tokenSecret) {
        this(token, tokenSecret, null);
    }

    public OAuth1AccessToken(String token, String tokenSecret, String rawResponse) {
        super(token, tokenSecret, rawResponse);
    }

    public boolean isEmpty() {
        return getToken() == null || getToken().isEmpty();
    }
}
