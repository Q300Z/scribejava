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

import com.github.scribejava.core.model.OAuth2AccessToken;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Gère le rafraîchissement automatique et thread-safe des jetons.
 *
 * @param <K> Type de la clé d'identification.
 */
public class TokenAutoRenewer<K> {

  private final TokenRepository<K, ExpiringTokenWrapper> repository;
  private final Function<OAuth2AccessToken, OAuth2AccessToken> refreshFunction;
  private final int expirationBufferSeconds;

  private final ConcurrentHashMap<K, Lock> locks = new ConcurrentHashMap<>();

  /**
   * @param repository repository
   * @param refreshFunction refreshFunction
   */
  public TokenAutoRenewer(
      TokenRepository<K, ExpiringTokenWrapper> repository,
      Function<OAuth2AccessToken, OAuth2AccessToken> refreshFunction) {
    this(repository, refreshFunction, 60);
  }

  /**
   * @param repository repository
   * @param refreshFunction refreshFunction
   * @param expirationBufferSeconds expirationBufferSeconds
   */
  public TokenAutoRenewer(
      TokenRepository<K, ExpiringTokenWrapper> repository,
      Function<OAuth2AccessToken, OAuth2AccessToken> refreshFunction,
      int expirationBufferSeconds) {
    this.repository = Objects.requireNonNull(repository);
    this.refreshFunction = Objects.requireNonNull(refreshFunction);
    this.expirationBufferSeconds = expirationBufferSeconds;
  }

  /**
   * @param key key
   * @return OAuth2AccessToken
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   */
  public OAuth2AccessToken getValidToken(K key) throws InterruptedException, ExecutionException {
    ExpiringTokenWrapper wrapper =
        repository
            .findByKey(key)
            .orElseThrow(() -> new IllegalArgumentException("No token found for key: " + key));

    if (!wrapper.isExpiredWithBuffer(expirationBufferSeconds)) {
      return wrapper.getToken();
    }

    final Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
    lock.lock();
    try {
      // Re-fetch après acquisition du verrou
      wrapper =
          repository
              .findByKey(key)
              .orElseThrow(
                  () -> new IllegalStateException("Token vanished while waiting for lock"));

      if (!wrapper.isExpiredWithBuffer(expirationBufferSeconds)) {
        return wrapper.getToken();
      }

      final OAuth2AccessToken newToken = refreshFunction.apply(wrapper.getToken());
      repository.save(key, new ExpiringTokenWrapper(newToken));
      return newToken;
    } finally {
      lock.unlock();
    }
  }
}
