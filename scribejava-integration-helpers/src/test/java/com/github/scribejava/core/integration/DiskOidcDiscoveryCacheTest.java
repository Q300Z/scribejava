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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.scribejava.oidc.OidcDiscoveryService;
import com.github.scribejava.oidc.OidcProviderMetadata;
import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests pour la persistance du cache OIDC sur disque. */
class DiskOidcDiscoveryCacheTest {

  @TempDir Path tempDir;

  @Test
  void shouldPersistMetadataToDisk() throws Exception {
    final File cacheFile = tempDir.resolve("oidc-cache.json").toFile();
    final DiskOidcDiscoveryCache diskCache = new DiskOidcDiscoveryCache(cacheFile);

    final OidcDiscoveryService mockService = mock(OidcDiscoveryService.class);
    final String json =
        "{\"issuer\":\"http://localhost\","
            + "\"authorization_endpoint\":\"http://auth\","
            + "\"token_endpoint\":\"http://token\"}";
    final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);

    when(mockService.getProviderMetadata()).thenReturn(metadata);

    // 1. Premier appel : doit appeler le service réseau (le mock)
    final OidcProviderMetadata result1 = diskCache.getMetadata("test-id", mockService);
    diskCache.flush(); // On force l'écriture sur disque
    assertThat(result1.getIssuer()).isEqualTo("http://localhost");
    verify(mockService, times(1)).getProviderMetadata();
    assertThat(cacheFile).exists();

    // 2. Second appel avec une nouvelle instance de cache : doit utiliser le fichier disque
    final DiskOidcDiscoveryCache diskCache2 = new DiskOidcDiscoveryCache(cacheFile);
    clearInvocations(mockService);

    final OidcProviderMetadata result2 = diskCache2.getMetadata("test-id", mockService);
    assertThat(result2.getIssuer()).isEqualTo("http://localhost");
    // Vérification : le mock ne doit PAS être appelé à nouveau
    verify(mockService, times(0)).getProviderMetadata();
  }

  @Test
  void shouldRefreshWhenCacheIsExpired() throws Exception {
    final File cacheFile = tempDir.resolve("oidc-expired-cache.json").toFile();

    // 1. Création d'un cache avec un TTL très court (100ms)
    final DiskOidcDiscoveryCache diskCache = new DiskOidcDiscoveryCache(cacheFile, 100);

    final OidcDiscoveryService mockService = mock(OidcDiscoveryService.class);
    final String json =
        "{\"issuer\":\"http://localhost\","
            + "\"authorization_endpoint\":\"http://auth\","
            + "\"token_endpoint\":\"http://token\"}";
    final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);
    when(mockService.getProviderMetadata()).thenReturn(metadata);

    // Initialisation : Peuple le cache
    diskCache.getMetadata("test-id", mockService);
    verify(mockService, times(1)).getProviderMetadata();

    // Attente de l'expiration du TTL
    Thread.sleep(200);

    // Appel après expiration : doit rafraîchir (appeler le mock à nouveau)
    clearInvocations(mockService);
    diskCache.getMetadata("test-id", mockService);
    verify(mockService, times(1)).getProviderMetadata();
  }
}
