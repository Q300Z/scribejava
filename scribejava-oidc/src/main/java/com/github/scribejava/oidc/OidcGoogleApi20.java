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
package com.github.scribejava.oidc;

/**
 * Implémentation de l'API Google OpenID Connect 1.0.
 *
 * <p>Cette classe utilise la structure {@link DefaultOidcApi20} pour supporter la découverte
 * dynamique et la validation des jetons d'identité (ID Tokens).
 *
 * @see <a href="https://developers.google.com/identity/protocols/oauth2/openid-connect">Google OIDC
 * Documentation</a>
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect Core 1.0</a>
 */
public class OidcGoogleApi20 extends DefaultOidcApi20 {

    /**
     * Constructeur protégé.
     */
    protected OidcGoogleApi20() {
    }

    /**
     * Retourne l'instance unique (singleton) de l'API Google OIDC.
     *
     * @return L'instance de {@link OidcGoogleApi20}.
     */
    public static OidcGoogleApi20 instance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Retourne l'identifiant de l'émetteur (Issuer) officiel de Google.
     *
     * @return {@code https://accounts.google.com}
     */
    @Override
    public String getIssuer() {
        return "https://accounts.google.com";
    }

    @Override
    public String getAccessTokenEndpoint() {
        final String endpoint = super.getAccessTokenEndpoint();
        return endpoint != null ? endpoint : "https://oauth2.googleapis.com/token";
    }

    @Override
    public String getAuthorizationBaseUrl() {
        final String baseUrl = super.getAuthorizationBaseUrl();
        return baseUrl != null ? baseUrl : "https://accounts.google.com/o/oauth2/v2/auth";
    }

    @Override
    public String getRevokeTokenEndpoint() {
        final String endpoint = super.getRevokeTokenEndpoint();
        return endpoint != null ? endpoint : "https://oauth2.googleapis.com/revoke";
    }

    private static class InstanceHolder {
        private static final OidcGoogleApi20 INSTANCE = new OidcGoogleApi20();
    }
}
