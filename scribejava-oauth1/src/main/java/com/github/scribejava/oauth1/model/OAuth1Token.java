package com.github.scribejava.oauth1.model;

import com.github.scribejava.core.model.Token;
import java.util.Objects;

/**
 * Abstract class for OAuth 1.0a tokens.
 */
public abstract class OAuth1Token extends Token {

    private final String token;
    private final String tokenSecret;

    public OAuth1Token(String token, String tokenSecret, String rawResponse) {
        super(rawResponse);
        this.token = token;
        this.tokenSecret = tokenSecret;
    }

    public String getToken() {
        return token;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(token);
        hash = 29 * hash + Objects.hashCode(tokenSecret);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final OAuth1Token other = (OAuth1Token) obj;
        return Objects.equals(token, other.token) && Objects.equals(tokenSecret, other.tokenSecret);
    }
}
