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
import com.github.scribejava.core.model.JsonBuilder;
import com.github.scribejava.oidc.model.OidcKey;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests du service de découverte (Discovery) OIDC. */
public class OidcDiscoveryServiceTest {

  private MockWebServer server;
  private OidcDiscoveryService service;

  /**
   * Initialisation du serveur et du service.
   *
   * @throws IOException en cas d'erreur.
   */
  @BeforeEach
  public void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    service =
        new OidcDiscoveryService(
            server.url("/").toString(), new JDKHttpClient(), "ScribeJava-Test");
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
   * Vérifie la récupération des métadonnées du fournisseur via JsonBuilder.
   *
   * @throws Exception en cas d'erreur
   */
  @Test
  public void shouldFetchMetadata() throws Exception {
    final String json =
        new JsonBuilder()
            .add("issuer", server.url("/").toString())
            .add("authorization_endpoint", server.url("/authorize").toString())
            .add("token_endpoint", server.url("/token").toString())
            .add("jwks_uri", server.url("/jwks.json").toString())
            .add("response_types_supported", Collections.singletonList("code"))
            .add("subject_types_supported", Collections.singletonList("public"))
            .add("id_token_signing_alg_values_supported", Collections.singletonList("RS256"))
            .build();

    server.enqueue(new MockResponse().setBody(json).setResponseCode(200));

    final OidcProviderMetadata metadata = service.getProviderMetadata();

    assertThat(metadata).isNotNull();
    assertThat(metadata.getIssuer()).isEqualTo(server.url("/").toString());
    assertThat(metadata.getTokenEndpoint()).isEqualTo(server.url("/token").toString());
  }

  /**
   * Vérifie la récupération des clés JWKS via JsonBuilder.
   *
   * @throws Exception en cas d'erreur
   */
  @Test
  public void shouldFetchJwks() throws Exception {
    final String jwksJson =
        new JsonBuilder()
            .add(
                "keys",
                Collections.singletonList(
                    new JsonBuilder()
                        .add("kty", "RSA")
                        .add("use", "sig")
                        .add("kid", "123")
                        .add(
                            "n",
                            "AKZdf_vFrIs_Y_nd9Z6X_m_Z_u1P9f_vFrIs_Y_nd9Z6X_m_Z_u1P9f_vFrIs_Y_nd9Z6X_m_Z_u1P9f_vFrIs_Y_nd9Z6X_m_Z_")
                        .add("e", "AQAB")))
            .build();

    server.enqueue(new MockResponse().setBody(jwksJson).setResponseCode(200));

    final Map<String, OidcKey> keys = service.getJwks(server.url("/jwks.json").toString());

    assertThat(keys).isNotNull();
    assertThat(keys.get("123")).isNotNull();
    assertThat(keys.get("123").getKid()).isEqualTo("123");
  }
}
