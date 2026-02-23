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
package com.github.scribejava.core.oauth;

import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCECodeChallengeMethod;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests pour l'intercepteur PKCE (Phase 2 du refactoring SOLID).
 */
public class PKCEInterceptorTest {

  /**
   * Vérifie que l'intercepteur ajoute les paramètres PKCE à la map des paramètres d'autorisation.
   */
  @Test
  public void shouldAddPkceParameters() {
    // Arrange
    final PKCE pkce = new PKCE();
    pkce.setCodeChallenge("challenge123");
    pkce.setCodeChallengeMethod(PKCECodeChallengeMethod.S256);

    // Simulateur d'intercepteur (ce qu'on veut implémenter)
    final AuthorizationRequestInterceptor interceptor = new PKCEInterceptor(pkce);
    final Map<String, String> params = new HashMap<>();

    // Act
    interceptor.intercept(params);

    // Assert
    assertThat(params).containsEntry("code_challenge", "challenge123");
    assertThat(params).containsEntry("code_challenge_method", "S256");
  }
}
