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

/** Tests de parsing des métadonnées OIDC. */
public class OidcProviderMetadataTest {

  /**
   * Vérifie le parsing à partir d'un JSON construit via JsonBuilder.
   *
   * @throws IOException en cas d'erreur
   */
  @Test
  public void shouldParseMetadataFromJson() throws IOException {
    final String json =
        new JsonBuilder()
            .add("issuer", "https://server.example.com")
            .add("authorization_endpoint", "https://server.example.com/authorize")
            .add("token_endpoint", "https://server.example.com/token")
            .add("jwks_uri", "https://server.example.com/jwks.json")
            .add("response_types_supported", Arrays.asList("code", "id_token"))
            .add("subject_types_supported", Arrays.asList("public"))
            .add("id_token_signing_alg_values_supported", Arrays.asList("RS256"))
            .build();

    final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);

    assertThat(metadata).isNotNull();
    assertThat(metadata.getIssuer()).isEqualTo("https://server.example.com");
    assertThat(metadata.getAuthorizationEndpoint())
        .isEqualTo("https://server.example.com/authorize");
    assertThat(metadata.getTokenEndpoint()).isEqualTo("https://server.example.com/token");
    assertThat(metadata.getJwksUri()).isEqualTo("https://server.example.com/jwks.json");
    assertThat(metadata.getResponseTypesSupported()).contains("code");
    assertThat(metadata.getSubjectTypesSupported()).contains("public");
    assertThat(metadata.getIdTokenSigningAlgValuesSupported()).contains("RS256");
  }

  @Test
  public void shouldParseMetadataWithParEndpoint() throws IOException {
    final String parEndpoint = "https://server.example.com/par";
    final String json =
        new JsonBuilder()
            .add("issuer", "https://server.example.com")
            .add("authorization_endpoint", "https://server.example.com/authorize")
            .add("token_endpoint", "https://server.example.com/token")
            .add("jwks_uri", "https://server.example.com/jwks.json")
            .add("response_types_supported", Arrays.asList("code"))
            .add("subject_types_supported", Arrays.asList("public"))
            .add("id_token_signing_alg_values_supported", Arrays.asList("RS256"))
            .add("pushed_authorization_request_endpoint", parEndpoint)
            .build();

    final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);

    assertThat(metadata.getPushedAuthorizationRequestEndpoint()).isEqualTo(parEndpoint);
  }
}
