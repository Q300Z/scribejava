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
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.JsonBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.oidc.dpop.DefaultDPoPProofCreator;
import com.github.scribejava.oidc.model.DefaultSignatureVerifier;
import com.github.scribejava.oidc.model.OidcKey;
import com.github.scribejava.oidc.model.OidcNonce;
import com.github.scribejava.oidc.model.SignatureVerifier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests unitaires pour valider les mécanismes de durcissement (Hardening) de ScribeJava OIDC. */
public class OidcHardeningsTest {

  private MockWebServer server;
  private OidcDiscoveryService discoveryService;

  @BeforeEach
  public void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    discoveryService =
        new OidcDiscoveryService(
            server.url("/").toString(), new JDKHttpClient(), "ScribeJava-Test");
    discoveryService.setMaxAttempts(1);
  }

  @AfterEach
  public void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  public void testDefaultOidcKeyCache() {
    OidcKeyCache cache = new DefaultOidcKeyCache();
    assertThat(cache.get("kid-1")).isNull();

    Map<String, OidcKey> keys = new HashMap<>();
    OidcKey key =
        new OidcKey() {
          @Override
          public String getKid() {
            return "kid-1";
          }

          @Override
          public String getAlg() {
            return "RS256";
          }

          @Override
          public java.security.PublicKey getPublicKey() {
            return null;
          }
        };
    keys.put("kid-1", key);
    cache.putAll(keys);

    assertThat(cache.get("kid-1")).isEqualTo(key);
    cache.clear();
    assertThat(cache.get("kid-1")).isNull();
  }

  @Test
  public void testDefaultIssuerValidator() {
    IssuerValidator validator = new DefaultIssuerValidator();

    // Exact match
    assertThat(
            validator.isValid(
                "https://login.microsoftonline.com/common",
                "https://login.microsoftonline.com/common",
                null))
        .isTrue();

    // Microsoft dynamic tenant match with tid claim
    Map<String, Object> claims = new HashMap<>();
    claims.put("tid", "12345");
    assertThat(
            validator.isValid(
                "https://login.microsoftonline.com/{tenantid}/v2.0",
                "https://login.microsoftonline.com/12345/v2.0",
                claims))
        .isTrue();
    assertThat(
            validator.isValid(
                "https://login.microsoftonline.com/common/v2.0",
                "https://login.microsoftonline.com/12345/v2.0",
                claims))
        .isTrue();

    // General multi-tenant Okta/custom tenant matching
    assertThat(validator.isValid("https://{tenant}.okta.com", "https://mytenant.okta.com", null))
        .isTrue();
  }

  @Test
  public void testDiscoveryServiceJwksCacheAnd304() throws Exception {
    final String jwksJson =
        new JsonBuilder()
            .add(
                "keys",
                Collections.singletonList(
                    new JsonBuilder()
                        .add("kty", "RSA")
                        .add("use", "sig")
                        .add("kid", "123")
                        .add(
                            "n",
                            "AKZdf_vFrIs_Y_nd9Z6X_m_Z_u1P9f_vFrIs_Y_nd9Z6X_m_Z_u1P9f_vFrIs_Y_nd9Z6X_m_Z_u1P9f_vFrIs_Y_nd9Z6X_m_Z_")
                        .add("e", "AQAB")))
            .build();

    // 1er appel: retourne les clés et configure l'ETag / Cache-Control (max-age=1 seconde)
    server.enqueue(
        new MockResponse()
            .setBody(jwksJson)
            .setResponseCode(200)
            .addHeader("Cache-Control", "max-age=1")
            .addHeader("ETag", "w/12345"));

    // 2eme appel (après expiration): retourne 304 Not Modified
    server.enqueue(
        new MockResponse().setResponseCode(304).addHeader("Cache-Control", "max-age=10"));

    String jwksUri = server.url("/jwks.json").toString();

    // 1. Premier fetch -> stocke dans le cache
    Map<String, OidcKey> keys1 = discoveryService.getJwks(jwksUri);
    assertThat(keys1).containsKey("123");
    assertThat(server.getRequestCount()).isEqualTo(1);

    // 2. Second fetch instantané -> servi par le cache sans appel réseau (max-age pas expiré)
    Map<String, OidcKey> keys2 = discoveryService.getJwks(jwksUri);
    assertThat(keys2).containsKey("123");
    assertThat(server.getRequestCount()).isEqualTo(1);

    // Attente de l'expiration du cache
    Thread.sleep(1100);

    // 3. Troisième fetch -> envoie If-None-Match, reçoit 304, met à jour et retourne la clé du
    // cache
    Map<String, OidcKey> keys3 = discoveryService.getJwks(jwksUri);
    assertThat(keys3).containsKey("123");
    assertThat(server.getRequestCount()).isEqualTo(2);

    server.takeRequest(); // Première requête
    okhttp3.mockwebserver.RecordedRequest conditionalRequest = server.takeRequest();
    assertThat(conditionalRequest.getHeader("If-None-Match")).isEqualTo("w/12345");
  }

  @Test
  public void testDiscoveryServiceRetryMechanism() throws Exception {
    discoveryService.setMaxAttempts(3);
    discoveryService.setInitialDelayMs(50L);
    discoveryService.setBackoffMultiplier(1.5);

    // Enqueue two 500 responses and one 200 response
    server.enqueue(new MockResponse().setResponseCode(500));
    server.enqueue(new MockResponse().setResponseCode(500));

    final String metadataJson =
        new JsonBuilder()
            .add("issuer", server.url("/").toString())
            .add("authorization_endpoint", server.url("/authorize").toString())
            .add("token_endpoint", server.url("/token").toString())
            .add("jwks_uri", server.url("/jwks.json").toString())
            .add("response_types_supported", Collections.singletonList("code"))
            .add("subject_types_supported", Collections.singletonList("public"))
            .add("id_token_signing_alg_values_supported", Collections.singletonList("RS256"))
            .build();
    server.enqueue(new MockResponse().setBody(metadataJson).setResponseCode(200));

    OidcProviderMetadata metadata = discoveryService.getProviderMetadata();
    assertThat(metadata).isNotNull();
    assertThat(server.getRequestCount()).isEqualTo(3);
  }

  @Test
  public void testUnknownKidRateLimit() throws Exception {
    final String emptyJwksJson = new JsonBuilder().add("keys", Collections.emptyList()).build();

    server.enqueue(new MockResponse().setBody(emptyJwksJson).setResponseCode(200));

    OidcKeyCache cache = new DefaultOidcKeyCache();
    String jwksUri = server.url("/jwks.json").toString();

    IdTokenValidator validator =
        new IdTokenValidator(
            "https://issuer.example.com", "client-id", "RS256", cache, discoveryService, jwksUri);

    String header = "{\"alg\":\"RS256\",\"kid\":\"unknown-kid-1\"}";
    String payload =
        "{\"iss\":\"https://issuer.example.com\",\"aud\":\"client-id\",\"exp\":"
            + (System.currentTimeMillis() / 1000 + 3600)
            + "}";
    String rawToken =
        Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(header.getBytes(StandardCharsets.UTF_8))
            + "."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8))
            + "."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("dummy-signature".getBytes(StandardCharsets.UTF_8));

    // First attempt: should throw OAuthException and trigger exactly one JWKS reload
    assertThrows(OAuthException.class, () -> validator.validate(rawToken, null, 0));
    assertThat(server.getRequestCount()).isEqualTo(1);

    // Second attempt: should throw OAuthException and NOT trigger a second reload (under 5 min
    // cooldown)
    assertThrows(OAuthException.class, () -> validator.validate(rawToken, null, 0));
    assertThat(server.getRequestCount()).isEqualTo(1);
  }

  @Test
  public void testPkceSessionStateFlow() throws Exception {
    final DefaultOidcApi20 api = mock(DefaultOidcApi20.class);
    when(api.getAuthorizationBaseUrl()).thenReturn("https://example.com/authorize");

    final OidcService service =
        new OidcService(
            api,
            "client-id",
            "secret",
            "http://callback",
            "openid",
            "code",
            null,
            "userAgent",
            null,
            null) {
          @Override
          public OAuth2AccessToken getAccessToken(
              com.github.scribejava.core.oauth2.grant.OAuth20Grant grant) {
            return new OAuth2AccessToken(
                "abc", "{\"access_token\":\"abc\",\"id_token\":\"raw_id_token\"}");
          }
        };

    final IdTokenValidator validator = mock(IdTokenValidator.class);
    service.setIdTokenValidator(validator);

    // 1. Initialize session state
    OidcSessionState sessionState = service.initSessionState();
    assertThat(sessionState).isNotNull();
    assertThat(sessionState.getState()).isNotNull();
    assertThat(sessionState.getNonce()).isNotNull();
    assertThat(sessionState.getCodeVerifier()).isNotNull();

    // Verify state is stored
    OidcSessionState stored = service.getSessionStateStore().load(sessionState.getState());
    assertThat(stored).isSameAs(sessionState);

    // 2. Generate auth URL
    String authUrl = service.getAuthorizationUrl(sessionState);
    assertThat(authUrl).contains("state=" + sessionState.getState());
    assertThat(authUrl).contains("nonce=" + sessionState.getNonce().getValue());
    assertThat(authUrl).contains("code_challenge=");

    // 3. Complete token exchange successfully with correlation
    when(validator.validate("raw_id_token", sessionState.getNonce(), 0))
        .thenReturn(mock(IdToken.class));
    OAuth2AccessToken token = service.getAccessToken("auth-code", sessionState.getState());
    assertThat(token).isNotNull();
    assertThat(token.getAccessToken()).isEqualTo("abc");

    // Verify state is removed after success
    assertThat(service.getSessionStateStore().load(sessionState.getState())).isNull();

    // 4. Test failure on state/nonce mismatch
    OidcSessionState mismatchState = service.initSessionState();
    when(validator.validate("raw_id_token", mismatchState.getNonce(), 0))
        .thenThrow(new OAuthException("Nonce mismatch."));
    assertThrows(
        OAuthException.class, () -> service.getAccessToken("auth-code", mismatchState.getState()));

    // 5. Test with a custom OidcSessionStateStore
    OidcSessionStateStore customStore =
        new OidcSessionStateStore() {
          private OidcSessionState state;

          @Override
          public void save(OidcSessionState s) {
            this.state = s;
          }

          @Override
          public OidcSessionState load(String s) {
            // Return mismatched nonce state to trigger failure
            if (state != null) {
              return new OidcSessionState(
                  state.getState(),
                  new OidcNonce("custom-mismatched-nonce"),
                  state.getCodeVerifier());
            }
            return null;
          }

          @Override
          public void remove(String s) {
            this.state = null;
          }
        };
    service.setSessionStateStore(customStore);

    OidcSessionState customState = service.initSessionState();
    when(validator.validate("raw_id_token", customState.getNonce(), 0))
        .thenReturn(mock(IdToken.class));
    // Mock to throw on custom-mismatched-nonce
    when(validator.validate("raw_id_token", new OidcNonce("custom-mismatched-nonce"), 0))
        .thenThrow(new OAuthException("Nonce mismatch."));

    assertThrows(
        OAuthException.class, () -> service.getAccessToken("auth-code", customState.getState()));
  }

  @Test
  public void testCustomSignatureVerifier() throws Exception {
    OidcKeyCache cache = new DefaultOidcKeyCache();
    OidcKey dummyKey =
        new OidcKey() {
          @Override
          public String getKid() {
            return "kid-1";
          }

          @Override
          public String getAlg() {
            return "RS256";
          }

          @Override
          public java.security.PublicKey getPublicKey() {
            return null;
          }
        };
    cache.putAll(Collections.singletonMap("kid-1", dummyKey));

    IdTokenValidator validator =
        new IdTokenValidator("https://issuer.example.com", "client-id", "RS256", cache, null, null);

    // Stub custom SignatureVerifier
    SignatureVerifier mockVerifier = mock(SignatureVerifier.class);
    when(mockVerifier.verify(eq("RS256"), any(), any(), any())).thenReturn(true);
    validator.setSignatureVerifier(mockVerifier);

    String header = "{\"alg\":\"RS256\",\"kid\":\"kid-1\"}";
    String payload =
        "{\"iss\":\"https://issuer.example.com\",\"aud\":\"client-id\",\"exp\":"
            + (System.currentTimeMillis() / 1000 + 3600)
            + "}";
    String rawToken =
        Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(header.getBytes(StandardCharsets.UTF_8))
            + "."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8))
            + "."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("sig".getBytes(StandardCharsets.UTF_8));

    // Validation should succeed because custom verifier returns true
    IdToken token = validator.validate(rawToken, null, 0);
    assertThat(token).isNotNull();
    verify(mockVerifier).verify(eq("RS256"), any(), any(), any());

    // Change verifier to return false, validation must fail
    when(mockVerifier.verify(eq("RS256"), any(), any(), any())).thenReturn(false);
    final String rawToken2 = rawToken + "2";
    assertThrows(OAuthException.class, () -> validator.validate(rawToken2, null, 0));
  }

  @Test
  public void testDefaultSignatureVerifierCustomProviderAndAlg() throws Exception {
    DefaultSignatureVerifier verifier = new DefaultSignatureVerifier();

    // 1. Verify provider configuration
    verifier.setProviderName("NonExistentProvider");
    assertThat(verifier.getProviderName()).isEqualTo("NonExistentProvider");

    java.security.Provider customProvider =
        new java.security.Provider("TestProvider", 1.0, "info") {};
    verifier.setProvider(customProvider);
    assertThat(verifier.getProvider()).isSameAs(customProvider);
    assertThat(verifier.getProviderName()).isNull();

    // 2. Verify custom algorithm mapping registration
    verifier.registerAlgorithm("CUSTOM-RS256", "SHA256withRSA");

    // Generate actual RSA key pair and sign some content
    java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    java.security.KeyPair keyPair = keyGen.generateKeyPair();

    byte[] signedContent = "hello".getBytes(StandardCharsets.UTF_8);
    java.security.Signature sig = java.security.Signature.getInstance("SHA256withRSA");
    sig.initSign(keyPair.getPrivate());
    sig.update(signedContent);
    byte[] signature = sig.sign();

    // Verify using standard verifier settings
    DefaultSignatureVerifier standardVerifier = new DefaultSignatureVerifier();
    standardVerifier.registerAlgorithm("CUSTOM-RS256", "SHA256withRSA");

    boolean verified =
        standardVerifier.verify("RS256", signedContent, signature, keyPair.getPublic());
    assertThat(verified).isTrue();

    boolean customVerified =
        standardVerifier.verify("CUSTOM-RS256", signedContent, signature, keyPair.getPublic());
    assertThat(customVerified).isTrue();

    // Verify using non-existent provider name should fail (due to NoSuchProviderException
    // internally)
    standardVerifier.setProviderName("NonExistentProvider");
    boolean failedVerification =
        standardVerifier.verify("RS256", signedContent, signature, keyPair.getPublic());
    assertThat(failedVerification).isFalse();
  }

  @Test
  public void testJwksCooldownBug() throws Exception {
    OidcDiscoveryService mockDiscovery = mock(OidcDiscoveryService.class);
    String jwksUri = "https://example.com/jwks";
    OidcKeyCache cache = new DefaultOidcKeyCache();

    IdTokenValidator validator =
        new IdTokenValidator(
            "https://issuer.example.com", "client-id", "RS256", cache, mockDiscovery, jwksUri);

    // First, getJwks throws an exception
    when(mockDiscovery.getJwks(jwksUri)).thenThrow(new RuntimeException("JWKS fetch failed"));

    String header = "{\"alg\":\"RS256\",\"kid\":\"unknown-kid-1\"}";
    String payload =
        "{\"iss\":\"https://issuer.example.com\",\"aud\":\"client-id\",\"exp\":"
            + (System.currentTimeMillis() / 1000 + 3600)
            + "}";
    String rawToken =
        Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(header.getBytes(StandardCharsets.UTF_8))
            + "."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8))
            + "."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("dummy".getBytes(StandardCharsets.UTF_8));

    // First validation attempt: kid is not found, so it attempts reloadKeys(), which fails/throws.
    // And the validation throws an OAuthException because key is not found.
    assertThrows(OAuthException.class, () -> validator.validate(rawToken, null, 0));

    // Verify mockDiscovery.getJwks was called once.
    verify(mockDiscovery, times(1)).getJwks(jwksUri);

    // Second attempt, if getJwks succeeds now, it should call getJwks again because no cooldown was
    // set.
    reset(mockDiscovery);

    OidcKey key = mock(OidcKey.class);
    when(key.getKid()).thenReturn("unknown-kid-1");
    when(key.getAlg()).thenReturn("RS256");
    when(key.getPublicKey()).thenReturn(null);

    Map<String, OidcKey> successKeys = Collections.singletonMap("unknown-kid-1", key);
    when(mockDiscovery.getJwks(jwksUri)).thenReturn(successKeys);

    SignatureVerifier mockVerifier = mock(SignatureVerifier.class);
    when(mockVerifier.verify(any(), any(), any(), any())).thenReturn(true);
    validator.setSignatureVerifier(mockVerifier);

    // Now, validation is attempted again. It should call getJwks(jwksUri) because there was no
    // cooldown.
    validator.validate(rawToken, null, 0);
    verify(mockDiscovery, times(1)).getJwks(jwksUri);

    // Third attempt: should NOT call getJwks again because of cooldown.
    reset(mockDiscovery);
    String header2 = "{\"alg\":\"RS256\",\"kid\":\"unknown-kid-2\"}";
    String rawToken2 =
        Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(header2.getBytes(StandardCharsets.UTF_8))
            + "."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8))
            + "."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("dummy".getBytes(StandardCharsets.UTF_8));

    // This will throw because key-2 is not found and discovery was skipped/cooldown active.
    assertThrows(OAuthException.class, () -> validator.validate(rawToken2, null, 0));
    verify(mockDiscovery, never()).getJwks(jwksUri);
  }

  @Test
  public void testSessionStateExpiration() throws Exception {
    DefaultOidcSessionStateStore store = new DefaultOidcSessionStateStore();
    OidcSessionState nonExpiredState =
        new OidcSessionState(
            "state-active", new OidcNonce("nonce-active-long-value"), "verifier-active");
    OidcSessionState expiredState =
        new OidcSessionState(
            "state-expired", new OidcNonce("nonce-expired-long-value"), "verifier-expired");

    store.save(nonExpiredState);
    store.save(expiredState);

    java.lang.reflect.Field storeField =
        DefaultOidcSessionStateStore.class.getDeclaredField("store");
    storeField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, ?> internalMap = (Map<String, ?>) storeField.get(store);

    Object expiredEntry = internalMap.get("state-expired");
    assertThat(expiredEntry).isNotNull();

    java.lang.reflect.Field createdAtField = expiredEntry.getClass().getDeclaredField("createdAt");
    createdAtField.setAccessible(true);
    createdAtField.set(expiredEntry, System.currentTimeMillis() - 1_200_000L);

    OidcSessionState loadedActive = store.load("state-active");
    assertThat(loadedActive).isSameAs(nonExpiredState);

    OidcSessionState loadedExpired = store.load("state-expired");
    assertThat(loadedExpired).isNull();

    assertThat(internalMap).containsKey("state-active");
    assertThat(internalMap).doesNotContainKey("state-expired");
  }

  @Test
  public void testSignatureValidationBypassesOnCacheHit() throws Exception {
    OidcKeyCache cache = new DefaultOidcKeyCache();
    OidcKey dummyKey = mock(OidcKey.class);
    when(dummyKey.getKid()).thenReturn("kid-1");
    when(dummyKey.getAlg()).thenReturn("RS256");
    when(dummyKey.getPublicKey()).thenReturn(null);
    cache.putAll(Collections.singletonMap("kid-1", dummyKey));

    IdTokenValidator validator =
        new IdTokenValidator("https://issuer.example.com", "client-id", "RS256", cache, null, null);

    SignatureVerifier mockVerifier = mock(SignatureVerifier.class);
    when(mockVerifier.verify(eq("RS256"), any(), any(), any())).thenReturn(true);
    validator.setSignatureVerifier(mockVerifier);

    String header = "{\"alg\":\"RS256\",\"kid\":\"kid-1\"}";
    String payload =
        "{\"iss\":\"https://issuer.example.com\",\"aud\":\"client-id\",\"exp\":"
            + (System.currentTimeMillis() / 1000 + 3600)
            + "}";
    String rawToken =
        Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(header.getBytes(StandardCharsets.UTF_8))
            + "."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8))
            + "."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("sig".getBytes(StandardCharsets.UTF_8));

    // First validation should call the verifier once.
    validator.validate(rawToken, null, 0);
    verify(mockVerifier, times(1)).verify(eq("RS256"), any(), any(), any());

    // Second validation of the SAME token should NOT call the verifier again (cached!).
    validator.validate(rawToken, null, 0);
    verify(mockVerifier, times(1)).verify(eq("RS256"), any(), any(), any());
  }

  @Test
  public void testDPoPPublicJwkNoLeadingSignBytes() throws Exception {
    DefaultDPoPProofCreator creator = new DefaultDPoPProofCreator();

    java.lang.reflect.Method createJwkMethod =
        DefaultDPoPProofCreator.class.getDeclaredMethod("createPublicJwkMap");
    createJwkMethod.setAccessible(true);

    @SuppressWarnings("unchecked")
    Map<String, Object> jwk = (Map<String, Object>) createJwkMethod.invoke(creator);

    assertThat(jwk).containsKey("n");
    assertThat(jwk).containsKey("e");

    String nBase64 = (String) jwk.get("n");
    String eBase64 = (String) jwk.get("e");

    byte[] nBytes = Base64.getUrlDecoder().decode(nBase64);
    byte[] eBytes = Base64.getUrlDecoder().decode(eBase64);

    assertThat(nBytes[0]).isNotEqualTo((byte) 0x00);
    assertThat(eBytes[0]).isNotEqualTo((byte) 0x00);
  }
}
