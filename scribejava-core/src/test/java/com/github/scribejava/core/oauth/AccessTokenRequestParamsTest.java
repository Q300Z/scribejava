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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test des paramètres de requête de jeton d'accès. */
class AccessTokenRequestParamsTest {

  /** Vérifie le maintien des paramètres dans l'objet. */
  @Test
  void shouldMaintainParameters() {
    String code = "auth_code";
    String verifier = "pkce_verifier";
    String scope = "user_info";
    Map<String, String> extra = Collections.singletonMap("param", "value");

    AccessTokenRequestParams params =
        new AccessTokenRequestParams(code)
            .pkceCodeVerifier(verifier)
            .scope(scope)
            .extraParameters(extra);

    assertThat(params.getCode()).isEqualTo(code);
    assertThat(params.getPkceCodeVerifier()).isEqualTo(verifier);
    assertThat(params.getScope()).isEqualTo(scope);
    assertThat(params.getExtraParameters()).isEqualTo(extra);
  }
}
