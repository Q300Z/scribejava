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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Implémentation du cache de découverte OIDC avec persistance sur disque.
 *
 * <p>Ce cache permet de survivre aux redémarrages applicatifs et réduit la charge réseau vers le
 * fournisseur d'identité.
 */
public class DiskOidcDiscoveryCache extends OidcDiscoveryCache {

  /** Durée de vie par défaut du cache (24 heures). */
  private static final long DEFAULT_TTL_MS = 24 * 60 * 60 * 1000L;

  private final File cacheFile;
  private final long ttlMs;

  /**
   * Structure interne à plat pour compatibilité avec le parser JSON natif.
   *
   * <p>Clés : {providerId}.data et {providerId}.ts
   */
  private final Map<String, Object> persistentCache = new HashMap<>();

  /**
   * Construit un cache sur disque avec un TTL par défaut de 24h.
   *
   * @param cacheFile Le fichier à utiliser pour le stockage.
   */
  public DiskOidcDiscoveryCache(File cacheFile) {
    this(cacheFile, DEFAULT_TTL_MS);
  }

  /**
   * Construit un cache sur disque avec un TTL personnalisé.
   *
   * @param cacheFile Le fichier à utiliser pour le stockage.
   * @param ttlMs Durée de vie des métadonnées en millisecondes.
   */
  public DiskOidcDiscoveryCache(File cacheFile, long ttlMs) {
    this.cacheFile = cacheFile;
    this.ttlMs = ttlMs;
    loadFromDisk();
  }

  /**
   * Récupère les métadonnées depuis le disque, la mémoire ou le réseau.
   *
   * @param providerId L'identifiant du fournisseur (ex: "google").
   * @param discoveryService Le service permettant de faire l'appel réseau si nécessaire.
   * @return Les métadonnées du fournisseur OIDC.
   * @throws IOException Erreur lors de la lecture/écriture disque ou réseau.
   * @throws InterruptedException Si l'opération est interrompue.
   * @throws ExecutionException En cas d'erreur lors du rafraîchissement.
   */
  @Override
  public OidcProviderMetadata getMetadata(String providerId, OidcDiscoveryService discoveryService)
      throws IOException, InterruptedException, ExecutionException {

    final String dataKey = providerId + ".data";
    final String tsKey = providerId + ".ts";

    final String cachedData = (String) persistentCache.get(dataKey);
    final Object tsObj = persistentCache.get(tsKey);

    if (cachedData != null && tsObj instanceof Number) {
      final long timestamp = ((Number) tsObj).longValue();
      if (System.currentTimeMillis() - timestamp < ttlMs) {
        return OidcProviderMetadata.parse(cachedData);
      }
    }

    // 3. Fetch from network
    final OidcProviderMetadata metadata = discoveryService.getProviderMetadata();

    // 4. Save to disk (Structure à plat)
    persistentCache.put(dataKey, metadata.getRawResponse());
    persistentCache.put(tsKey, System.currentTimeMillis());
    saveToDisk();

    return metadata;
  }

  /**
   * Persiste l'état actuel du cache mémoire vers le fichier disque.
   *
   * <p>Cette méthode peut être appelée manuellement pour garantir que les données sont bien écrites
   * sur le support physique avant un arrêt ou entre des tests.
   */
  public void flush() {
    saveToDisk();
  }

  /** Charge le cache depuis le fichier disque lors de l'initialisation. */
  private void loadFromDisk() {
    if (!cacheFile.exists()) {
      return;
    }
    try {
      final byte[] bytes = Files.readAllBytes(cacheFile.toPath());
      final String content = new String(bytes, StandardCharsets.UTF_8);
      persistentCache.putAll(JsonUtils.parse(content));
    } catch (Exception e) {
      // Échec silencieux
    }
  }

  /** Persiste l'état actuel du cache mémoire vers le fichier disque. */
  private void saveToDisk() {
    try {
      final JsonBuilder builder = new JsonBuilder();
      for (Map.Entry<String, Object> entry : persistentCache.entrySet()) {
        builder.add(entry.getKey(), entry.getValue());
      }
      Files.write(cacheFile.toPath(), builder.build().getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      // Échec silencieux
    }
  }
}
