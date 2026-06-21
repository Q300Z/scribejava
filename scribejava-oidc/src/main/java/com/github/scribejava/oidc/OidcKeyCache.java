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

/** Interface for OIDC JWKS (JSON Web Key Set) key caching. */
public interface OidcKeyCache {
  /**
   * Retrieves an OIDC key from the cache by its Key ID (kid).
   *
   * @param kid the key identifier
   * @return the corresponding {@link OidcKey}, or {@code null} if not found in the cache
   */
  OidcKey get(String kid);

  /**
   * Caches all the given OIDC keys.
   *
   * @param keys a map of Key ID to {@link OidcKey} to be cached
   */
  void putAll(Map<String, OidcKey> keys);

  /** Clears all cached keys. */
  void clear();
}
