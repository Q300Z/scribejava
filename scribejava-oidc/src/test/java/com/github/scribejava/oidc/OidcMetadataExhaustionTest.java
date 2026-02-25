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

import com.github.scribejava.core.model.JsonBuilder;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Test d'exhaustion pour OidcProviderMetadata (Couverture maximale). */
public class OidcMetadataExhaustionTest {

  /**
   * Vérifie tous les getters des métadonnées avec un JSON généré proprement.
   *
   * @throws IOException en cas d'erreur de parsing
   */
  @Test
  public void shouldCallAllGetters() throws IOException {
    final String json =
        new JsonBuilder()
            .add("issuer", "https://server.com")
            .add("authorization_endpoint", "https://server.com/auth")
            .add("token_endpoint", "https://server.com/token")
            .add("jwks_uri", "https://server.com/jwks")
            .add("response_types_supported", Arrays.asList("code", "id_token"))
            .add("subject_types_supported", Arrays.asList("public"))
            .add("id_token_signing_alg_values_supported", Arrays.asList("RS256"))
            .add("userinfo_endpoint", "https://server.com/userinfo")
            .add("registration_endpoint", "https://server.com/reg")
            .add("scopes_supported", Arrays.asList("openid", "email"))
            .add("response_modes_supported", Arrays.asList("query", "fragment"))
            .add("grant_types_supported", Arrays.asList("authorization_code"))
            .add("revocation_endpoint", "https://server.com/revoke")
            .add("introspection_endpoint", "https://server.com/introspect")
            .add("pushed_authorization_request_endpoint", "https://server.com/par")
            .add("dpop_signing_alg_values_supported", Arrays.asList("ES256"))
            .build();

    final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);

    assertThat(metadata.getIssuer()).isEqualTo("https://server.com");
    assertThat(metadata.getAuthorizationEndpoint()).isEqualTo("https://server.com/auth");
    assertThat(metadata.getTokenEndpoint()).isEqualTo("https://server.com/token");
    assertThat(metadata.getJwksUri()).isEqualTo("https://server.com/jwks");
    assertThat(metadata.getResponseTypesSupported()).containsExactly("code", "id_token");
    assertThat(metadata.getSubjectTypesSupported()).containsExactly("public");
    assertThat(metadata.getIdTokenSigningAlgValuesSupported()).containsExactly("RS256");
    assertThat(metadata.getUserinfoEndpoint()).isEqualTo("https://server.com/userinfo");
    assertThat(metadata.getRegistrationEndpoint()).isEqualTo("https://server.com/reg");
    assertThat(metadata.getScopesSupported()).containsExactly("openid", "email");
    assertThat(metadata.getResponseModesSupported()).containsExactly("query", "fragment");
    assertThat(metadata.getGrantTypesSupported()).containsExactly("authorization_code");
    assertThat(metadata.getRevocationEndpoint()).isEqualTo("https://server.com/revoke");
    assertThat(metadata.getIntrospectionEndpoint()).isEqualTo("https://server.com/introspect");
    assertThat(metadata.getPushedAuthorizationRequestEndpoint())
        .isEqualTo("https://server.com/par");
    assertThat(metadata.getDpopSigningAlgValuesSupported()).containsExactly("ES256");
  }

  /**
   * Vérifie le comportement avec des listes absentes.
   *
   * @throws IOException en cas d'erreur de parsing
   */
  @Test
  public void shouldHandleMissingLists() throws IOException {
    final String json = new JsonBuilder().add("issuer", "https://server.com").build();
    final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);
    assertThat(metadata.getResponseTypesSupported()).isEmpty();
    assertThat(metadata.getScopesSupported()).isEmpty();
  }
}
