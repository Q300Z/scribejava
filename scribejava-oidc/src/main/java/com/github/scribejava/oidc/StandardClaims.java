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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/** OpenID Connect Standard Claims. */
public class StandardClaims {

  private final Map<String, Object> claims;

  public StandardClaims(final Map<String, Object> claims) {
    this.claims = claims != null ? Collections.unmodifiableMap(claims) : Collections.emptyMap();
  }

  public Optional<String> getSub() {
    return getClaim("sub");
  }

  public Optional<String> getName() {
    return getClaim("name");
  }

  public Optional<String> getGivenName() {
    return getClaim("given_name");
  }

  public Optional<String> getFamilyName() {
    return getClaim("family_name");
  }

  public Optional<String> getMiddleName() {
    return getClaim("middle_name");
  }

  public Optional<String> getNickname() {
    return getClaim("nickname");
  }

  public Optional<String> getPreferredUsername() {
    return getClaim("preferred_username");
  }

  public Optional<String> getProfile() {
    return getClaim("profile");
  }

  public Optional<String> getPicture() {
    return getClaim("picture");
  }

  public Optional<String> getWebsite() {
    return getClaim("website");
  }

  public Optional<String> getEmail() {
    return getClaim("email");
  }

  public Optional<Boolean> isEmailVerified() {
    return getClaim("email_verified");
  }

  public Optional<String> getGender() {
    return getClaim("gender");
  }

  public Optional<String> getBirthdate() {
    return getClaim("birthdate");
  }

  public Optional<String> getZoneinfo() {
    return getClaim("zoneinfo");
  }

  public Optional<String> getLocale() {
    return getClaim("locale");
  }

  public Optional<String> getPhoneNumber() {
    return getClaim("phone_number");
  }

  public Optional<Boolean> isPhoneNumberVerified() {
    return getClaim("phone_number_verified");
  }

  public Optional<Object> getAddress() {
    return getClaim("address");
  }

  public Optional<Long> getUpdatedAt() {
    return getClaim("updated_at");
  }

  @SuppressWarnings("unchecked")
  private <T> Optional<T> getClaim(final String key) {
    return Optional.ofNullable((T) claims.get(key));
  }

  public Map<String, Object> getAllClaims() {
    return claims;
  }
}
