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
package com.github.scribejava.core.oauth2;

import java.util.EnumSet;
import java.util.Set;

/** Énumération des codes d'erreur OAuth 2.0 et OpenID Connect standards. */
public enum OAuth2Error {
  /** RFC 6749, 5.2 Error Response. */
  INVALID_REQUEST("invalid_request"),
  /** RFC 6749, 5.2 Error Response. */
  UNAUTHORIZED_CLIENT("unauthorized_client"),
  /** RFC 6749, 5.2 Error Response. */
  ACCESS_DENIED("access_denied"),
  /** RFC 6749, 5.2 Error Response. */
  UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type"),
  /** RFC 6749, 5.2 Error Response. */
  INVALID_SCOPE("invalid_scope"),
  /** RFC 6749, 5.2 Error Response. */
  SERVER_ERROR("server_error"),
  /** RFC 6749, 5.2 Error Response. */
  TEMPORARILY_UNAVAILABLE("temporarily_unavailable"),
  /** RFC 6749, 5.2 Error Response. */
  INVALID_CLIENT("invalid_client"),
  /** RFC 6749, 5.2 Error Response. */
  INVALID_GRANT("invalid_grant"),
  /** RFC 6749, 5.2 Error Response. */
  UNSUPPORTED_GRANT_TYPE("unsupported_grant_type"),
  /** RFC 6750, 6.2. */
  INVALID_TOKEN("invalid_token"),
  /** RFC 6750, 6.2. */
  INSUFFICIENT_SCOPE("insufficient_scope"),
  /** RFC 7009, 4.1. */
  UNSUPPORTED_TOKEN_TYPE("unsupported_token_type"),
  /** RFC 8628, 3.5. */
  AUTHORIZATION_PENDING("authorization_pending"),
  /** RFC 8628, 3.5. */
  SLOW_DOWN("slow_down"),
  /** RFC 8628, 3.5. */
  EXPIRED_TOKEN("expired_token"),

  // --- Erreurs spécifiques OIDC ---
  /** OIDC Core 1.0, 3.1.2.6. */
  INTERACTION_REQUIRED("interaction_required"),
  /** OIDC Core 1.0, 3.1.2.6. */
  LOGIN_REQUIRED("login_required"),
  /** OIDC Core 1.0, 3.1.2.6. */
  ACCOUNT_SELECTION_REQUIRED("account_selection_required"),
  /** OIDC Core 1.0, 3.1.2.6. */
  CONSENT_REQUIRED("consent_required"),
  /** OIDC Core 1.0, 3.1.2.6. */
  INVALID_REQUEST_URI("invalid_request_uri"),
  /** OIDC Core 1.0, 3.1.2.6. */
  INVALID_REQUEST_OBJECT("invalid_request_object"),
  /** OIDC Core 1.0, 3.1.2.6. */
  REQUEST_NOT_SUPPORTED("request_not_supported"),
  /** OIDC Core 1.0, 3.1.2.6. */
  REQUEST_URI_NOT_SUPPORTED("request_uri_not_supported"),
  /** OIDC Core 1.0, 3.1.2.6. */
  REGISTRATION_NOT_SUPPORTED("registration_not_supported");

  private static final Set<OAuth2Error> VALUES = EnumSet.allOf(OAuth2Error.class);

  private final String errorString;

  OAuth2Error(String errorString) {
    this.errorString = errorString;
  }

  /**
   * Analyse la chaîne de caractères fournie.
   *
   * @param errorString La chaîne brute.
   * @return OAuth2Error
   * @throws IllegalArgumentException si le code d'erreur est inconnu.
   */
  public static OAuth2Error parseFrom(String errorString) {
    for (OAuth2Error error : VALUES) {
      if (error.errorString.equals(errorString)) {
        return error;
      }
    }
    throw new IllegalArgumentException("There is no knowledge about '" + errorString + "' Error");
  }
}
