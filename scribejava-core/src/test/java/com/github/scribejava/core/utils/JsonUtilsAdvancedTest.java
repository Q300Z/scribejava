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
package com.github.scribejava.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.scribejava.core.exceptions.OAuthException;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests avancés pour JsonUtils (Sécurité & Unicode). */
public class JsonUtilsAdvancedTest {

  /** Vérifie le support des séquences Unicode. */
  @Test
  public void shouldHandleUnicodeEscape() {
    final String json = "{\"name\":\"J\\u00e9r\\u00f4me\"}";
    final Map<String, Object> map = JsonUtils.parse(json);

    assertThat(map.get("name")).isEqualTo("Jérôme");
  }

  /** Vérifie la limitation de la profondeur de récursion. */
  @Test
  public void shouldFailOnDeepRecursion() {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 50; i++) {
      sb.append("{\"a\":");
    }
    sb.append("\"val\"");
    for (int i = 0; i < 50; i++) {
      sb.append('}');
    }

    assertThatExceptionOfType(OAuthException.class)
        .isThrownBy(() -> JsonUtils.parse(sb.toString()))
        .withMessageContaining("JSON nesting limit exceeded");
  }
}
