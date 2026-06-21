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

import com.github.scribejava.oidc.model.OidcKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Default in-memory implementation of {@link OidcKeyCache} using a {@link ConcurrentHashMap}. */
public class DefaultOidcKeyCache implements OidcKeyCache {
  private final Map<String, OidcKey> keys = new ConcurrentHashMap<>();

  /**
   * {@inheritDoc}
   *
   * @param kid the key identifier
   * @return the corresponding {@link OidcKey}, or {@code null} if not found in the cache
   */
  @Override
  public OidcKey get(String kid) {
    return keys.get(kid);
  }

  /**
   * {@inheritDoc}
   *
   * @param newKeys a map of Key ID to {@link OidcKey} to be cached
   */
  @Override
  public void putAll(Map<String, OidcKey> newKeys) {
    if (newKeys != null) {
      keys.putAll(newKeys);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    keys.clear();
  }
}
