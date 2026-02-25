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
package com.github.scribejava.oidc.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import com.github.scribejava.core.exceptions.OAuthException;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.junit.jupiter.api.Test;

/** Tests des échecs cryptographiques (Catch blocks). */
public class CryptoFailureTest {

  @Test
  public void shouldFailOnInvalidPrivateKey() {
    final PrivateKey invalidKey = mock(PrivateKey.class);
    final JwtSigner signer = new JwtSigner.RsaSha256Signer();

    assertThatExceptionOfType(OAuthException.class)
        .isThrownBy(() -> signer.sign("payload", invalidKey))
        .withMessageContaining("RS256 signing failed");
  }

  @Test
  public void shouldHandleVerificationErrorGracefully() {
    final PublicKey invalidKey = mock(PublicKey.class);
    final JwtSignatureVerifier verifier = new JwtSignatureVerifier();

    // Doit retourner false au lieu de lever une exception (comportement SOLID/Fail-safe)
    final boolean result = verifier.verifyRS256("content".getBytes(), new byte[0], invalidKey);
    assertThat(result).isFalse();
  }
}
