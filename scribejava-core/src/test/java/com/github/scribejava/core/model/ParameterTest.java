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
package com.github.scribejava.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Tests pour la classe Parameter. */
public class ParameterTest {

  /** Vérifie le fonctionnement des accesseurs et de l'encodage URL. */
  @Test
  public void testGettersAndUrlEncodedPair() {
    final Parameter param = new Parameter("foo bar", "baz/qux");
    assertThat(param.getKey()).isEqualTo("foo bar");
    assertThat(param.getValue()).isEqualTo("baz/qux");
    assertThat(param.asUrlEncodedPair()).isEqualTo("foo%20bar=baz%2Fqux");
  }

  /** Vérifie le fonctionnement de equals et hashCode. */
  @Test
  public void testEqualsAndHashCode() {
    final Parameter param1 = new Parameter("key", "value");
    final Parameter param2 = new Parameter("key", "value");
    final Parameter paramDifferentKey = new Parameter("other", "value");
    final Parameter paramDifferentValue = new Parameter("key", "other");

    assertThat(param1).isEqualTo(param1);
    assertThat(param1).isEqualTo(param2);
    assertThat(param1.hashCode()).isEqualTo(param2.hashCode());

    assertThat(param1).isNotEqualTo(null);
    assertThat(param1).isNotEqualTo("not a parameter");
    assertThat(param1).isNotEqualTo(paramDifferentKey);
    assertThat(param1).isNotEqualTo(paramDifferentValue);
  }

  /** Vérifie le fonctionnement de compareTo. */
  @Test
  public void testCompareTo() {
    final Parameter param1 = new Parameter("key", "value");
    final Parameter param2 = new Parameter("key", "value");
    final Parameter paramLessKey = new Parameter("a_key", "value");
    final Parameter paramMoreKey = new Parameter("z_key", "value");
    final Parameter paramLessVal = new Parameter("key", "a_value");
    final Parameter paramMoreVal = new Parameter("key", "z_value");

    assertThat(param1.compareTo(param2)).isEqualTo(0);
    assertThat(param1.compareTo(paramLessKey)).isGreaterThan(0);
    assertThat(param1.compareTo(paramMoreKey)).isLessThan(0);
    assertThat(param1.compareTo(paramLessVal)).isGreaterThan(0);
    assertThat(param1.compareTo(paramMoreVal)).isLessThan(0);
  }
}
