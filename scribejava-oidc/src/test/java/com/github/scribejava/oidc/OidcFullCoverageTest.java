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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests de couverture complète pour les modèles de données OIDC. */
public class OidcFullCoverageTest {

  /**
   * Vérifie que toutes les réclamations standards (Standard Claims) sont correctement gérées.
   *
   * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims">OIDC Core,
   *     Section 5.1</a>
   */
  @Test
  public void testStandardClaimsFullCoverage() {
    final Map<String, Object> map = new HashMap<>();
    map.put("sub", "sub");
    map.put("name", "name");
    map.put("given_name", "given");
    map.put("family_name", "family");
    map.put("middle_name", "middle");
    map.put("nickname", "nick");
    map.put("preferred_username", "user");
    map.put("profile", "profile");
    map.put("picture", "picture");
    map.put("website", "website");
    map.put("email", "email");
    map.put("email_verified", true);
    map.put("gender", "gender");
    map.put("birthdate", "birthdate");
    map.put("zoneinfo", "zoneinfo");
    map.put("locale", "locale");
    map.put("phone_number", "phone");
    map.put("phone_number_verified", true);
    map.put("address", Collections.singletonMap("formatted", "addr"));
    map.put("updated_at", 123456789L);

    final StandardClaims claims = new StandardClaims(map);
    assertThat(claims.getSub()).contains("sub");
    assertThat(claims.getName()).contains("name");
    assertThat(claims.getGivenName()).contains("given");
    assertThat(claims.getFamilyName()).contains("family");
    assertThat(claims.getMiddleName()).contains("middle");
    assertThat(claims.getNickname()).contains("nick");
    assertThat(claims.getPreferredUsername()).contains("user");
    assertThat(claims.getProfile()).contains("profile");
    assertThat(claims.getPicture()).contains("picture");
    assertThat(claims.getWebsite()).contains("website");
    assertThat(claims.getEmail()).contains("email");
    assertThat(claims.isEmailVerified()).contains(true);
    assertThat(claims.getGender()).contains("gender");
    assertThat(claims.getBirthdate()).contains("birthdate");
    assertThat(claims.getZoneinfo()).contains("zoneinfo");
    assertThat(claims.getLocale()).contains("locale");
    assertThat(claims.getPhoneNumber()).contains("phone");
    assertThat(claims.isPhoneNumberVerified()).contains(true);
    assertThat(claims.getAddress()).isPresent();
    assertThat(claims.getUpdatedAt()).contains(123456789L);
  }

  /** Vérifie que la classe de base DefaultOidcApi20 expose correctement les métadonnées. */
  @Test
  public void testDefaultOidcApi20FullCoverage() {
    final DefaultOidcApi20 api =
        new DefaultOidcApi20() {
          @Override
          public String getIssuer() {
            return "https://idp.com";
          }
        };

    assertThat(api.getAccessTokenEndpoint()).isNull();
    assertThat(api.getAuthorizationBaseUrl()).isNull();
    assertThat(api.getJwksUri()).isNull();
    assertThat(api.getUserinfoEndpoint()).isNull();

    final OidcProviderMetadata metadata =
        new OidcProviderMetadata(
            "iss",
            "auth",
            "token",
            "jwks",
            null,
            null,
            null,
            "userinfo",
            "reg",
            null,
            null,
            null,
            "rev",
            "intro",
            "par",
            null);
    api.setMetadata(metadata);

    assertThat(api.getAccessTokenEndpoint()).isEqualTo("token");
    assertThat(api.getAuthorizationBaseUrl()).isEqualTo("auth");
    assertThat(api.getRevokeTokenEndpoint()).isEqualTo("rev");
    assertThat(api.getPushedAuthorizationRequestEndpoint()).isEqualTo("par");
    assertThat(api.getJwksUri()).isEqualTo("jwks");
    assertThat(api.getUserinfoEndpoint()).isEqualTo("userinfo");
    assertThat(api.getMetadata()).isEqualTo(metadata);
  }
}
