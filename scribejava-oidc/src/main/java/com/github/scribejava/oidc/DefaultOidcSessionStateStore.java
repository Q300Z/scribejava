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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default in-memory implementation of {@link OidcSessionStateStore} using a {@link
 * ConcurrentHashMap}.
 */
public class DefaultOidcSessionStateStore implements OidcSessionStateStore {
  private final Map<String, OidcSessionState> store = new ConcurrentHashMap<>();

  /**
   * {@inheritDoc}
   *
   * @param sessionState the {@link OidcSessionState} to save
   */
  @Override
  public void save(OidcSessionState sessionState) {
    if (sessionState != null && sessionState.getState() != null) {
      store.put(sessionState.getState(), sessionState);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @param state the state value used as lookup key
   * @return the associated {@link OidcSessionState}, or {@code null} if not found
   */
  @Override
  public OidcSessionState load(String state) {
    if (state == null) {
      return null;
    }
    return store.get(state);
  }

  /**
   * {@inheritDoc}
   *
   * @param state the state value used as lookup key
   */
  @Override
  public void remove(String state) {
    if (state != null) {
      store.remove(state);
    }
  }
}
