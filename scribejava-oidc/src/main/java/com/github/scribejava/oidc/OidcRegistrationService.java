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

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.utils.JsonUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Service gérant l'enregistrement dynamique des clients (Dynamic Client Registration) natif. */
public class OidcRegistrationService {

  private final HttpClient httpClient;
  private final String userAgent;

  /**
   * @param httpClient Le client HTTP à utiliser.
   * @param userAgent La chaîne User-Agent.
   */
  public OidcRegistrationService(final HttpClient httpClient, final String userAgent) {
    this.httpClient = httpClient;
    this.userAgent = userAgent;
  }

  /**
   * Enregistre un client de manière asynchrone auprès du fournisseur.
   *
   * @param registrationEndpoint L'URL du point de terminaison d'enregistrement.
   * @param redirectUris Liste des URIs de redirection autorisées pour ce client.
   * @param clientName Nom convivial du client.
   * @param tokenEndpointAuthMethod Méthode d'authentification souhaitée au point de terminaison de
   *     jeton.
   * @return Un {@link CompletableFuture} contenant la réponse du serveur sous forme de Map.
   */
  public CompletableFuture<Map<String, Object>> registerClientAsync(
      final String registrationEndpoint,
      final List<String> redirectUris,
      final String clientName,
      final String tokenEndpointAuthMethod) {
    final Map<String, Object> registrationRequest = new HashMap<>();
    registrationRequest.put("redirect_uris", redirectUris);
    registrationRequest.put("client_name", clientName);
    if (tokenEndpointAuthMethod != null) {
      registrationRequest.put("token_endpoint_auth_method", tokenEndpointAuthMethod);
    }

    final OAuthRequest request = new OAuthRequest(Verb.POST, registrationEndpoint);
    request.setPayload(JsonUtils.toJson(registrationRequest));
    request.addHeader("Content-Type", "application/json");

    return httpClient.executeAsync(
        userAgent,
        request.getHeaders(),
        request.getVerb(),
        request.getCompleteUrl(),
        request.getStringPayload(),
        null,
        response -> {
          try (Response resp = response) {
            if (resp.getCode() != 201 && resp.getCode() != 200) {
              throw new OAuthException(
                  "Client registration failed. Status: "
                      + resp.getCode()
                      + ", Body: "
                      + resp.getBody());
            }
            return JsonUtils.parse(resp.getBody());
          } catch (final IOException e) {
            throw new OAuthException("Error parsing registration response", e);
          }
        });
  }
}
