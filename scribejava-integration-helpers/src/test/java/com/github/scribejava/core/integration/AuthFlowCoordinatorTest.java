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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

class AuthFlowCoordinatorTest {

  @Test
  void shouldValidateStateAndExchangeCode()
      throws IOException, InterruptedException, ExecutionException {
    // Given
    OAuth20Service mockService = mock(OAuth20Service.class);
    TokenRepository<String, ExpiringTokenWrapper> mockRepo = mock(TokenRepository.class);

    String code = "auth_code";
    String state = "secure_state";
    String userId = "user1";
    OAuth2AccessToken token = new OAuth2AccessToken("access");

    when(mockService.getAccessToken(any(AuthorizationCodeGrant.class))).thenReturn(token);

    AuthFlowCoordinator<String> coordinator = new AuthFlowCoordinator<>(mockService, mockRepo);

    // When
    AuthResult result = coordinator.finishAuthorization(userId, code, state, state);

    // Then
    assertThat(result.getToken()).isEqualTo(token);
    verify(mockService).getAccessToken(argThat(grant -> grant.getCode().equals(code)));
    verify(mockRepo).save(eq(userId), any(ExpiringTokenWrapper.class));
  }

  @Test
  void shouldFailIfStateIsInvalid() {
    // Given
    AuthFlowCoordinator<String> coordinator =
        new AuthFlowCoordinator<>(mock(OAuth20Service.class), mock(TokenRepository.class));

    // When & Then
    assertThatThrownBy(
            () -> coordinator.finishAuthorization("user1", "code", "wrong_state", "expected_state"))
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("CSRF");
  }
}
