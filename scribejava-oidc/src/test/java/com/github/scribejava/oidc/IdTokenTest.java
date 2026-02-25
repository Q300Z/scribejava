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

import com.github.scribejava.oidc.model.JwtBuilder;
import com.github.scribejava.oidc.model.JwtSigner;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import org.junit.jupiter.api.Test;

/** Tests pour {@link IdToken} natif. */
public class IdTokenTest {

  /**
   * Vérifie l'extraction des claims depuis un token généré.
   *
   * @throws Exception erreur
   */
  @Test
  public void shouldExtractClaims() throws Exception {
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    final KeyPair kp = kpg.generateKeyPair();

    final String rawToken =
        new JwtBuilder()
            .claim("sub", "123")
            .claim("iss", "https://idp.com")
            .claim("nonce", "xyz")
            .buildAndSign(new JwtSigner.RsaSha256Signer(), kp.getPrivate());

    final IdToken idToken = new IdToken(rawToken);

    assertThat(idToken.getClaim("sub")).isEqualTo("123");
    assertThat(idToken.getClaim("iss")).isEqualTo("https://idp.com");
    assertThat(idToken.getClaim("nonce")).isEqualTo("xyz");
  }
}
