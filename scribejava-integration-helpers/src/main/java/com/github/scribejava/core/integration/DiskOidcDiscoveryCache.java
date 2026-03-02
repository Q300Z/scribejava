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

import com.github.scribejava.core.model.JsonBuilder;
import com.github.scribejava.core.utils.JsonUtils;
import com.github.scribejava.oidc.OidcDiscoveryService;
import com.github.scribejava.oidc.OidcProviderMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Implémentation du cache de découverte OIDC avec persistance sur disque.
 */
public class DiskOidcDiscoveryCache extends OidcDiscoveryCache {

  private static final Logger LOG = LoggerFactory.getLogger(DiskOidcDiscoveryCache.class);
  private static final long DEFAULT_TTL_MS = 24 * 60 * 60 * 1000L; // 24 heures

  private final File cacheFile;
  private final long ttlMs;
  /** Structure : Issuer -> { "data": JSON_BRUT, "ts": TIMESTAMP_MS } */
  private final Map<String, Map<String, Object>> persistentCache = new HashMap<>();

  /**
   * @param cacheFile Fichier de cache.
   */
  public DiskOidcDiscoveryCache(File cacheFile) {
    this(cacheFile, DEFAULT_TTL_MS);
  }

  /**
   * @param cacheFile Fichier de cache.
   * @param ttlMs Durée de vie du cache en millisecondes.
   */
  public DiskOidcDiscoveryCache(File cacheFile, long ttlMs) {
    this.cacheFile = cacheFile;
    this.ttlMs = ttlMs;
    loadFromDisk();
  }

  @Override
  public OidcProviderMetadata getMetadata(String providerId, OidcDiscoveryService discoveryService)
      throws IOException, InterruptedException, ExecutionException {

    final Map<String, Object> entry = persistentCache.get(providerId);

    if (entry != null) {
      final long timestamp = ((Number) entry.get("ts")).longValue();
      if (System.currentTimeMillis() - timestamp < ttlMs) {
        LOG.info("OIDC Discovery: Cache hit on disk for {} (Valid)", providerId);
        return OidcProviderMetadata.parse((String) entry.get("data"));
      }
      LOG.info("OIDC Discovery: Cache expired for {}. Refreshing...", providerId);
    }

    // 3. Fetch from network
    final OidcProviderMetadata metadata = super.getMetadata(providerId, discoveryService);

    // 4. Save to disk with current timestamp
    final JsonBuilder builder = new JsonBuilder()
        .add("data", metadata.getRawResponse())
        .add("ts", System.currentTimeMillis());

    persistentCache.put(providerId, builder.asMap());
    saveToDisk();

    return metadata;
  }

  private void loadFromDisk() {
    if (!cacheFile.exists()) {
      return;
    }
    try {
      final byte[] bytes = Files.readAllBytes(cacheFile.toPath());
      final String content = new String(bytes, StandardCharsets.UTF_8);
      final Map<String, Object> data = JsonUtils.parse(content);
      for (Map.Entry<String, Object> entry : data.entrySet()) {
        if (entry.getValue() instanceof Map) {
          persistentCache.put(entry.getKey(), (Map<String, Object>) entry.getValue());
        }
      }
      LOG.info("OIDC Discovery: Loaded {} entries from disk cache.", persistentCache.size());
    } catch (Exception e) {
      LOG.warn("Failed to load OIDC discovery cache from disk: {}", e.getMessage());
    }
  }

  private void saveToDisk() {
    try {
      final JsonBuilder builder = new JsonBuilder();
      for (Map.Entry<String, Map<String, Object>> entry : persistentCache.entrySet()) {
        builder.add(entry.getKey(), entry.getValue());
      }
      Files.write(cacheFile.toPath(), builder.build().getBytes(StandardCharsets.UTF_8));
      LOG.debug("OIDC Discovery: Saved cache to disk.");
    } catch (Exception e) {
      LOG.warn("Failed to save OIDC discovery cache to disk: {}", e.getMessage());
    }
  }
}
