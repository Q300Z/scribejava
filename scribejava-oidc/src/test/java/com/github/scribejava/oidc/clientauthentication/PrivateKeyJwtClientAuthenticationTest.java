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
package com.github.scribejava.oidc.clientauthentication;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.oidc.model.Jwt;
import com.github.scribejava.oidc.model.JwtSigner;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests pour {@link PrivateKeyJwtClientAuthentication}. */
public class PrivateKeyJwtClientAuthenticationTest {

  /**
   * Vérifie l'ajout de l'assertion client à la requête.
   *
   * @throws Exception erreur
   */
  @Test
  public void shouldAddClientAssertionToRequest() throws Exception {
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    final KeyPair kp = kpg.generateKeyPair();

    final String clientId = "my-client-id";
    final String audience = "https://server.example.com/token";

    final PrivateKeyJwtClientAuthentication auth =
        new PrivateKeyJwtClientAuthentication(
            clientId, audience, kp.getPrivate(), "123", new JwtSigner.RsaSha256Signer());

    final OAuthRequest request = new OAuthRequest(Verb.POST, audience);
    auth.addClientAuthentication(request);

    assertThat(getParam(request, "client_assertion_type"))
        .isEqualTo("urn:ietf:params:oauth:client-assertion-type:jwt-bearer");

    final String assertion = getParam(request, "client_assertion");
    assertThat(assertion).isNotNull();

    final Jwt jwt = Jwt.parse(assertion);
    assertThat(jwt.getPayload().get("sub")).isEqualTo(clientId);
    assertThat(jwt.getPayload().get("iss")).isEqualTo(clientId);

    final Object aud = jwt.getPayload().get("aud");
    if (aud instanceof String) {
      assertThat(aud).isEqualTo(audience);
    } else if (aud instanceof List) {
      assertThat((List<String>) aud).contains(audience);
    }
  }

  private String getParam(final OAuthRequest request, final String name) {
    return request.getBodyParams().getParams().stream()
        .filter(p -> p.getKey().equals(name))
        .map(com.github.scribejava.core.model.Parameter::getValue)
        .findFirst()
        .orElse(null);
  }
}
