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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth2.grant.OAuth20Grant;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.oidc.IdToken;
import com.github.scribejava.oidc.OidcService;
import com.github.scribejava.oidc.StandardClaims;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

class OidcAuthFlowCoordinatorTest {

  @Test
  void shouldFailIfIdTokenInvalid() throws IOException, InterruptedException, ExecutionException {
    // Given
    OidcService mockOidcService = mock(OidcService.class);
    TokenRepository<String, ExpiringTokenWrapper> mockRepo = mock(TokenRepository.class);

    AuthSessionContext sessionContext =
        new AuthSessionContext("state", "nonce1234567890123456", new PKCE());
    OAuth2AccessToken token = mock(OAuth2AccessToken.class);

    // Fix ambiguity: use the specific grant type method
    when(mockOidcService.getAccessToken(any(OAuth20Grant.class))).thenReturn(token);
    when(mockOidcService.validateIdToken(any(), any()))
        .thenThrow(new IllegalArgumentException("Invalid ID Token"));

    OidcAuthFlowCoordinator<String> coordinator =
        new OidcAuthFlowCoordinator<>(mockOidcService, mockRepo);

    // When & Then
    assertThatThrownBy(
            () -> coordinator.finishAuthorization("user1", "code", "state", sessionContext))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldCallUserInfoIfEmailIsMissingInIdToken()
      throws IOException, InterruptedException, ExecutionException {
    // Given
    OidcService mockOidcService = mock(OidcService.class);
    TokenRepository<String, ExpiringTokenWrapper> mockRepo = mock(TokenRepository.class);
    AuthSessionContext sessionContext =
        new AuthSessionContext("state", "nonce1234567890123456", new PKCE());

    OAuth2AccessToken token = mock(OAuth2AccessToken.class);
    IdToken idToken = mock(IdToken.class);

    // StandardClaims is immutable
    StandardClaims initialClaims = new StandardClaims(Collections.emptyMap());

    when(idToken.getStandardClaims()).thenReturn(initialClaims);
    when(mockOidcService.getAccessToken(any(OAuth20Grant.class))).thenReturn(token);
    when(mockOidcService.validateIdToken(any(), any())).thenReturn(idToken);

    // Mock UserInfo claims
    Map<String, Object> claimsMap = new HashMap<>();
    claimsMap.put("email", "fallback@example.com");
    StandardClaims fallbackClaims = new StandardClaims(claimsMap);

    when(mockOidcService.getUserInfoAsync(token))
        .thenReturn(CompletableFuture.completedFuture(fallbackClaims));

    OidcAuthFlowCoordinator<String> coordinator =
        new OidcAuthFlowCoordinator<>(mockOidcService, mockRepo);

    // When
    OidcAuthResult result =
        coordinator.finishAuthorization("user1", "code", "state", sessionContext);

    // Then
    assertThat(result.getEmail()).isEqualTo("fallback@example.com");
    verify(mockOidcService).getUserInfoAsync(token);
  }
}
