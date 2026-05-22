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
package com.github.scribejava.core.dpop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import org.junit.jupiter.api.Test;

/**
 * Tests additionnels pour {@link DPoPInterceptor} vérifiant la prise en charge de l'access token.
 */
class DPoPInterceptorUpdateTest {

  /** Vérifie que le constructeur avec accessToken lie correctement le token à la preuve DPoP. */
  @Test
  void shouldBindAccessTokenUsingConstructor() {
    DPoPProofCreator mockCreator = mock(DPoPProofCreator.class);
    when(mockCreator.createDPoPProof(any(), any())).thenReturn("fake_proof_with_ath");

    DPoPInterceptor interceptor = new DPoPInterceptor(mockCreator, "my_access_token_123");
    assertEquals("my_access_token_123", interceptor.getAccessToken());

    OAuthRequest request = new OAuthRequest(Verb.POST, "https://example.com/api");
    interceptor.intercept(request);

    verify(mockCreator).createDPoPProof(request, "my_access_token_123");
    assertEquals("fake_proof_with_ath", request.getHeaders().get("DPoP"));
  }

  /** Vérifie que le setter lie dynamiquement le token à la preuve DPoP. */
  @Test
  void shouldBindAccessTokenUsingSetter() {
    DPoPProofCreator mockCreator = mock(DPoPProofCreator.class);
    when(mockCreator.createDPoPProof(any(), any())).thenReturn("fake_proof_updated");

    DPoPInterceptor interceptor = new DPoPInterceptor(mockCreator);
    assertNull(interceptor.getAccessToken());

    interceptor.setAccessToken("new_token");
    assertEquals("new_token", interceptor.getAccessToken());

    OAuthRequest request = new OAuthRequest(Verb.GET, "https://example.com/api");
    interceptor.intercept(request);

    verify(mockCreator).createDPoPProof(request, "new_token");
    assertEquals("fake_proof_updated", request.getHeaders().get("DPoP"));
  }
}
