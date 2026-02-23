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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests de sécurité pour le service de découverte OIDC. */
public class OidcDiscoveryServiceSecurityTest {

  private MockWebServer server;

  /**
   * Initialisation du serveur.
   *
   * @throws IOException en cas d'erreur.
   */
  @BeforeEach
  public void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
  }

  /**
   * Arrêt du serveur.
   *
   * @throws IOException en cas d'erreur.
   */
  @AfterEach
  public void tearDown() throws IOException {
    server.shutdown();
  }

  /**
   * Vérifie que le service rejette les métadonnées si l'émetteur (issuer) ne correspond pas à l'URL
   * demandée.
   *
   * @see <a
   *     href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfigurationValidation">OpenID
   *     Connect Discovery 1.0, Section 4.3</a>
   */
  @Test
  public void shouldRejectMetadataWithMismatchingIssuer() {
    final String issuer = server.url("/").toString();
    final String discoveryJson =
        "{"
            + "\"issuer\":\"https://malicious-issuer.com\","
            + "\"authorization_endpoint\":\""
            + issuer
            + "auth\","
            + "\"token_endpoint\":\""
            + issuer
            + "token\","
            + "\"jwks_uri\":\""
            + issuer
            + "keys\""
            + "}";

    server.enqueue(new MockResponse().setBody(discoveryJson).setResponseCode(200));

    final OidcDiscoveryService discoveryService =
        new OidcDiscoveryService(issuer, new JDKHttpClient(), "ScribeJava");
    final ExecutionException ex =
        assertThrows(ExecutionException.class, discoveryService::getProviderMetadata);
    assertThat(ex.getCause()).isInstanceOf(OAuthException.class);
    assertThat(ex.getCause().getMessage()).contains("Issuer mismatch");
  }

  /** Vérifie la validation manuelle de l'émetteur dans le service OIDC. */
  @Test
  public void shouldRejectIssuerMismatch() {
    final OidcService oidcService =
        new OidcService(
            null,
            "client-id",
            "secret",
            "callback",
            null,
            "code",
            null,
            null,
            null,
            new JDKHttpClient(),
            null);
    assertThrows(
        OAuthException.class,
        () ->
            oidcService.validateIssuerResponse(
                "https://malicious.com", "https://expected-idp.com"));
  }
}
