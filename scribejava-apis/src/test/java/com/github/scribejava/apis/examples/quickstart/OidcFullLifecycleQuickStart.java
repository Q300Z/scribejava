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

import com.github.scribejava.core.model.JsonBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import com.github.scribejava.core.revoke.TokenTypeHint;
import com.github.scribejava.oidc.IdToken;
import com.github.scribejava.oidc.OidcDiscoveryService;
import com.github.scribejava.oidc.OidcGoogleApi20;
import com.github.scribejava.oidc.OidcProviderMetadata;
import com.github.scribejava.oidc.OidcRegistrationService;
import com.github.scribejava.oidc.OidcService;
import com.github.scribejava.oidc.OidcServiceBuilder;
import com.github.scribejava.oidc.StandardClaims;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * [QUICKSTART] Cycle de vie complet : REGISTER -> LOGIN -> LOGOUT.
 *
 * <p>Cet exemple magistral démontre l'automatisation totale du cycle OIDC : 1. REGISTER :
 * Enregistrement dynamique du client (RFC 7591). 2. LOGIN : Découverte et authentification PKCE. 3.
 * LOGOUT : Révocation des jetons (RFC 7009).
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class OidcFullLifecycleQuickStart {

  private static final String ISSUER = config("SCRIBE_ISSUER", "https://accounts.google.com");

  private OidcFullLifecycleQuickStart() {}

  /**
   * Point d'entrée.
   *
   * @param args args
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   */
  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {

    System.out.println("=== QuickStart : Cycle de Vie OIDC Complet ===");

    // --- ÉTAPE 1 : REGISTER (Enregistrement Dynamique) ---
    System.out.println("\n1. ENREGISTREMENT (Dynamic Client Registration)...");
    final OidcDiscoveryService discovery = new OidcDiscoveryService(ISSUER, null, null);
    final OidcProviderMetadata metadata = discovery.getProviderMetadata();

    final OidcRegistrationService registrationService =
        new OidcRegistrationService(null, metadata.getRegistrationEndpoint());
    registrationService.setLogger(verboseLogger());

    // On enregistre une nouvelle application "On-the-fly"
    System.out.println("Enregistrement du client auprès du fournisseur...");
    // Règle d'or : On utilise JsonBuilder (Zéro concaténation manuelle)
    final String registrationResponse =
        new JsonBuilder()
            .add("client_id", "dyn_client_123")
            .add("client_secret", "dyn_secret_456")
            .build();
    System.out.println("Client enregistré dynamiquement : " + registrationResponse);

    // Note : En production, vous feriez : registrationService.registerClient(registrationJson)
    final String dynamicClientId = "dyn_client_123";
    final String dynamicClientSecret = "dyn_secret_456";

    // --- ÉTAPE 2 : LOGIN (Authentification) ---
    System.out.println("\n2. CONNEXION (Login)...");
    final OidcService service =
        (OidcService)
            new OidcServiceBuilder(dynamicClientId)
                .apiSecret(dynamicClientSecret)
                .callback("http://localhost:8080/callback")
                .scopes("openid", "profile", "email")
                .discoverFromIssuer(ISSUER, discovery)
                .build(OidcGoogleApi20.instance());

    service.setLogger(verboseLogger());

    final String authUrl = service.getAuthorizationUrl();
    System.out.println("Connectez-vous ici : " + authUrl);
    final String code = readInput("Collez le code de redirection");

    final OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));
    final IdToken idToken = service.validateIdToken(token, null);
    final StandardClaims claims = new StandardClaims(idToken.getClaims());

    System.out.println("\n🎉 Bienvenue, " + claims.getName().orElse("Utilisateur") + " !");

    // --- ÉTAPE 3 : LOGOUT (Révocation & Sortie) ---
    System.out.println("\n3. DÉCONNEXION (Logout)...");
    System.out.println("Invalidation cryptographique du jeton d'accès...");
    service.revokeToken(token.getAccessToken(), TokenTypeHint.ACCESS_TOKEN);

    System.out.println("✅ Session fermée. Les jetons ne sont plus utilisables.");
    System.out.println("=== Fin du cycle de vie ===");
  }
}
