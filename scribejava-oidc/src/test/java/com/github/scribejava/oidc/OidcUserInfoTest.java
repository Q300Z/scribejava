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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.JsonBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.oidc.model.EcOidcKey;
import com.github.scribejava.oidc.model.OidcKey;
import com.github.scribejava.oidc.model.RsaOidcKey;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests pour la récupération des UserInfo (JSON et JWT signé). */
public class OidcUserInfoTest {

  private static final String CLIENT_ID = "client-123";
  private MockWebServer server;
  private RSAKey rsaKey;
  private ECKey ecKey;

  /**
   * Initialisation du serveur de test et des clés cryptographiques.
   *
   * @throws Exception en cas d'erreur.
   */
  @BeforeEach
  public void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
    rsaKey = new RSAKeyGenerator(2048).keyID("rsa-kid").generate();
    ecKey = new ECKeyGenerator(Curve.P_256).keyID("ec-kid").generate();
  }

  /**
   * Arrêt du serveur de test.
   *
   * @throws IOException en cas d'erreur.
   */
  @AfterEach
  public void tearDown() throws IOException {
    server.shutdown();
  }

  private OidcService createService(String baseUrl, IdTokenValidator validator) {
    final OidcFranceConnectApi20 api = OidcFranceConnectApi20.custom(baseUrl);
    final OidcService service =
        new OidcServiceBuilder(CLIENT_ID)
            .apiSecret("secret")
            .callback("https://app.example.com/cb")
            .defaultScope("openid")
            .httpClient(new JDKHttpClient())
            .build(api);
    service.setIdTokenValidator(validator);
    return service;
  }

  private IdTokenValidator createValidator(String issuer) throws Exception {
    final Map<String, OidcKey> keys = new HashMap<>();
    keys.put("rsa-kid", new RsaOidcKey("rsa-kid", "RS256", rsaKey.toRSAPublicKey()));
    keys.put("ec-kid", new EcOidcKey("ec-kid", "ES256", ecKey.toECPublicKey()));
    return new IdTokenValidator(issuer, CLIENT_ID, "RS256", keys);
  }

  private String signRsaJwt(JWTClaimsSet claimsSet, RSAKey signingKey) throws Exception {
    final SignedJWT jwt =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).build(),
            claimsSet);
    jwt.sign(new RSASSASigner(signingKey));
    return jwt.serialize();
  }

  private String signEcJwt(JWTClaimsSet claimsSet, ECKey signingKey) throws Exception {
    final SignedJWT jwt =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(signingKey.getKeyID()).build(),
            claimsSet);
    jwt.sign(new ECDSASigner(signingKey));
    return jwt.serialize();
  }

  @Test
  public void shouldGetUserInfo() throws IOException {
    final Response response = mock(Response.class);
    when(response.getBody()).thenReturn("{\"sub\":\"123\", \"name\":\"John Doe\"}");
    when(response.getCode()).thenReturn(200);

    final StandardClaims claims = UserInfoJsonExtractor.instance().extract(response);
    assertThat(claims.getSub()).contains("123");
    assertThat(claims.getName()).contains("John Doe");
  }

  @Test
  public void shouldGetUserInfoJsonWithBearerAndFallbackContentType() throws Exception {
    final String baseUrl = server.url("/api/v2").toString();
    final String issuer = baseUrl;
    final String idToken =
        signRsaJwt(
            new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(CLIENT_ID)
                .subject("user-123")
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build(),
            rsaKey);
    final OAuth2AccessToken token =
        new OAuth2AccessToken(
            "access-token-abc",
            "{\"access_token\":\"access-token-abc\",\"id_token\":\"" + idToken + "\"}");

    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(
                "{\"sub\":\"user-123\",\"given_name\":\"Jean\",\"family_name\":\"Dupont\",\"email\":\"jean@example.fr\"}"));

    final OidcService service = createService(baseUrl, null);
    final StandardClaims claims = service.getUserInfo(token);

    assertThat(claims.getSub()).contains("user-123");
    assertThat(claims.getGivenName()).contains("Jean");
    assertThat(claims.getFamilyName()).contains("Dupont");
    assertThat(claims.getEmail()).contains("jean@example.fr");

    final RecordedRequest req = server.takeRequest();
    assertThat(req.getMethod()).isEqualTo("GET");
    assertThat(req.getPath()).isEqualTo("/api/v2/userinfo");
    assertThat(req.getHeader("Authorization")).isEqualTo("Bearer access-token-abc");

    // Repli JSON lorsque Content-Type n'est pas application/json mais que le corps commence par {
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "text/plain")
            .setBody("{\"sub\":\"user-123\",\"name\":\"Jean Dupont\"}"));
    final StandardClaims asyncClaims = service.getUserInfoAsync(token).get();
    assertThat(asyncClaims.getSub()).contains("user-123");
    assertThat(asyncClaims.getName()).contains("Jean Dupont");
  }

  @Test
  public void shouldGetUserInfoJwtRs256WithoutExpAndEs256WithExp() throws Exception {
    final String baseUrl = server.url("/api/v2").toString();
    final OidcService service = createService(baseUrl, createValidator(baseUrl));
    final OAuth2AccessToken token = new OAuth2AccessToken("access-token-jwt");

    // 1. RS256 sans exp (conforme OIDC Core §5.3.2) avec Content-Type: application/jwt
    final String rsJwt =
        signRsaJwt(
            new JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .audience(CLIENT_ID)
                .subject("sub-rs256")
                .claim("name", "Alice RSA")
                .build(),
            rsaKey);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("content-type", "application/jwt")
            .setBody(rsJwt));

    final StandardClaims rsClaims = service.getUserInfo(token);
    assertThat(rsClaims.getSub()).contains("sub-rs256");
    assertThat(rsClaims.getName()).contains("Alice RSA");

    // 2. ES256 avec exp valide et repli JWS à 3 segments sans Content-Type application/jwt
    final String esJwt =
        signEcJwt(
            new JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .audience(CLIENT_ID)
                .subject("sub-es256")
                .claim("email", "bob@example.fr")
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build(),
            ecKey);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "text/plain")
            .setBody(esJwt));

    final StandardClaims esClaims = service.getUserInfoAsync(token).get();
    assertThat(esClaims.getSub()).contains("sub-es256");
    assertThat(esClaims.getEmail()).contains("bob@example.fr");
  }

  @Test
  public void shouldVerifyUserInfoJwtViaJwksFallbackWhenValidatorIsNull() throws Exception {
    final String baseUrl = server.url("/api/v2").toString();
    final OidcProConnectApi20 api = OidcProConnectApi20.custom(baseUrl);
    final String metadataJson =
        new JsonBuilder()
            .add("issuer", baseUrl)
            .add("authorization_endpoint", baseUrl + "/authorize")
            .add("token_endpoint", baseUrl + "/token")
            .add("userinfo_endpoint", baseUrl + "/userinfo")
            .add("jwks_uri", baseUrl + "/jwks")
            .add("response_types_supported", Collections.singletonList("code"))
            .add("subject_types_supported", Collections.singletonList("public"))
            .add("id_token_signing_alg_values_supported", Collections.singletonList("ES256"))
            .build();
    api.setMetadata(OidcProviderMetadata.parse(metadataJson));

    final OidcService service =
        new OidcServiceBuilder(CLIENT_ID)
            .apiSecret("secret")
            .callback("https://app.example.com/cb")
            .httpClient(new JDKHttpClient())
            .build(api);

    final String esJwt =
        signEcJwt(
            new JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .audience(CLIENT_ID)
                .subject("sub-jwks-fallback")
                .claim("given_name", "Claire")
                .build(),
            ecKey);
    final String jwksJson =
        new JWKSet(Arrays.asList(rsaKey.toPublicJWK(), ecKey.toPublicJWK())).toString();

    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(esJwt));
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(jwksJson));

    final StandardClaims claims = service.getUserInfo(new OAuth2AccessToken("token-jwks"));
    assertThat(claims.getSub()).contains("sub-jwks-fallback");
    assertThat(claims.getGivenName()).contains("Claire");
  }

  @Test
  public void shouldRejectInvalidUserInfoJwtAndHttpErrors() throws Exception {
    final String baseUrl = server.url("/api/v2").toString();
    final OidcService service = createService(baseUrl, createValidator(baseUrl));
    final OAuth2AccessToken token = new OAuth2AccessToken("access-token");

    // 1. Signature invalide (signé avec une autre clé RSA)
    final RSAKey wrongKey = new RSAKeyGenerator(2048).keyID("rsa-kid").generate();
    final String badSigJwt =
        signRsaJwt(
            new JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .audience(CLIENT_ID)
                .subject("user-1")
                .build(),
            wrongKey);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(badSigJwt));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 2. Issuer invalide
    final String badIssJwt =
        signRsaJwt(
            new JWTClaimsSet.Builder()
                .issuer("https://evil.example.com")
                .audience(CLIENT_ID)
                .subject("user-1")
                .build(),
            rsaKey);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(badIssJwt));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 3. Audience invalide
    final String badAudJwt =
        signRsaJwt(
            new JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .audience("other-client")
                .subject("user-1")
                .build(),
            rsaKey);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(badAudJwt));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 4. Subject manquant dans le JWT
    final String missingSubJwt =
        signRsaJwt(new JWTClaimsSet.Builder().issuer(baseUrl).audience(CLIENT_ID).build(), rsaKey);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(missingSubJwt));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 5. exp expiré (lorsqu'il est présent)
    final String expiredJwt =
        signRsaJwt(
            new JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .audience(CLIENT_ID)
                .subject("user-1")
                .expirationTime(new Date(System.currentTimeMillis() - 60_000))
                .build(),
            rsaKey);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(expiredJwt));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 6. Erreur HTTP 401
    server.enqueue(new MockResponse().setResponseCode(401).setBody("Unauthorized"));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 7. Incohérence de sub avec l'ID Token présent dans le jeton
    final String idToken =
        signRsaJwt(
            new JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .audience(CLIENT_ID)
                .subject("sub-id-token")
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build(),
            rsaKey);
    final OAuth2AccessToken tokenWithId =
        new OAuth2AccessToken(
            "access-token", "{\"access_token\":\"access-token\",\"id_token\":\"" + idToken + "\"}");
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("{\"sub\":\"different-sub\"}"));
    final ExecutionException subEx =
        assertThrows(ExecutionException.class, () -> service.getUserInfoAsync(tokenWithId).get());
    assertThat(subEx.getCause()).isInstanceOf(OAuthException.class);
  }

  @Test
  public void shouldRejectMalformedOrEmptyJsonAndMissingOrNonStringSub() throws Exception {
    final String baseUrl = server.url("/api/v2").toString();
    final OidcService service = createService(baseUrl, null);
    final OAuth2AccessToken token = new OAuth2AccessToken("access-token");

    // 1. JSON malformé {invalid
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("{invalid"));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 2. JSON vide {}
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("{}"));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 3. JSON partiellement malformé avec virgule/junk
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("{\"sub\":\"u1\", invalid}"));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("{\"sub\":\"u1\" invalid}"));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 4. JSON valide mais sans sub (OIDC Core §5.3.2)
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("{\"name\":\"Alice\"}"));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 5. JSON avec sub non-String (entier) — doit lever OAuthException et non ClassCastException
    final String idToken =
        signRsaJwt(
            new JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .audience(CLIENT_ID)
                .subject("12345")
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build(),
            rsaKey);
    final OAuth2AccessToken tokenWithId =
        new OAuth2AccessToken(
            "access-token", "{\"access_token\":\"access-token\",\"id_token\":\"" + idToken + "\"}");
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("{\"sub\":12345}"));
    assertThrows(OAuthException.class, () -> service.getUserInfo(tokenWithId));
  }

  @Test
  public void shouldRejectJwksFallbackWithoutIssuerAndInvalidMultiAudAzp() throws Exception {
    final String baseUrl = server.url("/api/v2").toString();
    final DefaultOidcApi20 noIssuerApi =
        new DefaultOidcApi20() {
          @Override
          public String getUserinfoEndpoint() {
            return baseUrl + "/userinfo";
          }

          @Override
          public String getJwksUri() {
            return baseUrl + "/jwks";
          }
        };
    final OidcService service =
        new OidcServiceBuilder(CLIENT_ID)
            .apiSecret("secret")
            .callback("https://app.example.com/cb")
            .httpClient(new JDKHttpClient())
            .build(noIssuerApi);

    final String jwksJson =
        new JWKSet(Arrays.asList(rsaKey.toPublicJWK(), ecKey.toPublicJWK())).toString();
    final String signedJwt =
        signRsaJwt(
            new JWTClaimsSet.Builder()
                .issuer("https://evil-issuer.example")
                .audience(Arrays.asList(CLIENT_ID, "other-client"))
                .subject("sub-evil")
                .build(),
            rsaKey);

    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(signedJwt));
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(jwksJson));
    assertThrows(OAuthException.class, () -> service.getUserInfo(new OAuth2AccessToken("tok")));

    // Multi-audience avec azp ne correspondant pas au client_id via IdTokenValidator
    final IdTokenValidator validator = createValidator(baseUrl);
    final String badAzpJwt =
        signRsaJwt(
            new JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .audience(Arrays.asList(CLIENT_ID, "other-client"))
                .claim("azp", "wrong-azp")
                .subject("sub-1")
                .build(),
            rsaKey);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(badAzpJwt));
    final OidcService fcService = createService(baseUrl, validator);
    assertThrows(OAuthException.class, () -> fcService.getUserInfo(new OAuth2AccessToken("tok")));
  }

  @Test
  public void shouldThrowOAuthExceptionInsteadOfClassCastExceptionOnNonStringJwtFields()
      throws Exception {
    final String baseUrl = server.url("/api/v2").toString();
    final OidcService service = createService(baseUrl, createValidator(baseUrl));
    final OAuth2AccessToken token = new OAuth2AccessToken("access-token");

    // 1. En-tête JWT avec alg numérique {"alg":123,"kid":"rsa-kid"}
    final String badAlgHeader =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":123,\"kid\":\"rsa-kid\"}".getBytes(StandardCharsets.UTF_8));
    final String validPayload =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                ("{\"iss\":\"" + baseUrl + "\",\"aud\":\"" + CLIENT_ID + "\",\"sub\":\"u1\"}")
                    .getBytes(StandardCharsets.UTF_8));
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(badAlgHeader + "." + validPayload + ".sig"));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 2. Payload JWT signé avec iss numérique {"iss":123,...}
    final String signedNumericIss =
        signRsaJwt(
            new JWTClaimsSet.Builder().claim("iss", 123L).audience(CLIENT_ID).subject("u1").build(),
            rsaKey);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(signedNumericIss));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));

    // 3. En-tête JWT avec kid numérique {"alg":"RS256","kid":123} sur IdTokenValidator
    final String badKidHeader =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":\"RS256\",\"kid\":123}".getBytes(StandardCharsets.UTF_8));
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(badKidHeader + "." + validPayload + ".sig"));
    assertThrows(OAuthException.class, () -> service.getUserInfo(token));
  }
}
