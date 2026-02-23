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
 * Représente la concession de type "Client Credentials" (identifiants du client).
 *
 * <p>Ce type de concession est utilisé typiquement lorsque le client agit en son propre nom (le
 * client est aussi le propriétaire de la ressource) ou demande l'accès à des ressources protégées
 * sur la base d'une autorisation préalablement arrangée avec le serveur d'autorisation.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-1.3.4">RFC 6749, Section 1.3.4 (Client
 * Credentials)</a>
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.4">RFC 6749, Section 4.4 (Client
 * Credentials Grant)</a>
 */
public class ClientCredentialsGrant implements OAuth20Grant {

    private final String scope;

    /**
     * Constructeur par défaut.
     */
    public ClientCredentialsGrant() {
        this(null);
    }

    /**
     * Constructeur avec une portée (scope) spécifique.
     *
     * @param scope La portée de la demande d'accès.
     */
    public ClientCredentialsGrant(String scope) {
        this.scope = scope;
    }

    @Override
    public OAuthRequest createRequest(OAuth20Service service) {
        final OAuthRequest request =
                new OAuthRequest(
                        service.getApi().getAccessTokenVerb(), service.getApi().getAccessTokenEndpoint());

        service
                .getApi()
                .getClientAuthentication()
                .addClientAuthentication(request, service.getApiKey(), service.getApiSecret());

        if (scope != null) {
            request.addParameter(OAuthConstants.SCOPE, scope);
        } else if (service.getDefaultScope() != null) {
            request.addParameter(OAuthConstants.SCOPE, service.getDefaultScope());
        }
        request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.CLIENT_CREDENTIALS);

        return request;
    }
}
