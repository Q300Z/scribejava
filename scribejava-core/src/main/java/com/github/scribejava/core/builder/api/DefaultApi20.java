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
package com.github.scribejava.core.builder.api;

import com.github.scribejava.core.dpop.DPoPProofCreator;
import com.github.scribejava.core.extractors.DeviceAuthorizationJsonExtractor;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.ParameterList;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.bearersignature.BearerSignature;
import com.github.scribejava.core.oauth2.bearersignature.BearerSignatureAuthorizationRequestHeaderField;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;

import java.io.OutputStream;
import java.util.Map;

/**
 * Implémentation par défaut du protocole OAuth, version 2.0.
 *
 * <p>Cette classe abstraite définit la structure de base pour toutes les APIs OAuth 2.0, incluant
 * les points de terminaison, les extracteurs de jetons et les méthodes de signature.
 */
public abstract class DefaultApi20 {

    /**
     * Retourne l'extracteur de jeton d'accès.
     *
     * @return L'instance de {@link TokenExtractor} pour {@link OAuth2AccessToken}.
     */
    public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return OAuth2AccessTokenJsonExtractor.instance();
    }

    /**
     * Retourne le verbe HTTP pour le point de terminaison de jeton d'accès (POST par défaut).
     *
     * @return Le verbe {@link Verb}.
     */
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    /**
     * Retourne l'URL qui reçoit les requêtes de jeton d'accès.
     *
     * @return L'URL du point de terminaison de jeton.
     */
    public abstract String getAccessTokenEndpoint();

    /**
     * Retourne l'URL pour le renouvellement de jeton (identique au jeton d'accès par défaut).
     *
     * @return L'URL du point de terminaison de renouvellement.
     */
    public String getRefreshTokenEndpoint() {
        return getAccessTokenEndpoint();
    }

    /**
     * Retourne le point de terminaison de révocation de jeton (RFC 7009).
     *
     * @return L'URL de révocation.
     * @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009</a>
     */
    public String getRevokeTokenEndpoint() {
        throw new UnsupportedOperationException(
                "This API doesn't support revoking tokens or we have no info about this");
    }

    /**
     * Retourne le point de terminaison PAR (RFC 9126).
     *
     * @return L'URL PAR ou null si non supporté.
     * @see <a href="https://tools.ietf.org/html/rfc9126">RFC 9126</a>
     */
    public String getPushedAuthorizationRequestEndpoint() {
        return null;
    }

    /**
     * Retourne l'URL de base pour l'autorisation.
     *
     * @return L'URL d'autorisation.
     */
    public abstract String getAuthorizationBaseUrl();

    /**
     * Génère l'URL d'autorisation complète.
     *
     * @param responseType     Le type de réponse.
     * @param apiKey           Le Client ID.
     * @param callback         L'URI de redirection.
     * @param scope            La portée demandée.
     * @param state            L'état opaque.
     * @param additionalParams Paramètres additionnels.
     * @return L'URL d'autorisation.
     */
    public String getAuthorizationUrl(
            String responseType,
            String apiKey,
            String callback,
            String scope,
            String state,
            Map<String, String> additionalParams) {
        final ParameterList parameters = new ParameterList(additionalParams);
        parameters.add(OAuthConstants.RESPONSE_TYPE, responseType);
        parameters.add(OAuthConstants.CLIENT_ID, apiKey);

        if (callback != null) {
            parameters.add(OAuthConstants.REDIRECT_URI, callback);
        }

        if (scope != null) {
            parameters.add(OAuthConstants.SCOPE, scope);
        }

        if (state != null) {
            parameters.add(OAuthConstants.STATE, state);
        }

        return parameters.appendTo(getAuthorizationBaseUrl());
    }

    /**
     * Crée l'instance de service OAuth 2.0.
     *
     * @param apiKey           Client ID.
     * @param apiSecret        Client Secret.
     * @param callback         Redirect URI.
     * @param defaultScope     Portée par défaut.
     * @param responseType     Type de réponse.
     * @param debugStream      Flux de débogage.
     * @param userAgent        User-Agent.
     * @param httpClientConfig Configuration HTTP.
     * @param httpClient       Client HTTP.
     * @return Une instance de {@link OAuth20Service}.
     */
    public OAuth20Service createService(
            String apiKey,
            String apiSecret,
            String callback,
            String defaultScope,
            String responseType,
            OutputStream debugStream,
            String userAgent,
            HttpClientConfig httpClientConfig,
            HttpClient httpClient) {
        return new OAuth20Service(
                this,
                apiKey,
                apiSecret,
                callback,
                defaultScope,
                responseType,
                debugStream,
                userAgent,
                httpClientConfig,
                httpClient);
    }

    /**
     * Crée l'instance de service OAuth 2.0 supportant DPoP.
     *
     * @param apiKey           Client ID.
     * @param apiSecret        Client Secret.
     * @param callback         Redirect URI.
     * @param defaultScope     Portée par défaut.
     * @param responseType     Type de réponse.
     * @param debugStream      Flux de débogage.
     * @param userAgent        User-Agent.
     * @param httpClientConfig Configuration HTTP.
     * @param httpClient       Client HTTP.
     * @param dpopProofCreator Créateur de preuves DPoP.
     * @return Une instance de {@link OAuth20Service}.
     */
    public OAuth20Service createService(
            String apiKey,
            String apiSecret,
            String callback,
            String defaultScope,
            String responseType,
            OutputStream debugStream,
            String userAgent,
            HttpClientConfig httpClientConfig,
            HttpClient httpClient,
            DPoPProofCreator dpopProofCreator) {
        return new OAuth20Service(
                this,
                apiKey,
                apiSecret,
                callback,
                defaultScope,
                responseType,
                debugStream,
                userAgent,
                httpClientConfig,
                httpClient,
                dpopProofCreator);
    }

    /**
     * Retourne le mécanisme de signature Bearer (RFC 6750).
     *
     * @return L'instance de {@link BearerSignature}.
     */
    public BearerSignature getBearerSignature() {
        return BearerSignatureAuthorizationRequestHeaderField.instance();
    }

    /**
     * Retourne le mécanisme d'authentification du client (RFC 6749).
     *
     * @return L'instance de {@link ClientAuthentication}.
     */
    public ClientAuthentication getClientAuthentication() {
        return HttpBasicAuthenticationScheme.instance();
    }

    /**
     * Retourne le point de terminaison pour l'autorisation d'appareil (RFC 8628).
     *
     * @return L'URL du point de terminaison.
     * @see <a href="https://tools.ietf.org/html/rfc8628">RFC 8628</a>
     */
    public String getDeviceAuthorizationEndpoint() {
        throw new UnsupportedOperationException(
                "This API doesn't support Device Authorization Grant or we have no info about this");
    }

    /**
     * Retourne l'extracteur pour l'autorisation d'appareil.
     *
     * @return L'instance de {@link DeviceAuthorizationJsonExtractor}.
     */
    public DeviceAuthorizationJsonExtractor getDeviceAuthorizationExtractor() {
        return DeviceAuthorizationJsonExtractor.instance();
    }
}
