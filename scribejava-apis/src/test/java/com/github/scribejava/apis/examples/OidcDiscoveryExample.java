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
package com.github.scribejava.apis.examples;

import static com.github.scribejava.apis.examples.quickstart.QuickStartUtils.config;
import static com.github.scribejava.apis.examples.quickstart.QuickStartUtils.readInput;
import static com.github.scribejava.apis.examples.quickstart.QuickStartUtils.verboseLogger;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import com.github.scribejava.core.revoke.TokenTypeHint;
import com.github.scribejava.oidc.IdToken;
import com.github.scribejava.oidc.OidcDiscoveryService;
import com.github.scribejava.oidc.OidcGoogleApi20;
import com.github.scribejava.oidc.OidcService;
import com.github.scribejava.oidc.OidcServiceBuilder;
import com.github.scribejava.oidc.StandardClaims;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Exemple complet OIDC avec découverte dynamique (Google).
 *
 * <p>Ce programme démontre : 1. Découverte automatique des endpoints. 2. Initialisation fluide du
 * service OIDC. 3. Validation native de l'ID Token. 4. Accès typé aux UserInfo. 5. Logout
 * (Révocation).
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class OidcDiscoveryExample {

  private static final String GOOGLE_ISSUER =
      config("SCRIBE_ISSUER", "https://accounts.google.com");
  private static final String CLIENT_ID = config("SCRIBE_CLIENT_ID", "votre_client_id");
  private static final String CLIENT_SECRET = config("SCRIBE_CLIENT_SECRET", "votre_client_secret");

  private OidcDiscoveryExample() {}

  /**
   * Point d'entrée.
   *
   * @param args Arguments
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   */
  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {

    System.out.println("=== Découverte Dynamique OIDC (Enterprise Edition) ===");

    // 1. Découverte
    System.out.println("\nRécupération de la configuration depuis : " + GOOGLE_ISSUER);
    final OidcDiscoveryService discovery = new OidcDiscoveryService(GOOGLE_ISSUER, null, null);
    discovery.setLogger(verboseLogger());
    discovery.getProviderMetadata();

    // 2. Initialisation du service via le builder spécialisé
    final OidcService service =
        (OidcService)
            new OidcServiceBuilder(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .defaultScope("openid profile email")
                .callback("http://localhost:8080/callback")
                .discoverFromIssuer(GOOGLE_ISSUER, discovery)
                .build(OidcGoogleApi20.instance());

    service.setLogger(verboseLogger());

    // 3. Flux d'autorisation
    final String authorizationUrl = service.getAuthorizationUrl();
    System.out.println("\n1. Ouvrez cette URL :");
    System.out.println(authorizationUrl);
    final String code = readInput("2. Collez le code reçu ici");

    // 4. Échange et Validation
    System.out.println("\nÉchange du code...");
    final OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));

    System.out.println("Validation de l'ID Token...");
    final IdToken idToken = service.validateIdToken(token, null);
    System.out.println("Utilisateur authentifié (Sub) : " + idToken.getClaim("sub"));

    // 5. Récupération des UserInfo
    System.out.println("\nRécupération des UserInfo complètes...");
    final StandardClaims claims = service.getUserInfoAsync(token).get();
    System.out.println("Nom   : " + claims.getName().orElse("N/A"));
    System.out.println("Email : " + claims.getEmail().orElse("N/A"));

    // 6. Logout (Révocation)
    System.out.println("\n3. DÉCONNEXION (Logout)...");
    service.revokeToken(token.getAccessToken(), TokenTypeHint.ACCESS_TOKEN);
    System.out.println("✅ Session fermée.");

    System.out.println("\n=== Fin de l'exemple ===");
  }
}
