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

import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth2.OAuth2Error;
import com.github.scribejava.core.oauth2.grant.DeviceCodeGrant;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/** Handles OAuth 2.0 Device Authorization Grant flow. */
/**
 * Gère le flux de concession d'autorisation pour les appareils (Device Authorization Grant).
 *
 * @see <a href="https://tools.ietf.org/html/rfc8628">RFC 8628</a>
 */
public class OAuth20DeviceFlowHandler {

  private final OAuth20Service service;

  /**
   * Constructeur.
   *
   * @param service Le service OAuth 2.0 associé.
   */
  public OAuth20DeviceFlowHandler(OAuth20Service service) {
    this.service = service;
  }

  /**
   * Crée la requête pour obtenir les codes d'autorisation d'appareil.
   *
   * @param scope La portée demandée.
   * @return Une {@link OAuthRequest} configurée.
   */
  public OAuthRequest createDeviceAuthorizationCodesRequest(String scope) {
    final OAuthRequest request =
        new OAuthRequest(Verb.POST, service.getApi().getDeviceAuthorizationEndpoint());
    request.addParameter(OAuthConstants.CLIENT_ID, service.getApiKey());
    if (scope != null) {
      request.addParameter(OAuthConstants.SCOPE, scope);
    } else if (service.getDefaultScope() != null) {
      request.addParameter(OAuthConstants.SCOPE, service.getDefaultScope());
    }
    return request;
  }

  /**
   * Récupère les codes d'autorisation d'appareil de manière synchrone.
   *
   * @param scope La portée demandée.
   * @return Un objet {@link DeviceAuthorization}.
   * @throws InterruptedException si le thread est interrompu.
   * @throws ExecutionException si la requête échoue.
   * @throws IOException en cas d'erreur réseau.
   */
  public DeviceAuthorization getDeviceAuthorizationCodes(String scope)
      throws InterruptedException, ExecutionException, IOException {
    final OAuthRequest request = createDeviceAuthorizationCodesRequest(scope);

    try (Response response = service.execute(request)) {
      if (service.isDebug()) {
        service.log("got DeviceAuthorizationCodes response");
        service.log("response status code: %s", response.getCode());
        service.log("response body: %s", response.getBody());
      }
      return service.getApi().getDeviceAuthorizationExtractor().extract(response);
    }
  }

  /**
   * Interroge périodiquement le serveur pour obtenir le jeton d'accès (Polling).
   *
   * @param deviceAuthorization L'objet contenant le device_code.
   * @return Le jeton d'accès {@link OAuth2AccessToken}.
   * @throws InterruptedException si le thread est interrompu.
   * @throws ExecutionException si la requête échoue.
   * @throws IOException en cas d'erreur réseau.
   */
  public OAuth2AccessToken pollAccessTokenDeviceAuthorizationGrant(
      DeviceAuthorization deviceAuthorization)
      throws InterruptedException, ExecutionException, IOException {
    long intervalMillis = deviceAuthorization.getIntervalSeconds() * 1000;
    while (true) {
      try {
        return service.getAccessToken(new DeviceCodeGrant(deviceAuthorization.getDeviceCode()));
      } catch (OAuth2AccessTokenErrorResponse e) {
        if (e.getError() != OAuth2Error.AUTHORIZATION_PENDING) {
          if (e.getError() == OAuth2Error.SLOW_DOWN) {
            intervalMillis += 5000;
          } else {
            throw e;
          }
        }
      }
      Thread.sleep(intervalMillis);
    }
  }
}
