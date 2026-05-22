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
package com.github.scribejava.apis.examples.quickstart;

import static com.github.scribejava.apis.examples.quickstart.QuickStartUtils.config;
import static com.github.scribejava.apis.examples.quickstart.QuickStartUtils.readInput;
import static com.github.scribejava.apis.examples.quickstart.QuickStartUtils.verboseLogger;

import com.github.scribejava.apis.KeycloakApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCEService;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * [QUICKSTART] Connexion OAuth 2.0 et OIDC avec Keycloak.
 *
 * <p>Ce programme démontre l'intégration de ScribeJava avec Keycloak :
 * 1. Configuration de l'API Keycloak avec l'URL de base et le Realm.
 * 2. Utilisation de PKCE (Proof Key for Code Exchange) pour sécuriser le flux.
 * 3. Appel de l'endpoint standard UserInfo.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class KeycloakQuickStart {

  private static final String KEYCLOAK_URL = config("KEYCLOAK_URL", "http://localhost:8080");
  private static final String REALM = config("KEYCLOAK_REALM", "master");
  private static final String CLIENT_ID = config("KEYCLOAK_CLIENT_ID", "votre_client_id");
  private static final String CLIENT_SECRET = config("KEYCLOAK_CLIENT_SECRET", "votre_client_secret");

  private KeycloakQuickStart() {}

  /**
   * Point d'entrée de l'exemple Keycloak.
   *
   * @param args arguments
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   */
  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {

    System.out.println("=== QuickStart : Connexion avec Keycloak ===");
    System.out.println("Serveur Keycloak : " + KEYCLOAK_URL);
    System.out.println("Royaume (Realm)  : " + REALM);

    // 1. Initialisation de l'API Keycloak dynamique
    final KeycloakApi keycloakApi = KeycloakApi.instance(KEYCLOAK_URL, REALM);

    // 2. Initialisation du service ScribeJava
    final OAuth20Service service =
        new ServiceBuilder(CLIENT_ID)
            .apiSecret(CLIENT_SECRET)
            .callback("http://localhost:8080/callback")
            .defaultScope("openid profile email")
            .build(keycloakApi);

    // Activer les logs de debug
    service.setLogger(verboseLogger());

    // 3. Préparation du PKCE (Proof Key for Code Exchange)
    final PKCE pkce = PKCEService.defaultInstance().generatePKCE();

    // 4. Génération de l'URL d'autorisation
    final String authUrl =
        service
            .createAuthorizationUrlBuilder()
            .pkce(pkce)
            .state("keycloak_csrf_state")
            .build();

    System.out.println("\n1. Ouvrez cette URL dans votre navigateur :");
    System.out.println(authUrl);

    final String code = readInput("2. Collez le code d'autorisation reçu après redirection");

    // 5. Échange du code contre le jeton d'accès
    System.out.println("\nÉchange du code...");
    final AuthorizationCodeGrant grant = new AuthorizationCodeGrant(code);
    grant.setPkceCodeVerifier(pkce.getCodeVerifier());

    final OAuth2AccessToken token = service.getAccessToken(grant);
    System.out.println("Succès ! Token obtenu.");
    System.out.println("Access Token  : " + token.getAccessToken());
    System.out.println("Refresh Token : " + token.getRefreshToken());

    // 6. Appel de l'endpoint UserInfo de Keycloak
    final String userInfoUrl = KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/userinfo";
    System.out.println("\nRécupération des informations utilisateur depuis : " + userInfoUrl);

    final OAuthRequest request = new OAuthRequest(Verb.GET, userInfoUrl);
    service.signRequest(token, request);

    try (Response response = service.execute(request)) {
      System.out.println("\nRéponse de Keycloak (UserInfo) :");
      System.out.println(response.getBody());
    }
  }
}
