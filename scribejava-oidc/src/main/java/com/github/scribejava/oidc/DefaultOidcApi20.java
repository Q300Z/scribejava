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

import com.github.scribejava.core.builder.api.DefaultApi20;

/**
 * Classe de base pour les APIs OpenID Connect 1.0.
 *
 * <p>Cette abstraction permet la découverte dynamique des points de terminaison via le mécanisme
 * OIDC Discovery 1.0 et la gestion des métadonnées du fournisseur.
 *
 * @see <a href="http://openid.net/specs/openid-connect-discovery-1_0.html">OpenID Connect Discovery
 * 1.0</a>
 */
public abstract class DefaultOidcApi20 extends DefaultApi20 {

    private OidcProviderMetadata metadata;

    /**
     * Retourne l'URL de l'émetteur (Issuer) pour cette API.
     *
     * @return L'URL de l'émetteur.
     * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#IDToken">OpenID Connect Core
     * 1.0, Section 2 (ID Token - iss claim)</a>
     */
    public abstract String getIssuer();

    /**
     * Retourne les métadonnées du fournisseur associées à cette API.
     *
     * @return Les métadonnées {@link OidcProviderMetadata}, ou null si non encore récupérées.
     */
    public OidcProviderMetadata getMetadata() {
        return metadata;
    }

    /**
     * Définit les métadonnées du fournisseur pour cette API.
     *
     * @param metadata Les métadonnées récupérées via le service de découverte.
     */
    public void setMetadata(final OidcProviderMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Retourne l'URL du point de terminaison de jeton (Token Endpoint).
     *
     * @return L'URL extraite des métadonnées.
     */
    @Override
    public String getAccessTokenEndpoint() {
        return metadata != null ? metadata.getTokenEndpoint() : null;
    }

    /**
     * Retourne l'URL de base pour l'autorisation (Authorization Endpoint).
     *
     * @return L'URL extraite des métadonnées.
     */
    @Override
    public String getAuthorizationBaseUrl() {
        return metadata != null ? metadata.getAuthorizationEndpoint() : null;
    }

    /**
     * Retourne l'URL du point de terminaison de révocation de jeton.
     *
     * @return L'URL extraite des métadonnées ou celle par défaut.
     * @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009 (Token Revocation)</a>
     */
    @Override
    public String getRevokeTokenEndpoint() {
        return metadata != null ? metadata.getRevocationEndpoint() : super.getRevokeTokenEndpoint();
    }

    /**
     * Retourne l'URL du point de terminaison PAR (Pushed Authorization Request).
     *
     * @return L'URL extraite des métadonnées ou celle par défaut.
     * @see <a href="https://tools.ietf.org/html/rfc9126">RFC 9126 (OAuth 2.0 PAR)</a>
     */
    @Override
    public String getPushedAuthorizationRequestEndpoint() {
        return metadata != null
                ? metadata.getPushedAuthorizationRequestEndpoint()
                : super.getPushedAuthorizationRequestEndpoint();
    }

    /**
     * Retourne l'URL du document JWK Set (jwks_uri).
     *
     * @return L'URL extraite des métadonnées.
     * @see <a href="http://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">OIDC
     * Discovery, jwks_uri</a>
     */
    public String getJwksUri() {
        return metadata != null ? metadata.getJwksUri() : null;
    }

    /**
     * Retourne l'URL du point de terminaison UserInfo.
     *
     * @return L'URL extraite des métadonnées.
     * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#UserInfo">OIDC Core, Section
     * 5.3</a>
     */
    public String getUserinfoEndpoint() {
        return metadata != null ? metadata.getUserinfoEndpoint() : null;
    }
}
