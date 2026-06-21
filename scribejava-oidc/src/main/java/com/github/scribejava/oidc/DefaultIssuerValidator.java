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
import java.util.regex.Pattern;

/**
 * Default implementation of {@link IssuerValidator} supporting standard matching and multi-tenant
 * placeholders.
 */
public class DefaultIssuerValidator implements IssuerValidator {

  /**
   * {@inheritDoc}
   *
   * @param configuredIssuer the issuer URL configured in the application
   * @param claimIssuer the issuer URL present in the token's claims
   * @param claims the complete map of claims present in the token
   * @return {@code true} if the claim issuer is valid according to the configured issuer; {@code
   *     false} otherwise
   */
  @Override
  public boolean isValid(String configuredIssuer, String claimIssuer, Map<String, Object> claims) {
    if (configuredIssuer == null || claimIssuer == null) {
      return false;
    }

    final String normConfigured = normalizeIssuer(configuredIssuer);
    final String normClaim = normalizeIssuer(claimIssuer);

    if (normConfigured.equals(normClaim)) {
      return true;
    }

    // 1. Essai de correspondance par Microsoft tenant ID (tid) si présent dans les claims
    if (claims != null) {
      final String tid = (String) claims.get("tid");
      if (tid != null && !tid.isEmpty()) {
        final String replaced =
            normConfigured
                .replace("{tenantid}", tid)
                .replace("common", tid)
                .replace("organizations", tid)
                .replace("consumers", tid);
        if (replaced.equals(normClaim)) {
          return true;
        }
      }
    }

    // 2. Remplacement et correspondance générique pour les patterns multi-tenant Microsoft
    if (normConfigured.contains("{tenantid}")
        || normConfigured.contains("common")
        || normConfigured.contains("organizations")
        || normConfigured.contains("consumers")) {
      final String marked =
          normConfigured
              .replace("{tenantid}", "###TENANT###")
              .replace("common", "###TENANT###")
              .replace("organizations", "###TENANT###")
              .replace("consumers", "###TENANT###");
      final String regexPattern =
          "^" + Pattern.quote(marked).replace("###TENANT###", "\\E[^/]+\\Q") + "$";
      if (normClaim.matches(regexPattern)) {
        return true;
      }
    }

    // 3. Correspondance Okta ou personnalisée avec placeholder {tenant}
    if (normConfigured.contains("{tenant}")) {
      final String marked = normConfigured.replace("{tenant}", "###TENANT###");
      final String regexPattern =
          "^" + Pattern.quote(marked).replace("###TENANT###", "\\E[^/]+\\Q") + "$";
      if (normClaim.matches(regexPattern)) {
        return true;
      }
    }

    return false;
  }

  private String normalizeIssuer(String url) {
    if (url == null) {
      return "";
    }
    String normalized = url.trim();
    while (normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }
}
