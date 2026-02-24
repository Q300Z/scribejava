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

import com.github.scribejava.oidc.OidcDiscoveryService;
import com.github.scribejava.oidc.OidcProviderMetadata;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/** Cache thread-safe pour les métadonnées de découverte OIDC. */
public class OidcDiscoveryCache {

  private final Map<String, OidcProviderMetadata> cache = new ConcurrentHashMap<>();

  /**
   * Récupère les métadonnées depuis le cache ou utilise le service fourni pour les découvrir.
   *
   * @param providerId Identifiant du fournisseur (ex: "google").
   * @param discoveryService Service configuré pour ce fournisseur.
   * @return Les métadonnées.
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   */
  public OidcProviderMetadata getMetadata(String providerId, OidcDiscoveryService discoveryService)
      throws IOException, InterruptedException, ExecutionException {
    if (!cache.containsKey(providerId)) {
      cache.put(providerId, discoveryService.getProviderMetadata());
    }
    return cache.get(providerId);
  }

  public void clear() {
    cache.clear();
  }
}
