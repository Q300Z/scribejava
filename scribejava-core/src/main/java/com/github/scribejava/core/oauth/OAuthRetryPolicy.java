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

import com.github.scribejava.core.model.Response;

/** Configuration de la politique de Retry. */
public class OAuthRetryPolicy {

  private final int maxAttempts;
  private final long delayMs;

  /**
   * @param maxAttempts Nombre maximum d'essais (incluant le premier).
   * @param delayMs Délai entre chaque essai (ms).
   */
  public OAuthRetryPolicy(int maxAttempts, long delayMs) {
    this.maxAttempts = maxAttempts;
    this.delayMs = delayMs;
  }

  public int getMaxAttempts() {
    return maxAttempts;
  }

  public long getDelayMs() {
    return delayMs;
  }

  /**
   * Détermine si une réponse doit déclencher un Retry.
   *
   * @param response La réponse HTTP.
   * @return true si on doit rejouer.
   */
  public boolean shouldRetry(Response response) {
    final int code = response.getCode();
    // Codes transitoires : 429 (Rate Limit) ou 5xx (Server Error)
    return code == 429 || code >= 500 && code <= 599;
  }
}
