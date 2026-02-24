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
import static org.mockito.Mockito.*;

import com.github.scribejava.oidc.OidcDiscoveryService;
import com.github.scribejava.oidc.OidcProviderMetadata;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

class OidcDiscoveryCacheTest {

  @Test
  void shouldCacheMetadata() throws IOException, InterruptedException, ExecutionException {
    // Given
    OidcDiscoveryService mockService = mock(OidcDiscoveryService.class);
    OidcProviderMetadata mockMetadata = mock(OidcProviderMetadata.class);
    when(mockService.getProviderMetadata()).thenReturn(mockMetadata);

    OidcDiscoveryCache cache = new OidcDiscoveryCache();

    // When
    OidcProviderMetadata first = cache.getMetadata("google", mockService);
    OidcProviderMetadata second = cache.getMetadata("google", mockService);

    // Then
    assertThat(first).isEqualTo(mockMetadata);
    assertThat(second).isEqualTo(mockMetadata);
    verify(mockService, times(1)).getProviderMetadata();
  }
}
