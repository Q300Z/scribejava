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

import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth2.grant.PasswordGrant;
import com.github.scribejava.core.utils.JsonUtils;
import com.github.scribejava.oidc.model.OidcKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Tests E2E contre une instance Keycloak réelle simulant les profils Azure Connect (RS256 +
 * UserInfo JSON), FranceConnect (ES256 + UserInfo JWT signé) et FranceConnect+ (ES256 + JWE
 * RSA-OAEP-256/A256GCM).
 */
public class OidcScribeKeycloakProfilesE2ETest {

  private static final String BASE_URL = "http://localhost:8081";
  private static final String REALM = "scribejava-test";
  private static final String ISSUER = BASE_URL + "/realms/" + REALM;

  private static class KeycloakProfileApi extends DefaultOidcApi20 {
    @Override
    public String getAuthorizationBaseUrl() {
      final String url = super.getAuthorizationBaseUrl();
      return url != null ? url : ISSUER + "/protocol/openid-connect/auth";
    }
  }

  private boolean isKeycloakReachable() {
    try {
      final HttpURLConnection conn =
          (HttpURLConnection)
              new URL(ISSUER + "/.well-known/openid-configuration").openConnection();
      conn.setConnectTimeout(1000);
      conn.setReadTimeout(1000);
      conn.setRequestMethod("GET");
      return conn.getResponseCode() == 200;
    } catch (Exception e) {
      return false;
    }
  }

  @Test
  public void shouldAuthenticateAgainstKeycloakWithAzureFcAndFcPlusProfiles() throws Exception {
    Assumptions.assumeTrue(isKeycloakReachable(), "Keycloak is not running at " + ISSUER);

    final String adminToken = fetchAdminToken();
    ensureEcKeyProvider(adminToken);
    enrichTestUser(adminToken);

    final RSAKey rpEncKey =
        new RSAKeyGenerator(2048)
            .keyID("rp-enc-kid")
            .keyUse(KeyUse.ENCRYPTION)
            .algorithm(com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256)
            .generate();
    final String rpJwksJson = new JWKSet(rpEncKey.toPublicJWK()).toString();

    final MockWebServer rpJwksServer = new MockWebServer();
    rpJwksServer.setDispatcher(
        new Dispatcher() {
          @Override
          public MockResponse dispatch(RecordedRequest request) {
            return new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(rpJwksJson);
          }
        });
    rpJwksServer.start();
    try {
      final String rpJwksUrl = rpJwksServer.url("/jwks").toString();

      createOrUpdateClient(
          adminToken,
          "e2e-azure-client",
          "azure-secret",
          "{\"id.token.signed.response.alg\":\"RS256\"}");
      createOrUpdateClient(
          adminToken,
          "e2e-fc-client",
          "fc-secret",
          "{\"id.token.signed.response.alg\":\"ES256\","
              + "\"userinfo.signed.response.alg\":\"ES256\"}");
      createOrUpdateClient(
          adminToken,
          "e2e-fc-plus-client",
          "fc-plus-secret",
          "{\"id.token.signed.response.alg\":\"ES256\","
              + "\"userinfo.signed.response.alg\":\"ES256\","
              + "\"id.token.encrypted.response.alg\":\"RSA-OAEP-256\","
              + "\"id.token.encrypted.response.enc\":\"A256GCM\","
              + "\"use.jwks.url\":\"true\","
              + "\"jwks.url\":"
              + jsonQuote(rpJwksUrl)
              + "}");

      final OidcDiscoveryService discoveryService =
          new OidcDiscoveryService(ISSUER, null, "ScribeJava-E2E-Profiles");
      final OidcProviderMetadata metadata = discoveryService.getProviderMetadata();
      final Map<String, OidcKey> jwks = discoveryService.getJwks(metadata.getJwksUri());

      // 1. Profil Azure Connect : RS256 + /userinfo JSON
      verifyProfileFlow(
          metadata, jwks, discoveryService, "e2e-azure-client", "azure-secret", "RS256", null);

      // 2. Profil FranceConnect : ES256 + /userinfo JWT signé (application/jwt)
      verifyProfileFlow(
          metadata, jwks, discoveryService, "e2e-fc-client", "fc-secret", "ES256", null);

      // 3. Profil FranceConnect+ : ES256 + id_token chiffré JWE (RSA-OAEP-256 / A256GCM)
      verifyProfileFlow(
          metadata,
          jwks,
          discoveryService,
          "e2e-fc-plus-client",
          "fc-plus-secret",
          "ES256",
          rpEncKey);
    } finally {
      rpJwksServer.shutdown();
    }
  }

  private void verifyProfileFlow(
      OidcProviderMetadata metadata,
      Map<String, OidcKey> jwks,
      OidcDiscoveryService discoveryService,
      String clientId,
      String clientSecret,
      String expectedAlg,
      RSAKey decryptionKey)
      throws Exception {
    final KeycloakProfileApi api = new KeycloakProfileApi();
    api.setMetadata(metadata);

    final OidcService service =
        new OidcServiceBuilder(clientId)
            .apiSecret(clientSecret)
            .defaultScope("openid profile email")
            .httpClient(new JDKHttpClient())
            .build(api);

    final IdTokenValidator validator =
        new IdTokenValidator(
            metadata.getIssuer(),
            clientId,
            expectedAlg,
            jwks,
            discoveryService,
            metadata.getJwksUri());
    service.setIdTokenValidator(validator);
    if (decryptionKey != null) {
      service.setDecryptionKey(decryptionKey.toPrivateKey());
    }

    final OAuth2AccessToken token =
        service.getAccessToken(new PasswordGrant("test-user", "test-password"));
    assertThat(token).isNotNull();
    assertThat(token.getAccessToken()).isNotEmpty();

    if (decryptionKey != null) {
      final Map<String, Object> rawMap = JsonUtils.parse(token.getRawResponse());
      final String rawIdToken = (String) rawMap.get("id_token");
      assertThat(rawIdToken.split("\\.", -1)).hasSize(5);
    }

    final IdToken idToken = service.validateIdToken(token, null);
    assertThat(idToken).isNotNull();
    assertThat(idToken.getStandardClaims().getSub()).isPresent();

    final StandardClaims userInfo = service.getUserInfo(token);
    assertThat(userInfo.getSub()).isEqualTo(idToken.getStandardClaims().getSub());
    assertThat(userInfo.getEmail()).contains("test-user@example.com");
    assertThat(userInfo.getGivenName()).contains("Jean");
    assertThat(userInfo.getFamilyName()).contains("Dupont");
  }

