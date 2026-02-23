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
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCECodeChallengeMethod;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests du constructeur d'URL d'autorisation {@link AuthorizationUrlBuilder}. */
public class AuthorizationUrlBuilderTest {

  private OAuth20Service service;

  /** Initialisation du service. */
  @BeforeEach
  public void setUp() {
    final DefaultApi20 api =
        new DefaultApi20() {
          @Override
          public String getAccessTokenEndpoint() {
            return "http://test.com/token";
          }

          @Override
          public String getAuthorizationBaseUrl() {
            return "https://test.com/auth";
          }
        };
    service =
        new OAuth20Service(
            api,
            "api-key",
            "api-secret",
            "callback",
            "default-scope",
            "code",
            null,
            null,
            null,
            null);
  }

  /** Vérifie la construction d'une URL d'autorisation simple. */
  @Test
  public void shouldBuildSimpleAuthorizationUrl() {
    final String url = service.createAuthorizationUrlBuilder().build();
    assertThat(url).contains("response_type=code");
    assertThat(url).contains("client_id=api-key");
    assertThat(url).contains("redirect_uri=callback");
    assertThat(url).contains("scope=default-scope");
  }

  /** Vérifie la surcharge de la portée (scope) et de l'état (state). */
  @Test
  public void shouldOverrideScopeAndState() {
    final String url =
        service.createAuthorizationUrlBuilder().scope("custom-scope").state("custom-state").build();
    assertThat(url).contains("scope=custom-scope");
    assertThat(url).contains("state=custom-state");
  }

  /** Vérifie l'ajout de paramètres supplémentaires à l'URL. */
  @Test
  public void shouldAddAdditionalParameters() {
    final Map<String, String> params = new HashMap<>();
    params.put("display", "page");
    params.put("prompt", "login");

    final String url = service.createAuthorizationUrlBuilder().additionalParams(params).build();
    assertThat(url).contains("display=page");
    assertThat(url).contains("prompt=login");
  }

  /** Vérifie le support de PKCE (RFC 7636). */
  @Test
  public void shouldSupportPKCE() {
    final PKCE pkce = new PKCE();
    pkce.setCodeChallenge("challenge123");
    pkce.setCodeChallengeMethod(PKCECodeChallengeMethod.S256);
    pkce.setCodeVerifier("verifier123");

    final String url = service.createAuthorizationUrlBuilder().pkce(pkce).build();
    assertThat(url).contains("code_challenge=challenge123");
    assertThat(url).contains("code_challenge_method=S256");
  }

  /** Vérifie l'initialisation automatique de PKCE. */
  @Test
  public void shouldInitPKCEAutomatically() {
    final AuthorizationUrlBuilder builder = service.createAuthorizationUrlBuilder().initPKCE();
    final String url = builder.build();
    assertThat(url).contains("code_challenge=");
    assertThat(url).contains("code_challenge_method=S256");
    assertThat(builder.getPkce().getCodeVerifier()).isNotNull();
  }
}
