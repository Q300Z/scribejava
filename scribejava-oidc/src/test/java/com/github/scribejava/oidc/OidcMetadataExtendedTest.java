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

/** Tests étendus pour les métadonnées OIDC. */
public class OidcMetadataExtendedTest {

  /**
   * Vérifie le parsing d'un objet de métadonnées complet via JsonBuilder.
   *
   * @throws IOException en cas d'erreur
   */
  @Test
  public void shouldParseCompleteMetadata() throws IOException {
    final String json =
        new JsonBuilder()
            .add("issuer", "https://idp.com")
            .add("authorization_endpoint", "https://idp.com/auth")
            .add("token_endpoint", "https://idp.com/token")
            .add("jwks_uri", "https://idp.com/keys")
            .add("response_types_supported", Arrays.asList("code", "id_token"))
            .add("subject_types_supported", Arrays.asList("public"))
            .add("id_token_signing_alg_values_supported", Arrays.asList("RS256"))
            .add("userinfo_endpoint", "https://idp.com/userinfo")
            .add("registration_endpoint", "https://idp.com/register")
            .add("scopes_supported", Arrays.asList("openid", "profile"))
            .add("response_modes_supported", Arrays.asList("query", "form_post"))
            .add("grant_types_supported", Arrays.asList("authorization_code", "refresh_token"))
            .add("revocation_endpoint", "https://idp.com/revoke")
            .add("introspection_endpoint", "https://idp.com/introspect")
            .add("pushed_authorization_request_endpoint", "https://idp.com/par")
            .add("dpop_signing_alg_values_supported", Arrays.asList("RS256", "ES256"))
            .build();

    final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);
    assertThat(metadata.getIssuer()).isEqualTo("https://idp.com");
    assertThat(metadata.getAuthorizationEndpoint()).isEqualTo("https://idp.com/auth");
    assertThat(metadata.getUserinfoEndpoint()).isEqualTo("https://idp.com/userinfo");
    assertThat(metadata.getRegistrationEndpoint()).isEqualTo("https://idp.com/register");
    assertThat(metadata.getRevocationEndpoint()).isEqualTo("https://idp.com/revoke");
    assertThat(metadata.getIntrospectionEndpoint()).isEqualTo("https://idp.com/introspect");
    assertThat(metadata.getPushedAuthorizationRequestEndpoint()).isEqualTo("https://idp.com/par");
    assertThat(metadata.getDpopSigningAlgValuesSupported()).contains("ES256");
    assertThat(metadata.getScopesSupported()).contains("profile");
  }

  /**
   * Vérifie le comportement avec un objet JSON vide.
   *
   * @throws IOException en cas d'erreur
   */
  @Test
  public void shouldHandleEmptyMetadata() throws IOException {
    final String json = new JsonBuilder().build();
    final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);
    assertThat(metadata.getIssuer()).isNull();
    assertThat(metadata.getScopesSupported()).isEmpty();
  }
}
