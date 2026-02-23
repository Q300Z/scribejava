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

import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests de la configuration automatique des points de terminaison via Discovery. */
public class OidcApiDiscoveryTest {

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

  /** Vérifie que les points de terminaison sont correctement configurés à partir du JSON. */
  @Test
  public void shouldConfigureEndpointsAutomatically() throws Exception {
    final String issuer = server.url("/").toString();
    final String discoveryJson =
        "{"
            + "\"issuer\":\""
            + issuer
            + "\","
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

    final TestOidcApi api = new TestOidcApi(issuer);
    final OidcDiscoveryService discoveryService =
        new OidcDiscoveryService(issuer, new JDKHttpClient(), "ScribeJava");
    api.setMetadata(discoveryService.getProviderMetadata());

    assertThat(api.getAuthorizationBaseUrl()).isEqualTo(issuer + "auth");
    assertThat(api.getAccessTokenEndpoint()).isEqualTo(issuer + "token");
    assertThat(api.getJwksUri()).isEqualTo(issuer + "keys");
  }

  /** Classe d'API OIDC factice pour les tests. */
  public static class TestOidcApi extends DefaultOidcApi20 {
    private final String issuer;

    /**
     * Constructeur.
     *
     * @param issuer L'émetteur.
     */
    public TestOidcApi(final String issuer) {
      this.issuer = issuer;
    }
  }
}
