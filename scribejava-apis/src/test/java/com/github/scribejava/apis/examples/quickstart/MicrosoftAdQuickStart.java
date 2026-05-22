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

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCEService;
import com.github.scribejava.oidc.IdToken;
import com.github.scribejava.oidc.OidcMicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.oidc.OidcService;
import com.github.scribejava.oidc.OidcServiceBuilder;
import com.github.scribejava.oidc.StandardClaims;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * [QUICKSTART] Connexion OIDC avec Microsoft Entra ID (Azure AD).
 *
 * <p>Ce programme démontre : 1. Configuration avec un tenant Microsoft spécifique (commun ou
 * privé). 2. Utilisation de PKCE pour sécuriser la communication. 3. Validation native de l'ID
 * Token Microsoft. 4. Appel de l'API Microsoft Graph (/me) à l'aide du jeton d'accès.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class MicrosoftAdQuickStart {

  private static final String TENANT = config("MICROSOFT_TENANT", "common");
  private static final String CLIENT_ID = config("MICROSOFT_CLIENT_ID", "votre_client_id");
  private static final String CLIENT_SECRET =
      config("MICROSOFT_CLIENT_SECRET", "votre_client_secret");

  private MicrosoftAdQuickStart() {}

  /**
   * Point d'entrée de l'exemple Microsoft AD.
   *
   * @param args arguments
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   */
  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {

    System.out.println("=== QuickStart : Connexion OIDC avec Microsoft Entra ID ===");
    System.out.println("Tenant Microsoft : " + TENANT);

    // 1. Initialisation de l'API Microsoft OIDC avec le tenant configuré
    final OidcMicrosoftAzureActiveDirectory20Api microsoftApi =
        OidcMicrosoftAzureActiveDirectory20Api.custom(TENANT);

    // 2. Initialisation du service OIDC ScribeJava
    final OidcService service =
        (OidcService)
            new OidcServiceBuilder(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback("http://localhost:8080/callback")
                .defaultScope("openid profile email User.Read")
                .build(microsoftApi);

    // Activer les logs de debug
    service.setLogger(verboseLogger());

    // 3. Préparation du PKCE (Proof Key for Code Exchange)
    final PKCE pkce = PKCEService.defaultInstance().generatePKCE();

    // 4. Génération de l'URL d'autorisation
    final String authUrl =
        service.createAuthorizationUrlBuilder().pkce(pkce).state("microsoft_csrf_state").build();

    System.out.println("\n1. Ouvrez cette URL dans votre navigateur :");
    System.out.println(authUrl);

    final String code = readInput("2. Collez le code d'autorisation reçu après redirection");

    // 5. Échange du code contre le jeton
    System.out.println("\nÉchange du code...");
    final AuthorizationCodeGrant grant = new AuthorizationCodeGrant(code);
    grant.setPkceCodeVerifier(pkce.getCodeVerifier());

    final OAuth2AccessToken token = service.getAccessToken(grant);
    System.out.println("Succès ! Jetons obtenus.");

    // 6. Validation native du jeton d'identité (ID Token)
    System.out.println("\nValidation de l'ID Token...");
    final IdToken idToken = service.validateIdToken(token, null);
    System.out.println(
        "ID Token valide ! Utilisateur ID (Sub) : "
            + idToken.getStandardClaims().getSub().orElse("N/A"));

    final StandardClaims claims = new StandardClaims(idToken.getClaims());
    System.out.println("Nom complet  : " + claims.getName().orElse("N/A"));
    System.out.println("Email        : " + claims.getEmail().orElse("N/A"));

    // 7. Appel de l'API Microsoft Graph (/me) avec le jeton d'accès
    System.out.println("\nAppel de Microsoft Graph (/v1.0/me)...");
    final OAuthRequest request = new OAuthRequest(Verb.GET, "https://graph.microsoft.com/v1.0/me");
    service.signRequest(token, request);

    try (Response response = service.execute(request)) {
      System.out.println("\nRéponse de Microsoft Graph :");
      System.out.println(response.getBody());
    }
  }
}
