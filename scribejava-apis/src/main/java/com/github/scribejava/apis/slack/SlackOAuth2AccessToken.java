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
package com.github.scribejava.apis.slack;

import com.github.scribejava.core.model.OAuth2AccessToken;

import java.util.Objects;

/**
 * Jeton d'accès OAuth 2.0 spécifique à Slack, incluant le jeton d'accès de l'utilisateur.
 */
public class SlackOAuth2AccessToken extends OAuth2AccessToken {

    private static final long serialVersionUID = 1L;

    private final String userAccessToken;

    /**
     * Constructeur.
     *
     * @param accessToken     Le jeton d'accès de l'application.
     * @param tokenType       Le type de jeton.
     * @param expiresIn       Durée de validité.
     * @param refreshToken    Jeton de renouvellement.
     * @param scope           Portée.
     * @param userAccessToken Le jeton d'accès de l'utilisateur authentifié.
     * @param rawResponse     La réponse brute.
     */
    public SlackOAuth2AccessToken(
            String accessToken,
            String tokenType,
            Integer expiresIn,
            String refreshToken,
            String scope,
            String userAccessToken,
            String rawResponse) {
        super(accessToken, tokenType, expiresIn, refreshToken, scope, rawResponse);
        this.userAccessToken = userAccessToken;
    }

    /**
     * @return Le jeton d'accès de l'utilisateur.
     */
    public String getUserAccessToken() {
        return userAccessToken;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 37 * hash + Objects.hashCode(userAccessToken);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }

        return Objects.equals(userAccessToken, ((SlackOAuth2AccessToken) obj).getUserAccessToken());
    }
}
