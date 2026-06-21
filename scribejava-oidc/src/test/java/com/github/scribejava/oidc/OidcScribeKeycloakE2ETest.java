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

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth2.grant.PasswordGrant;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCEService;
import com.github.scribejava.oidc.model.OidcKey;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/** Test E2E de l'intégration OIDC ScribeJava avec une instance locale Keycloak. */
public class OidcScribeKeycloakE2ETest {

  private static final String ISSUER = "http://localhost:8081/realms/scribejava-test";

  private static class KeycloakTestApi extends DefaultOidcApi20 {
    @Override
    public String getAuthorizationBaseUrl() {
      final String url = super.getAuthorizationBaseUrl();
      return url != null
          ? url
          : "http://localhost:8081/realms/scribejava-test/protocol/openid-connect/auth";
    }
  }

  private boolean isKeycloakReachable() {
    try {
      final URL url = new URL(ISSUER + "/.well-known/openid-configuration");
      final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(1000);
      conn.setReadTimeout(1000);
      conn.setRequestMethod("GET");
      return conn.getResponseCode() == 200;
    } catch (Exception e) {
      return false;
    }
  }

  @Test
  public void testKeycloakOidcFlowE2E() throws Exception {
    Assumptions.assumeTrue(isKeycloakReachable(), "Keycloak is not running at " + ISSUER);

    // 1. Découverte OIDC
    final OidcDiscoveryService discoveryService =
        new OidcDiscoveryService(ISSUER, null, "ScribeJava-E2E-Agent");
    final OidcProviderMetadata metadata = discoveryService.getProviderMetadata();
    assertThat(metadata).isNotNull();
    assertThat(metadata.getIssuer()).isEqualTo(ISSUER);

    // 2. Récupération des clés JWKS
    final Map<String, OidcKey> keys = discoveryService.getJwks(metadata.getJwksUri());
    assertThat(keys).isNotEmpty();

    // 3. Initialisation du service OIDC ScribeJava
    final KeycloakTestApi api = new KeycloakTestApi();
    api.setMetadata(metadata);

    final OidcService service =
        new OidcServiceBuilder("test-client")
            .apiSecret("test-secret")
            .defaultScope("openid profile email")
            .build(api);

    // 4. Configuration du Validateur d'ID Token
    final IdTokenValidator validator =
        new IdTokenValidator(
            metadata.getIssuer(),
            "test-client",
            "RS256",
            keys,
            discoveryService,
            metadata.getJwksUri());
    service.setIdTokenValidator(validator);

    // 5. Exécution du flux Direct Access Grant (Password Grant)
    final PasswordGrant grant = new PasswordGrant("test-user", "test-password");
    final OAuth2AccessToken token = service.getAccessToken(grant);

    // 6. Assertions & Validation
    assertThat(token).isNotNull();
    assertThat(token.getAccessToken()).isNotEmpty();

    final IdToken idToken = service.validateIdToken(token, null);
    assertThat(idToken).isNotNull();
    assertThat(idToken.getClaims()).containsKey("sub");
    assertThat(idToken.getClaims()).containsEntry("preferred_username", "test-user");

    // 7. Renouvellement de token (Refresh token flow)
    assertThat(token.getRefreshToken()).isNotEmpty();
    final OAuth2AccessToken renewedToken = service.refreshAccessToken(token.getRefreshToken());
    assertThat(renewedToken).isNotNull();
    assertThat(renewedToken.getAccessToken()).isNotEmpty();
    final IdToken renewedIdToken = service.validateIdToken(renewedToken, null);
    if (renewedIdToken != null) {
      assertThat(renewedIdToken.getClaims()).containsKey("sub");
    }

    // 8. Deconnexion (RP-initiated logout)
    final String logoutUrl =
        service.getLogoutUrl(
            idToken.getRawResponse(),
            "http://localhost:8080/callback",
            "some-state",
            "test-client");
    assertThat(logoutUrl).isNotNull();
    assertThat(logoutUrl).contains("id_token_hint=");
    assertThat(logoutUrl).contains("post_logout_redirect_uri=");
    assertThat(logoutUrl).contains("state=");
    assertThat(logoutUrl).contains("client_id=");

    final HttpURLConnection conn = (HttpURLConnection) new URL(logoutUrl).openConnection();
    conn.setInstanceFollowRedirects(false);
    conn.setRequestMethod("GET");
    final int responseCode = conn.getResponseCode();
    assertThat(responseCode).isIn(200, 302);
    if (responseCode == 302) {
      final String location = conn.getHeaderField("Location");
      assertThat(location).contains("http://localhost:8080/callback");
      assertThat(location).contains("state=some-state");
    }
  }

  /**
   * Test E2E du flux OIDC pour un client public mobile.
   *
   * @throws Exception en cas d'erreur
   */
  @Test
  public void testKeycloakOidcMobilePublicClientFlowE2E() throws Exception {
    Assumptions.assumeTrue(isKeycloakReachable(), "Keycloak is not running at " + ISSUER);

    // 1. Découverte OIDC
    final OidcDiscoveryService discoveryService =
        new OidcDiscoveryService(ISSUER, null, "ScribeJava-E2E-Agent");
    final OidcProviderMetadata metadata = discoveryService.getProviderMetadata();
    final Map<String, OidcKey> keys = discoveryService.getJwks(metadata.getJwksUri());

    // 2. Initialisation du service OIDC pour le client public mobile
    final KeycloakTestApi api = new KeycloakTestApi();
    api.setMetadata(metadata);

    final OidcService mobileService =
        new OidcServiceBuilder("test-mobile-client")
            .defaultScope("openid profile email")
            .build(api);

    final IdTokenValidator mobileValidator =
        new IdTokenValidator(
            metadata.getIssuer(),
            "test-mobile-client",
            "RS256",
            keys,
            discoveryService,
            metadata.getJwksUri());
    mobileService.setIdTokenValidator(mobileValidator);

    // 3. Exécution du flux Password Grant pour client public mobile
    final PasswordGrant grant = new PasswordGrant("test-user", "test-password");
    final OAuth2AccessToken token = mobileService.getAccessToken(grant);

    assertThat(token).isNotNull();
    assertThat(token.getAccessToken()).isNotEmpty();

    // Valider le ID Token
    final IdToken idToken = mobileService.validateIdToken(token, null);
    assertThat(idToken).isNotNull();
    assertThat(idToken.getClaims()).containsEntry("preferred_username", "test-user");

    // 4. Renouvellement
    assertThat(token.getRefreshToken()).isNotEmpty();
    final OAuth2AccessToken renewedToken =
        mobileService.refreshAccessToken(token.getRefreshToken());
    assertThat(renewedToken).isNotNull();
    assertThat(renewedToken.getAccessToken()).isNotEmpty();
  }

  /**
   * Test unitaire de la présence et de la validité des paramètres PKCE dans l'URL d'autorisation.
   */
  @Test
  public void testPkceParametersInAuthorizationUrl() {
    final KeycloakTestApi api = new KeycloakTestApi();
    final OidcService service =
        new OidcServiceBuilder("test-client")
            .apiSecret("test-secret")
            .callback("http://localhost:8080/callback")
            .build(api);

    final PKCE pkce = PKCEService.defaultInstance().generatePKCE();
    final String authUrl = service.createAuthorizationUrlBuilder().pkce(pkce).build();

    assertThat(authUrl).contains("code_challenge=" + pkce.getCodeChallenge());
    assertThat(authUrl).contains("code_challenge_method=" + pkce.getCodeChallengeMethod().name());
  }
}
