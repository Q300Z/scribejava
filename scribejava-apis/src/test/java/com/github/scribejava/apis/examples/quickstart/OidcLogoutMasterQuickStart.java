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
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import com.github.scribejava.core.revoke.TokenTypeHint;
import com.github.scribejava.oidc.IdTokenValidator;
import com.github.scribejava.oidc.OidcDiscoveryService;
import com.github.scribejava.oidc.OidcGoogleApi20;
import com.github.scribejava.oidc.OidcProviderMetadata;
import com.github.scribejava.oidc.OidcService;
import com.github.scribejava.oidc.OidcServiceBuilder;
import com.github.scribejava.oidc.model.OidcKey;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * [QUICKSTART] Maîtrise de la Déconnexion (Multi-Méthode).
 *
 * <p>Démontre les niveaux de Logout ScribeJava : 1. Révocation Technique (Back-Channel RFC 7009).
 * 2. Validation de Jeton de déconnexion (Back-Channel Logout Notification).
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class OidcLogoutMasterQuickStart {

  private static final String ISSUER = config("SCRIBE_ISSUER", "https://accounts.google.com");
  private static final String CLIENT_ID = config("SCRIBE_CLIENT_ID", "votre_client_id");
  private static final String CLIENT_SECRET = config("SCRIBE_CLIENT_SECRET", "votre_client_secret");

  private OidcLogoutMasterQuickStart() {}

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {

    System.out.println("=== QuickStart : Master Logout OIDC ===");

    // Initialisation
    final OidcDiscoveryService discovery = new OidcDiscoveryService(ISSUER, null, null);
    final OidcProviderMetadata metadata = discovery.getProviderMetadata();
    final OidcService service =
        (OidcService)
            new OidcServiceBuilder(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback("http://localhost:8080/callback")
                .scopes("openid", "profile")
                .discoverFromIssuer(ISSUER, discovery)
                .build(OidcGoogleApi20.instance());

    service.setLogger(verboseLogger());

    // --- PHASE 0 : Connexion pour obtenir un jeton ---
    System.out.println("\n0. CONNEXION...");
    final String authUrl = service.getAuthorizationUrl();
    System.out.println("Lien : " + authUrl);
    final String code = readInput("Code");
    final OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));
    service.validateIdToken(token, null);

    // --- MÉTHODE 1 : Révocation Technique (Back-Channel) ---
    // Invalide le jeton côté serveur IdP. Essentiel pour la sécurité.
    System.out.println("\n1. RÉVOCATION (RFC 7009)...");
    service.revokeToken(token.getAccessToken(), TokenTypeHint.ACCESS_TOKEN);
    System.out.println("✅ Jeton révoqué techniquement.");

    // --- MÉTHODE 2 : Validation de Notification (Webhook) ---
    // Simule la réception d'un 'logout_token' envoyé par le serveur IdP à votre application.
    System.out.println("\n2. VALIDATION JETON DE DÉCONNEXION (Notification IdP)...");
    final String simulatedLogoutToken = "eyJhbGciOiJSUzI1NiJ9.eyJlbnRyeSI6ImxvZ291dCJ9.sig";
    System.out.println("Réception d'un Logout Token (simulé)...");

    // On récupère les clés du fournisseur pour la validation
    final Map<String, OidcKey> keys = discovery.getJwks(metadata.getJwksUri());

    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, metadata.getJwksUri(), keys);
    try {
      validator.validateLogoutToken(simulatedLogoutToken);
      System.out.println("✅ Notification de déconnexion IdP validée. Session locale fermée.");
    } catch (Exception e) {
      System.out.println("❌ Jeton de déconnexion invalide (Attendu car simulé).");
    }

    System.out.println("\n=== Fin de la Master-Class Logout ===");
  }
}
