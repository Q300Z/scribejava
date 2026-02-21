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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Service for Dynamic Client Registration (RFC 7591 & OIDC Registration). */
public class OidcRegistrationService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final HttpClient httpClient;
  private final String userAgent;

  public OidcRegistrationService(final HttpClient httpClient, final String userAgent) {
    this.httpClient = httpClient;
    this.userAgent = userAgent;
  }

  public CompletableFuture<JsonNode> registerClientAsync(
      final String registrationEndpoint,
      final List<String> redirectUris,
      final String clientName,
      final String tokenEndpointAuthMethod) {
    final ObjectNode registrationRequest = OBJECT_MAPPER.createObjectNode();
    final ArrayNode redirectUrisNode = registrationRequest.putArray("redirect_uris");
    redirectUris.forEach(redirectUrisNode::add);
    registrationRequest.put("client_name", clientName);
    if (tokenEndpointAuthMethod != null) {
      registrationRequest.put("token_endpoint_auth_method", tokenEndpointAuthMethod);
    }

    final OAuthRequest request = new OAuthRequest(Verb.POST, registrationEndpoint);
    request.setPayload(registrationRequest.toString());
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
            return OBJECT_MAPPER.readTree(resp.getBody());
          } catch (final IOException e) {
            throw new OAuthException("Error parsing registration response", e);
          }
        });
  }
}
