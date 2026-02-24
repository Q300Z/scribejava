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

import static org.mockito.Mockito.*;

import com.github.scribejava.core.model.OAuth2AccessToken;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

class AuditedTokenAutoRenewerTest {

  @Test
  void shouldNotifyListenerOnRefresh() throws InterruptedException, ExecutionException {
    // Given
    TokenRepository<String, ExpiringTokenWrapper> mockRepo = mock(TokenRepository.class);
    AuthEventListener<String> mockListener = mock(AuthEventListener.class);

    OAuth2AccessToken oldToken = new OAuth2AccessToken("old", null, -10, "refresh", null, null);
    OAuth2AccessToken newToken = new OAuth2AccessToken("new", null, 3600, "refresh", null, null);

    when(mockRepo.findByKey("user1")).thenReturn(Optional.of(new ExpiringTokenWrapper(oldToken)));

    TokenAutoRenewer<String> renewer = new TokenAutoRenewer<>(mockRepo, t -> newToken);
    renewer.setListener(mockListener);

    // When
    renewer.getValidToken("user1");

    // Then
    verify(mockListener).onTokenRefreshed(eq("user1"), any(ExpiringTokenWrapper.class));
  }
}
