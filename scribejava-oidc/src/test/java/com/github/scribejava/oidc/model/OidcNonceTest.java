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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OidcNonceTest {

  @Test
  void shouldCreateValidNonce() {
    OidcNonce nonce = new OidcNonce("a-very-long-and-secure-nonce-value");
    assertThat(nonce.getValue()).isEqualTo("a-very-long-and-secure-nonce-value");
  }

  @Test
  void shouldRejectNullOrEmpty() {
    assertThatThrownBy(() -> new OidcNonce(null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new OidcNonce("")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new OidcNonce("   ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldRejectTooShortNonce() {
    // Un nonce de moins de 16 caractères est considéré comme peu sûr
    assertThatThrownBy(() -> new OidcNonce("too-short"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at least 16 characters long");
  }

  @Test
  void shouldGenerateUniqueRandomNonces() {
    OidcNonce nonce1 = OidcNonce.generate();
    OidcNonce nonce2 = OidcNonce.generate();

    assertThat(nonce1.getValue()).hasSizeGreaterThanOrEqualTo(32);
    assertThat(nonce1).isNotEqualTo(nonce2);
  }

  @Test
  void shouldRespectEquality() {
    OidcNonce nonce1 = new OidcNonce("constant-value-for-test");
    OidcNonce nonce2 = new OidcNonce("constant-value-for-test");
    OidcNonce nonce3 = new OidcNonce("different-value-for-test");

    assertThat(nonce1).isEqualTo(nonce2);
    assertThat(nonce1.hashCode()).isEqualTo(nonce2.hashCode());
    assertThat(nonce1).isNotEqualTo(nonce3);
  }
}
