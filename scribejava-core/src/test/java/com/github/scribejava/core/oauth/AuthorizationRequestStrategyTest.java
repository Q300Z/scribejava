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

import static org.assertj.core.api.Assertions.assertThat;

import com.github.scribejava.core.builder.api.DefaultApi20;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class AuthorizationRequestStrategyTest {

  @Test
  public void shouldApplyCustomAuthorizationStrategy() {
    // 1. Définition d'une stratégie "Mock JAR" (simule une encapsulation)
    AuthorizationRequestConverter mockJarConverter =
        params -> {
          Map<String, String> newParams = new HashMap<>();
          // On garde client_id en clair (souvent requis même avec JAR)
          if (params.containsKey("client_id")) {
            newParams.put("client_id", params.get("client_id"));
          }
          // On simule que tout le reste est dans un "request"
          newParams.put("request", "mock_signed_jwt_content");
          return newParams;
        };

    // 2. Création du service avec cette stratégie
    // Note: Cette méthode 'setAuthorizationRequestConverter' n'existe pas encore
    OAuth20Service service =
        new OAuth20Service(
            new DefaultApi20() {
              @Override
              public String getAccessTokenEndpoint() {
                return "http://token";
              }

              @Override
              public String getAuthorizationBaseUrl() {
                return "http://auth";
              }
            },
            "client_id",
            "secret",
            "http://callback",
            "scope",
            null,
            null,
            null,
            null,
            null);

    service.setAuthorizationRequestConverter(mockJarConverter);

    // 3. Exécution
    String url = service.getAuthorizationUrl();

    // 4. Vérification
    // L'URL doit contenir 'request=' et 'client_id='
    // Elle NE DOIT PAS contenir 'scope=' ou 'redirect_uri=' car ils sont censés être dans le JWT
    assertThat(url).contains("request=mock_signed_jwt_content");
    assertThat(url).contains("client_id=client_id");
    assertThat(url).doesNotContain("scope=scope");
    assertThat(url).doesNotContain("redirect_uri=http%3A%2F%2Fcallback");
  }
}
