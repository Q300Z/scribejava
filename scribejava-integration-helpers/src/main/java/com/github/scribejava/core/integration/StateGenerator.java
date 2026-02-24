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
package com.github.scribejava.core.integration;

import java.security.SecureRandom;
import java.util.Base64;

/** Générateur de paramètres 'state' sécurisés pour la protection CSRF. */
public class StateGenerator {

  private static final int DEFAULT_ENTROPY_BYTES = 32;
  private final SecureRandom random = new SecureRandom();
  private final int entropyBytes;

  public StateGenerator() {
    this(DEFAULT_ENTROPY_BYTES);
  }

  public StateGenerator(int entropyBytes) {
    this.entropyBytes = entropyBytes;
  }

  /**
   * Génère une chaîne aléatoire sécurisée encodée en URL-safe Base64.
   *
   * @return le state.
   */
  public String generate() {
    byte[] bytes = new byte[entropyBytes];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
