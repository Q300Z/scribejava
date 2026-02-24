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
import static org.mockito.Mockito.*;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

class AuthorizedClientServiceTest {

  @Test
  void shouldFetchTokenSignAndExecuteRequest()
      throws IOException, InterruptedException, ExecutionException {
    // Given
    OAuth20Service mockOauthService = mock(OAuth20Service.class);
    TokenAutoRenewer<String> mockRenewer = mock(TokenAutoRenewer.class);
    Response mockResponse = mock(Response.class);

    OAuth2AccessToken token = new OAuth2AccessToken("access");
    String userId = "user123";
    OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.example.com");

    when(mockRenewer.getValidToken(userId)).thenReturn(token);
    when(mockOauthService.execute(request)).thenReturn(mockResponse);

    AuthorizedClientService<String> clientService =
        new AuthorizedClientService<>(mockOauthService, mockRenewer);

    // When
    Response result = clientService.execute(userId, request);

    // Then
    verify(mockRenewer).getValidToken(userId);
    verify(mockOauthService).signRequest(token, request);
    verify(mockOauthService).execute(request);
    assertThat(result).isEqualTo(mockResponse);
  }
}
