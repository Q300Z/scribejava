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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiskOidcDiscoveryCacheTest {

  @TempDir
  Path tempDir;

  @Test
  void shouldPersistMetadataToDisk() throws Exception {
    final File cacheFile = tempDir.resolve("oidc-cache.json").toFile();
    final DiskOidcDiscoveryCache diskCache = new DiskOidcDiscoveryCache(cacheFile);

    final OidcDiscoveryService mockService = mock(OidcDiscoveryService.class);
    final String json = "{\"issuer\":\"http://localhost\","
        + "\"authorization_endpoint\":\"http://auth\","
        + "\"token_endpoint\":\"http://token\"}";
    final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);

    when(mockService.getProviderMetadata()).thenReturn(metadata);

    // 1. First call: should call service
    final OidcProviderMetadata result1 = diskCache.getMetadata("test-id", mockService);
    assertThat(result1.getIssuer()).isEqualTo("http://localhost");
    verify(mockService, times(1)).getProviderMetadata();
    assertThat(cacheFile).exists();

    // 2. Second call (new instance of cache): should use disk
    final DiskOidcDiscoveryCache diskCache2 = new DiskOidcDiscoveryCache(cacheFile);
    final OidcProviderMetadata result2 = diskCache2.getMetadata("test-id", mockService);
    assertThat(result2.getIssuer()).isEqualTo("http://localhost");
    verify(mockService, times(1)).getProviderMetadata(); // Still 1 call total
  }

  @Test
  void shouldRefreshWhenCacheIsExpired() throws Exception {
    final File cacheFile = tempDir.resolve("oidc-expired-cache.json").toFile();

    // 1. Create a cache with a very short TTL (100ms)
    final DiskOidcDiscoveryCache diskCache = new DiskOidcDiscoveryCache(cacheFile, 100);

    final OidcDiscoveryService mockService = mock(OidcDiscoveryService.class);
    final String json = "{\"issuer\":\"http://localhost\","
        + "\"authorization_endpoint\":\"http://auth\","
        + "\"token_endpoint\":\"http://token\"}";
    final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);
    when(mockService.getProviderMetadata()).thenReturn(metadata);

    // First call: Populate cache
    diskCache.getMetadata("test-id", mockService);
    verify(mockService, times(1)).getProviderMetadata();

    // Wait for expiration
    Thread.sleep(200);

    // Second call: Should refresh
    diskCache.getMetadata("test-id", mockService);
    verify(mockService, times(2)).getProviderMetadata();
  }
}
