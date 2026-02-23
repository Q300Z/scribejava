/*
 * The MIT License
 *
 * Copyright (c) 2010 Pablo Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.scribejava.oauth1.model;

import com.github.scribejava.core.model.Token;

import java.util.Objects;

/**
 * Classe abstraite pour les jetons OAuth 1.0a.
 */
public abstract class OAuth1Token extends Token {

    private final String token;
    private final String tokenSecret;

    /**
     * Constructeur.
     *
     * @param token       La valeur du jeton.
     * @param tokenSecret Le secret du jeton.
     * @param rawResponse La réponse brute du serveur.
     */
    public OAuth1Token(String token, String tokenSecret, String rawResponse) {
        super(rawResponse);
        this.token = token;
        this.tokenSecret = tokenSecret;
    }

    /**
     * @return La valeur du jeton.
     */
    public String getToken() {
        return token;
    }

    /**
     * @return Le secret du jeton.
     */
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
