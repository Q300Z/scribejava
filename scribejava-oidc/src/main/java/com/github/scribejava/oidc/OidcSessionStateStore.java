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

/** Interface for storing, loading, and removing {@link OidcSessionState}. */
public interface OidcSessionStateStore {
  /**
   * Saves the given OIDC session state.
   *
   * @param sessionState the {@link OidcSessionState} to save
   */
  void save(OidcSessionState sessionState);

  /**
   * Loads the OIDC session state associated with the given state value.
   *
   * @param state the state value used as lookup key
   * @return the associated {@link OidcSessionState}, or {@code null} if not found
   */
  OidcSessionState load(String state);

  /**
   * Removes the OIDC session state associated with the given state value.
   *
   * @param state the state value used as lookup key
   */
  void remove(String state);
}
