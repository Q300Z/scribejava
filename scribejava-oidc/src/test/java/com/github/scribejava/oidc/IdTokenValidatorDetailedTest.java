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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.utils.JsonUtils;
import com.github.scribejava.oidc.model.JwtSignatureVerifier;
import com.github.scribejava.oidc.model.OidcKey;
import java.lang.reflect.Field;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests détaillés pour IdTokenValidator (TDD). */
public class IdTokenValidatorDetailedTest {

  private IdTokenValidator validator;
  private static final String ISSUER = "https://server.com";
  private static final String CLIENT_ID = "client123";

  @BeforeEach
  public void setUp() throws Exception {
    final PublicKey mockPublicKey = mock(PublicKey.class);
    final OidcKey mockOidcKey = mock(OidcKey.class);
    when(mockOidcKey.getPublicKey()).thenReturn(mockPublicKey);

    final Map<String, OidcKey> keys = Collections.singletonMap("key1", mockOidcKey);
    validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);

    // Forcer le succès de la signature pour tester les claims (via réflexion sur le champ privé)
    final JwtSignatureVerifier mockVerifier = mock(JwtSignatureVerifier.class);
    when(mockVerifier.verifyRS256(any(), any(), any())).thenReturn(true);

    final Field field = IdTokenValidator.class.getDeclaredField("signatureVerifier");
    field.setAccessible(true);
    field.set(validator, mockVerifier);
  }

  @Test
  public void shouldFailOnExpiredToken() {
    final long past = System.currentTimeMillis() / 1000 - 3600;
    final String expiredToken = createMockToken(ISSUER, CLIENT_ID, past);

    assertThatExceptionOfType(OAuthException.class)
        .isThrownBy(() -> validator.validate(expiredToken, null, 0))
        .withMessageContaining("Token has expired");
  }

  @Test
  public void shouldFailOnIssuerMismatch() {
    final long future = System.currentTimeMillis() / 1000 + 3600;
    final String wrongIssuerToken = createMockToken("https://evil.com", CLIENT_ID, future);

    assertThatExceptionOfType(OAuthException.class)
        .isThrownBy(() -> validator.validate(wrongIssuerToken, null, 0))
        .withMessageContaining("Issuer mismatch");
  }

  @Test
  public void shouldFailOnAudienceMismatch() {
    final long future = System.currentTimeMillis() / 1000 + 3600;
    final String wrongAudToken = createMockToken(ISSUER, "wrong_client", future);

    assertThatExceptionOfType(OAuthException.class)
        .isThrownBy(() -> validator.validate(wrongAudToken, null, 0))
        .withMessageContaining("Audience mismatch");
  }

  private String createMockToken(String iss, String aud, long exp) {
    final Map<String, Object> header = new HashMap<>();
    header.put("alg", "RS256");
    header.put("typ", "JWT");
    header.put("kid", "key1");

    final Map<String, Object> payload = new HashMap<>();
    payload.put("iss", iss);
    payload.put("sub", "user123");
    payload.put("aud", aud);
    payload.put("exp", exp);
    payload.put("iat", exp - 3600);

    final String h =
        Base64.getUrlEncoder().withoutPadding().encodeToString(JsonUtils.toJson(header).getBytes());
    final String p =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(JsonUtils.toJson(payload).getBytes());
    return h + "." + p + ".mock_signature";
  }
}
