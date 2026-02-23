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
package com.github.scribejava.core.oauth;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.*;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** Handles OAuth 2.0 Pushed Authorization Requests (PAR). */

/**
 * Gère les requêtes d'autorisation poussées (PAR - Pushed Authorization Requests).
 *
 * @see <a href="https://tools.ietf.org/html/rfc9126">RFC 9126</a>
 */
public class OAuth20PushedAuthHandler {

  private final OAuth20Service service;
  // Clé de cache : Hash des paramètres triés. Valeur : Réponse mise en cache avec expiration.
  private final Map<Integer, CachedResponse> cache = new ConcurrentHashMap<>();

  /**
   * Constructeur.
   *
   * @param service Le service OAuth 2.0 associé.
   */
  public OAuth20PushedAuthHandler(OAuth20Service service) {
    this.service = service;
  }

  /**
   * Crée la requête HTTP pour l'appel au point de terminaison PAR.
   *
   * @param responseType Le type de réponse.
   * @param apiKey Le Client ID.
   * @param callback L'URI de redirection.
   * @param scope La portée demandée.
   * @param state L'état opaque.
   * @param additionalParams Paramètres additionnels.
   * @return Une {@link OAuthRequest} configurée.
   */
  public OAuthRequest createPushedAuthorizationRequest(
      String responseType,
      String apiKey,
      String callback,
      String scope,
      String state,
      Map<String, String> additionalParams) {
    final OAuthRequest request =
        new OAuthRequest(Verb.POST, service.getApi().getPushedAuthorizationRequestEndpoint());

    // 1. Collect all parameters in a map
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

    // 2. Apply strategy (JAR, etc.)
    final Map<String, String> convertedParams =
        service.getAuthorizationRequestConverter().convert(parameters.asMap());

    // 3. Add to request
    convertedParams.forEach(request::addParameter);

    service
        .getApi()
        .getClientAuthentication()
        .addClientAuthentication(request, service.getApiKey(), service.getApiSecret());
    return request;
  }

  /**
   * Envoie la requête PAR de manière asynchrone avec gestion du cache.
   *
   * @param responseType Le type de réponse.
   * @param apiKey Le Client ID.
   * @param callback L'URI de redirection.
   * @param scope La portée.
   * @param state L'état.
   * @param additionalParams Paramètres additionnels.
   * @param callbackConsumer Rappel optionnel.
   * @return Un futur résolvant vers {@link PushedAuthorizationResponse}.
   */
  public CompletableFuture<PushedAuthorizationResponse> pushAuthorizationRequestAsync(
      String responseType,
      String apiKey,
      String callback,
      String scope,
      String state,
      Map<String, String> additionalParams,
      OAuthAsyncRequestCallback<PushedAuthorizationResponse> callbackConsumer) {
    final String parEndpoint = service.getApi().getPushedAuthorizationRequestEndpoint();
    if (parEndpoint == null) {
      final CompletableFuture<PushedAuthorizationResponse> future = new CompletableFuture<>();
      future.completeExceptionally(
          new UnsupportedOperationException(
              "This API doesn't support Pushed Authorization Requests"));
      return future;
    }

    final OAuthRequest request =
        createPushedAuthorizationRequest(
            responseType, apiKey, callback, scope, state, additionalParams);

    // Calculate cache key based on request parameters
    final int cacheKey =
        Objects.hash(
            request.getQueryStringParams().asFormUrlEncodedString(),
            request.getBodyParams().asFormUrlEncodedString());

    final CachedResponse cached = cache.get(cacheKey);
    if (cached != null && cached.isValid()) {
      if (callbackConsumer != null) {
        callbackConsumer.onCompleted(cached.response);
      }
      return CompletableFuture.completedFuture(cached.response);
    }
    return service.execute(
        request,
        callbackConsumer,
        response -> {
          try (Response resp = response) {
            if (resp.getCode() != 201 && resp.getCode() != 200) {
              throw new OAuthException(
                  "Failed to push authorization request. Status: "
                      + resp.getCode()
                      + ", Body: "
                      + resp.getBody());
            }
            final PushedAuthorizationResponse parResponse =
                PushedAuthorizationResponse.parse(resp.getBody());
            cache.put(cacheKey, new CachedResponse(parResponse));
            return parResponse;
          }
        });
  }

  private static class CachedResponse {
    final PushedAuthorizationResponse response;
    final long expirationTime;

    CachedResponse(PushedAuthorizationResponse response) {
      this.response = response;
      // Expire en secondes. Conversion en millis. Marge de sécurité : 5 secondes.
      this.expirationTime =
          System.currentTimeMillis() + (long) (response.getExpiresIn() - 5) * 1000;
    }

    boolean isValid() {
      return System.currentTimeMillis() < expirationTime;
    }
  }
}
