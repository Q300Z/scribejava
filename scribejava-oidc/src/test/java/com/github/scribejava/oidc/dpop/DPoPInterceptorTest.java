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
package com.github.scribejava.oidc.dpop;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.oidc.model.Jwt;
import org.junit.jupiter.api.Test;

/** Tests pour {@link DefaultDPoPProofCreator}. */
public class DPoPInterceptorTest {

  /**
   * Vérifie la création d'une preuve DPoP valide.
   *
   * @throws Exception erreur
   */
  @Test
  public void shouldCreateValidDPoPProof() throws Exception {
    final DefaultDPoPProofCreator proofCreator = new DefaultDPoPProofCreator();
    final com.github.scribejava.core.dpop.DPoPInterceptor interceptor =
        new com.github.scribejava.core.dpop.DPoPInterceptor(proofCreator);

    final OAuthRequest request =
        new OAuthRequest(Verb.POST, "https://resource.example.com/api/user");
    interceptor.intercept(request);

    final String dpopHeader = request.getHeaders().get("DPoP");
    assertThat(dpopHeader).isNotNull();

    final Jwt jwt = Jwt.parse(dpopHeader);
    assertThat(jwt.getPayload().get("htm")).isEqualTo("POST");
    assertThat(jwt.getPayload().get("htu")).isEqualTo("https://resource.example.com/api/user");
    assertThat(jwt.getPayload().get("jti")).isNotNull();
    assertThat(jwt.getHeader().get("jwk")).isNotNull();
  }
}
