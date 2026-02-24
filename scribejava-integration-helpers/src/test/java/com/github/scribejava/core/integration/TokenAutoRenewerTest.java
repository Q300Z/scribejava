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

import com.github.scribejava.core.model.OAuth2AccessToken;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenAutoRenewerTest {

  private InMemoryTokenRepository repository;
  private AtomicInteger refreshCallCount;

  @BeforeEach
  void setUp() {
    repository = new InMemoryTokenRepository();
    refreshCallCount = new AtomicInteger(0);
  }

  @Test
  void shouldReturnExistingTokenIfNotExpired() {
    // Given
    String key = "user1";
    OAuth2AccessToken token = new OAuth2AccessToken("access", null, 3600, null, null, null);
    repository.save(key, new ExpiringTokenWrapper(token));

    TokenAutoRenewer<String> renewer =
        new TokenAutoRenewer<>(
            repository,
            t -> {
              refreshCallCount.incrementAndGet();
              return token;
            });

    // When
    OAuth2AccessToken result = renewer.getValidToken(key);

    // Then
    assertThat(result).isEqualTo(token);
    assertThat(refreshCallCount.get()).isZero();
  }

  @Test
  void shouldRefreshTokenIfExpired() {
    // Given
    String key = "user1";
    OAuth2AccessToken oldToken = new OAuth2AccessToken("old", null, -10, "refresh", null, null);
    OAuth2AccessToken newToken = new OAuth2AccessToken("new", null, 3600, "refresh", null, null);
    repository.save(key, new ExpiringTokenWrapper(oldToken));

    TokenAutoRenewer<String> renewer =
        new TokenAutoRenewer<>(
            repository,
            t -> {
              refreshCallCount.incrementAndGet();
              return newToken;
            });

    // When
    OAuth2AccessToken result = renewer.getValidToken(key);

    // Then
    assertThat(result.getAccessToken()).isEqualTo("new");
    assertThat(refreshCallCount.get()).isEqualTo(1);
    assertThat(repository.findByKey(key).get().getToken().getAccessToken()).isEqualTo("new");
  }

  private static class InMemoryTokenRepository
      implements TokenRepository<String, ExpiringTokenWrapper> {
    private final java.util.Map<String, ExpiringTokenWrapper> storage = new java.util.HashMap<>();

    @Override
    public Optional<ExpiringTokenWrapper> findByKey(String key) {
      return Optional.ofNullable(storage.get(key));
    }

    @Override
    public void save(String key, ExpiringTokenWrapper token) {
      storage.put(key, token);
    }

    @Override
    public void deleteByKey(String key) {
      storage.remove(key);
    }
  }
}
