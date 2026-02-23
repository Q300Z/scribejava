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
package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Représente la concession de type "Resource Owner Password Credentials" (identifiants du
 * propriétaire de la ressource).
 *
 * <p>Ce type de concession est utilisé lorsque le propriétaire de la ressource a un haut degré de
 * confiance envers le client et que d'autres types de concessions ne sont pas disponibles.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-1.3.3">RFC 6749, Section 1.3.3
 * (Resource Owner Password Credentials)</a>
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.3">RFC 6749, Section 4.3 (Resource
 * Owner Password Credentials Grant)</a>
 */
public class PasswordGrant implements OAuth20Grant {

    private final String username;
    private final String password;
    private final String scope;

    /**
     * Constructeur simple.
     *
     * @param username Le nom d'utilisateur du propriétaire de la ressource.
     * @param password Le mot de passe du propriétaire de la ressource.
     */
    public PasswordGrant(String username, String password) {
        this(username, password, null);
    }

    /**
     * Constructeur avec portée (scope) spécifique.
     *
     * @param username Le nom d'utilisateur du propriétaire de la ressource.
     * @param password Le mot de passe du propriétaire de la ressource.
     * @param scope    La portée de la demande d'accès.
     */
    public PasswordGrant(String username, String password, String scope) {
        this.username = username;
        this.password = password;
        this.scope = scope;
    }

    @Override
    public OAuthRequest createRequest(OAuth20Service service) {
        final OAuthRequest request =
                new OAuthRequest(
                        service.getApi().getAccessTokenVerb(), service.getApi().getAccessTokenEndpoint());

        request.addParameter(OAuthConstants.USERNAME, username);
        request.addParameter(OAuthConstants.PASSWORD, password);

        if (scope != null) {
            request.addParameter(OAuthConstants.SCOPE, scope);
        } else if (service.getDefaultScope() != null) {
            request.addParameter(OAuthConstants.SCOPE, service.getDefaultScope());
        }

        request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.PASSWORD);

        service
                .getApi()
                .getClientAuthentication()
                .addClientAuthentication(request, service.getApiKey(), service.getApiSecret());

        return request;
    }
}
