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

import org.junit.jupiter.api.Test;

/** Tests de robustesse pour Preconditions. */
public class PreconditionsRobustnessTest {

  /** Vérifie l'échec sur objet nul. */
  @Test
  public void shouldFailOnNullObject() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Preconditions.checkNotNull(null, "error"))
        .withMessage("error");
  }

  /** Vérifie l'échec sur chaîne vide ou nulle. */
  @Test
  public void shouldFailOnEmptyString() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Preconditions.checkEmptyString(null, "null msg"));

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Preconditions.checkEmptyString("", "empty msg"));

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Preconditions.checkEmptyString("   ", "spaces msg"));
  }

  /** Vérifie le succès sur entrées valides. */
  @Test
  public void shouldPassOnValidInput() {
    Preconditions.checkNotNull(new Object(), "ok");
    Preconditions.checkEmptyString("text", "ok");
  }

  /** Vérifie la méthode hasText. */
  @Test
  public void shouldHandleHasText() {
    assertThat(Preconditions.hasText(null)).isFalse();
    assertThat(Preconditions.hasText("")).isFalse();
    assertThat(Preconditions.hasText("  ")).isFalse();
    assertThat(Preconditions.hasText(" a ")).isTrue();
  }
}
