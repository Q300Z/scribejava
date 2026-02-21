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
package com.github.scribejava.core.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.dpop.DPoPProofCreator;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth2.bearersignature.BearerSignatureAuthorizationRequestHeaderField;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCECodeChallengeMethod;
import com.github.scribejava.core.pkce.PKCEService;
import com.github.scribejava.core.revoke.TokenTypeHint;
import org.junit.jupiter.api.Test;

public class OAuth20SecurityResilienceTest {

  @Test
  public void shouldFallbackToPlainPKCEWhenS256NotAvailable() {
    final PKCEService pkceService = new PKCEService();
    final PKCE pkce = pkceService.generatePKCE();
    assertThat(pkce.getCodeChallengeMethod()).isEqualTo(PKCECodeChallengeMethod.S256);
    assertThat(pkce.getCodeVerifier()).isNotNull();
    assertThat(pkce.getCodeChallenge()).isNotNull();
  }

  @Test
  public void shouldAddDPoPHeaderWhenCreatorIsPresent() {
    final DPoPProofCreator mockCreator = mock(DPoPProofCreator.class);
    when(mockCreator.createDPoPProof(any(OAuthRequest.class), anyString()))
        .thenReturn("mock-dpop-jwt");

    final DefaultApi20 api = mock(DefaultApi20.class);
    when(api.getAccessTokenVerb()).thenReturn(Verb.POST);
    when(api.getAccessTokenEndpoint()).thenReturn("http://example.com/token");
    when(api.getClientAuthentication()).thenReturn(HttpBasicAuthenticationScheme.instance());
    when(api.getBearerSignature())
        .thenReturn(BearerSignatureAuthorizationRequestHeaderField.instance());

    final OAuth20Service service =
        new OAuth20Service(
            api, "key", "secret", null, null, null, null, null, null, null, mockCreator);

    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
    service.signRequest("token", request);

    assertThat(request.getHeaders()).containsKey("DPoP");
    assertThat(request.getHeaders().get("DPoP")).isEqualTo("mock-dpop-jwt");
  }

  @Test
  public void shouldHandleRevocationWithHints() {
    final DefaultApi20 api = mock(DefaultApi20.class);
    when(api.getAccessTokenVerb()).thenReturn(Verb.POST);
    when(api.getAccessTokenEndpoint()).thenReturn("http://example.com/token");
    when(api.getRevokeTokenEndpoint()).thenReturn("http://example.com/revoke");
    when(api.getClientAuthentication()).thenReturn(HttpBasicAuthenticationScheme.instance());

    final OAuth20Service service =
        new OAuth20Service(api, "key", "secret", null, null, null, null, null, null, null);

    final OAuthRequest accessRequest =
        service.createRevokeTokenRequest("token", TokenTypeHint.ACCESS_TOKEN);
    assertThat(accessRequest.getBodyParams().asFormUrlEncodedString())
        .contains("token_type_hint=access_token");

    final OAuthRequest refreshRequest =
        service.createRevokeTokenRequest("token", TokenTypeHint.REFRESH_TOKEN);
    assertThat(refreshRequest.getBodyParams().asFormUrlEncodedString())
        .contains("token_type_hint=refresh_token");
  }

  @Test
  public void shouldSupportCustomGrantParameters() {
    final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com/token");
    request.addParameter(OAuthConstants.GRANT_TYPE, "custom");
    request.addParameter("extra", "value");

    assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("grant_type=custom");
    assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("extra=value");
  }
}
