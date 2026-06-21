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

/**
 * Interface for validating the OpenID Connect token issuer, support for multi-tenant
 * configurations.
 */
public interface IssuerValidator {
  /**
   * Validates if the claim issuer matches the configured issuer, considering any multi-tenant
   * rules.
   *
   * @param configuredIssuer the issuer URL configured in the application
   * @param claimIssuer the issuer URL present in the token's claims
   * @param claims the complete map of claims present in the token (may be used for tenant
   *     verification)
   * @return {@code true} if the claim issuer is valid according to the configured issuer; {@code
   *     false} otherwise
   */
  boolean isValid(String configuredIssuer, String claimIssuer, Map<String, Object> claims);
}
