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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthLogger;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.oidc.jar.JarAuthorizationRequestConverter;
import com.github.scribejava.oidc.model.JwtSigner;
import com.github.scribejava.oidc.model.OidcNonce;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OidcServiceEdgeCasesTest {

  private static final String ISSUER = "https://issuer.example.com";
  private static final String CLIENT_ID = "client-id";
  private RSAKey rsaKey;

  @BeforeEach
  public void setUp() throws Exception {
    rsaKey = new RSAKeyGenerator(2048).keyID("rsa-1").generate();
  }

  private String generateValidIdToken() throws Exception {
    final JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .issuer(ISSUER)
            .audience(CLIENT_ID)
            .subject("user123")
            .claim("nonce", "nonce123456789012")
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600000))
            .build();
    final JWSSigner signer = new RSASSASigner(rsaKey);
    final SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(), claimsSet);
    signedJWT.sign(signer);
    return signedJWT.serialize();
  }

  @Test
  public void shouldHandleNullIdToken() {
    final DefaultOidcApi20 api = mock(DefaultOidcApi20.class);
    final OidcService service =
        new OidcService(api, "key", "secret", null, null, null, null, "userAgent", null, null);
    assertThat(service).isNotNull();
  }

  @Test
  public void shouldValidateIdTokenWithValidator() throws Exception {
    final DefaultOidcApi20 api = mock(DefaultOidcApi20.class);
    final OidcService service =
        new OidcService(api, "key", "secret", null, null, null, null, "userAgent", null, null);

    final IdTokenValidator validator = mock(IdTokenValidator.class);
    final String rawToken = "raw-id-token";
    final OidcNonce nonce = new OidcNonce("nonce123456789012");
    final IdToken mockIdToken = mock(IdToken.class);

    when(validator.validate(rawToken, nonce, 0)).thenReturn(mockIdToken);
    when(validator.validate(rawToken, null, 0)).thenReturn(mockIdToken);

    service.setIdTokenValidator(validator);
    assertThat(service.getIdTokenValidator()).isSameAs(validator);

    final OAuth2AccessToken token = new OAuth2AccessToken("access-token", rawToken);
    final IdToken validated = service.validateIdToken(token, nonce);
    assertThat(validated).isSameAs(mockIdToken);

    verify(validator).validate(rawToken, nonce, 0);
  }

  @Test
  public void shouldValidateIdTokenWithoutValidator() throws Exception {
    final DefaultOidcApi20 api = mock(DefaultOidcApi20.class);
    final OidcService service =
        new OidcService(api, "key", "secret", null, null, null, null, "userAgent", null, null);

    final String rawIdToken = generateValidIdToken();
    final String tokenResponse = "{\"access_token\":\"abc\",\"id_token\":\"" + rawIdToken + "\"}";
    final OAuth2AccessToken token = new OAuth2AccessToken("abc", tokenResponse);

    final IdToken idToken = service.validateIdToken(token, null);
    assertThat(idToken).isNotNull();
    assertThat(idToken.getClaims().get("iss")).isEqualTo(ISSUER);
  }

  @Test
  public void shouldGetAccessTokenAndValidate() throws Exception {
    final OidcGoogleApi20 api = OidcGoogleApi20.instance();
    final HttpClient httpClient = mock(HttpClient.class);
    final Response response = mock(Response.class);
    when(response.getCode()).thenReturn(200);

    final String rawIdToken = generateValidIdToken();
    final String tokenResponse = "{\"access_token\":\"abc\",\"id_token\":\"" + rawIdToken + "\"}";
    when(response.getBody()).thenReturn(tokenResponse);

    doAnswer(
            invocation -> {
              final com.github.scribejava.core.model.OAuthRequest.ResponseConverter<?> converter =
                  invocation.getArgument(6);
              final Object result = converter != null ? converter.convert(response) : response;
              return CompletableFuture.completedFuture(result);
            })
        .when(httpClient)
        .executeAsync(any(), any(), any(), any(), (byte[]) any(), any(), any());

    final OidcService service =
        new OidcService(
            api,
            CLIENT_ID,
            "secret",
            "http://callback",
            "openid",
            "code",
            null,
            "ua",
            null,
            httpClient);

    final OAuth2AccessToken token = service.getAccessToken("auth-code");
    assertThat(token).isNotNull();
    assertThat(token.getAccessToken()).isEqualTo("abc");
  }

  @Test
  public void shouldGetUserInfoAsync() throws Exception {
    final DefaultOidcApi20 api = mock(DefaultOidcApi20.class);
    final OidcService service =
        new OidcService(api, "key", "secret", null, null, null, null, "userAgent", null, null);

    final OAuth2AccessToken token = new OAuth2AccessToken("access-token");
    final StandardClaims claims = service.getUserInfoAsync(token).get();
    assertThat(claims).isNotNull();
    assertThat(claims.getAllClaims()).isEmpty();
  }

  @Test
  public void shouldSupportBuilderFluentApis() {
    final OidcServiceBuilder builder =
        new OidcServiceBuilder("client-id")
            .defaultScope("openid")
            .callback("http://callback")
            .httpClient(mock(HttpClient.class))
            .apiSecret("secret")
            .logger(mock(OAuthLogger.class));

    assertThat(builder).isNotNull();
  }

  @Test
  public void shouldBuildWithDiscovery() throws Exception {
    final OidcGoogleApi20 api = OidcGoogleApi20.instance();
    final HttpClient httpClient = mock(HttpClient.class);

    final OidcDiscoveryService discoveryService = mock(OidcDiscoveryService.class);
    final com.github.scribejava.core.oauth.DiscoveredEndpoints endpoints =
        new com.github.scribejava.core.oauth.DiscoveredEndpoints("https://auth", "https://token");

    when(discoveryService.discoverAsync()).thenReturn(CompletableFuture.completedFuture(endpoints));

    final OidcServiceBuilder builder =
        new OidcServiceBuilder(CLIENT_ID)
            .httpClient(httpClient)
            .baseOnDiscovery(ISSUER, httpClient, "ua");

    // Manually inject mocked discovery service
    final java.lang.reflect.Field field =
        builder.getClass().getSuperclass().getDeclaredField("discoveryService");
    field.setAccessible(true);
    field.set(builder, discoveryService);

    final OidcService service = builder.build(api);
    assertThat(service).isNotNull();
    assertThat(service.getApi().getAuthorizationBaseUrl()).isEqualTo("https://auth");
    assertThat(service.getApi().getAccessTokenEndpoint()).isEqualTo("https://token");
  }

  @Test
  public void shouldBuildWithDiscoveryFailure() throws Exception {
    final OidcGoogleApi20 api = OidcGoogleApi20.instance();
    final HttpClient httpClient = mock(HttpClient.class);

    final OidcDiscoveryService discoveryService = mock(OidcDiscoveryService.class);
    final CompletableFuture<com.github.scribejava.core.oauth.DiscoveredEndpoints> future =
        new CompletableFuture<>();
    future.completeExceptionally(new ExecutionException(new RuntimeException("Discovery Failed")));

    when(discoveryService.discoverAsync()).thenReturn(future);

    final OidcServiceBuilder builder =
        new OidcServiceBuilder(CLIENT_ID)
            .httpClient(httpClient)
            .baseOnDiscovery(ISSUER, httpClient, "ua");

    // Manually inject mocked discovery service
    final java.lang.reflect.Field field =
        builder.getClass().getSuperclass().getDeclaredField("discoveryService");
    field.setAccessible(true);
    field.set(builder, discoveryService);

    assertThrows(OAuthException.class, () -> builder.build(api));
  }

  @Test
  public void shouldBuildWithCustomConfiguration() {
    final OidcGoogleApi20 api = OidcGoogleApi20.instance();
    final OidcServiceBuilder builder =
        new OidcServiceBuilder(CLIENT_ID).logger(mock(OAuthLogger.class));

    final JarAuthorizationRequestConverter converter = mock(JarAuthorizationRequestConverter.class);
    builder.authorizationRequestConverter(converter);

    final OidcService service = builder.build(api);
    assertThat(service).isNotNull();
  }

  @Test
  public void shouldBuildWithDebugStream() {
    final OidcGoogleApi20 api = OidcGoogleApi20.instance();
    final OidcServiceBuilder builder = new OidcServiceBuilder(CLIENT_ID);
    builder.debugStream(new ByteArrayOutputStream());

    final OidcService service = builder.build(api);
    assertThat(service).isNotNull();
  }

  @Test
  public void shouldConfigureSecuredAuthorizationRequest() {
    final OidcServiceBuilder builder = new OidcServiceBuilder(CLIENT_ID);
    final PrivateKey mockKey = mock(PrivateKey.class);
    final JwtSigner mockSigner = mock(JwtSigner.class);

    builder.jwtSecuredAuthorizationRequest("audience", mockKey, "key-id", mockSigner);

    final OidcGoogleApi20 api = OidcGoogleApi20.instance();
    final OidcService service = builder.build(api);
    assertThat(service).isNotNull();
  }
}
