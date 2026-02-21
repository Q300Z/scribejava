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
package com.github.scribejava.core.builder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * OAuth2.0 Scope Builder. It allows specifying multiple scopes one by one. It will combine them in
 * the single space-delimited string. OAuth 2.0 standard specifies space as a delimiter for scopes
 * (https://tools.ietf.org/html/rfc6749#section-3.3). If you found API, that do not support spaces,
 * but support something else, let ScribeJava know (submit the issue here
 * https://github.com/scribejava/scribejava/issues) and use your own concatenated string as a
 * temporary workaround.
 */
public class ScopeBuilder {

  private final Set<String> scopes = new HashSet<>();

  public ScopeBuilder() {}

  public ScopeBuilder(String scope) {
    withScope(scope);
  }

  public ScopeBuilder(String... scopes) {
    withScopes(scopes);
  }

  public ScopeBuilder(Collection<String> scopes) {
    withScopes(scopes);
  }

  public final ScopeBuilder withScope(String scope) {
    scopes.add(scope);
    return this;
  }

  public final ScopeBuilder withScopes(String... scopes) {
    this.scopes.addAll(Arrays.asList(scopes));
    return this;
  }

  public final ScopeBuilder withScopes(Collection<String> scopes) {
    this.scopes.addAll(scopes);
    return this;
  }

  public String build() {
    final StringBuilder scopeBuilder = new StringBuilder();
    for (String scope : scopes) {
      scopeBuilder.append(' ').append(scope);
    }
    return scopeBuilder.substring(1);
  }
}