  private String fetchAdminToken() throws Exception {
    final String body =
        "grant_type=password&client_id=admin-cli&username="
            + URLEncoder.encode("admin", "UTF-8")
            + "&password="
            + URLEncoder.encode("admin", "UTF-8");
    final HttpURLConnection conn =
        (HttpURLConnection)
            new URL(BASE_URL + "/realms/master/protocol/openid-connect/token").openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    try (OutputStream os = conn.getOutputStream()) {
      os.write(body.getBytes(StandardCharsets.UTF_8));
    }
    final String json = readBody(conn);
    assertThat(conn.getResponseCode()).isEqualTo(200);
    return (String) JsonUtils.parse(json).get("access_token");
  }

  private void ensureEcKeyProvider(String adminToken) throws Exception {
    final String componentsJson =
        sendAdminRequest(
            "GET", BASE_URL + "/admin/realms/" + REALM + "/components", adminToken, null);
    if (componentsJson.contains("ecdsa-generated")) {
      return;
    }
    final String realmJson =
        sendAdminRequest("GET", BASE_URL + "/admin/realms/" + REALM, adminToken, null);
    final String realmId = (String) JsonUtils.parse(realmJson).get("id");
    final String payload =
        "{\"name\":\"ecdsa-p256\","
            + "\"providerId\":\"ecdsa-generated\","
            + "\"providerType\":\"org.keycloak.keys.KeyProvider\","
            + "\"parentId\":"
            + jsonQuote(realmId)
            + ","
            + "\"config\":{\"ecdsaEllipticCurveKey\":[\"P-256\"],"
            + "\"active\":[\"true\"],\"enabled\":[\"true\"],\"priority\":[\"100\"]}}";
    sendAdminRequest(
        "POST", BASE_URL + "/admin/realms/" + REALM + "/components", adminToken, payload);
  }

  private void enrichTestUser(String adminToken) throws Exception {
    final String usersJson =
        sendAdminRequest(
            "GET",
            BASE_URL + "/admin/realms/" + REALM + "/users?username=test-user",
            adminToken,
            null);
    final java.util.List<?> users =
        (java.util.List<?>) JsonUtils.parse("{\"u\":" + usersJson + "}").get("u");
    final Map<?, ?> user = (Map<?, ?>) users.get(0);
    final String userId = (String) user.get("id");
    final String payload =
        "{\"email\":\"test-user@example.com\","
            + "\"emailVerified\":true,"
            + "\"firstName\":\"Jean\","
            + "\"lastName\":\"Dupont\"}";
    sendAdminRequest(
        "PUT", BASE_URL + "/admin/realms/" + REALM + "/users/" + userId, adminToken, payload);
  }

  private void createOrUpdateClient(
      String adminToken, String clientId, String secret, String attributesJson) throws Exception {
    final String existingJson =
        sendAdminRequest(
            "GET",
            BASE_URL + "/admin/realms/" + REALM + "/clients?clientId=" + clientId,
            adminToken,
            null);
    final java.util.List<?> clients =
        (java.util.List<?>) JsonUtils.parse("{\"c\":" + existingJson + "}").get("c");
    final String payload =
        "{\"clientId\":"
            + jsonQuote(clientId)
            + ",\"secret\":"
            + jsonQuote(secret)
            + ",\"enabled\":true"
            + ",\"publicClient\":false"
            + ",\"directAccessGrantsEnabled\":true"
            + ",\"attributes\":"
            + attributesJson
            + "}";
    if (clients.isEmpty()) {
      sendAdminRequest(
          "POST", BASE_URL + "/admin/realms/" + REALM + "/clients", adminToken, payload);
    } else {
      final String id = (String) ((Map<?, ?>) clients.get(0)).get("id");
      sendAdminRequest(
          "PUT", BASE_URL + "/admin/realms/" + REALM + "/clients/" + id, adminToken, payload);
    }
  }

  private String sendAdminRequest(String method, String urlStr, String token, String jsonBody)
      throws Exception {
    final HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
    conn.setRequestMethod(method);
    conn.setRequestProperty("Authorization", "Bearer " + token);
    if (jsonBody != null) {
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/json");
      try (OutputStream os = conn.getOutputStream()) {
        os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
      }
    }
    final int status = conn.getResponseCode();
    final String resp = readBody(conn);
    assertThat(status)
        .withFailMessage("Keycloak Admin %s %s failed (%d): %s", method, urlStr, status, resp)
        .isBetween(200, 299);
    return resp;
  }

  private String readBody(HttpURLConnection conn) throws Exception {
    final InputStream is =
        conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
    if (is == null) {
      return "";
    }
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining("\n"));
    }
  }

  private String jsonQuote(String value) {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }
}
