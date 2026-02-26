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
import com.github.scribejava.core.oauth.OAuthRetryPolicy;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import com.github.scribejava.oidc.IdToken;
import com.github.scribejava.oidc.OidcDiscoveryService;
import com.github.scribejava.oidc.OidcGoogleApi20;
import com.github.scribejava.oidc.OidcService;
import com.github.scribejava.oidc.OidcServiceBuilder;
import com.github.scribejava.oidc.StandardClaims;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * [QUICKSTART] OpenID Connect (OIDC) - Version Enterprise.
 *
 * <p>Cette version améliorée utilise : 1. La découverte dynamique. 2. Un logging verbeux pour le
 * debug. 3. Une politique de Retry pour la résilience. 4. L'accès typé aux claims utilisateur.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class OidcQuickStart {

  private static final String ISSUER = config("SCRIBE_ISSUER", "https://accounts.google.com");
  private static final String CLIENT_ID = config("SCRIBE_CLIENT_ID", "votre_client_id");
  private static final String CLIENT_SECRET = config("SCRIBE_CLIENT_SECRET", "votre_client_secret");

  private OidcQuickStart() {}

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

    System.out.println("=== QuickStart Enterprise : OpenID Connect ===");

    // 1. Découverte (utilisant le logger verbeux pour voir l'appel .well-known)
    final OidcDiscoveryService discovery = new OidcDiscoveryService(ISSUER, null, null);
    discovery.setLogger(verboseLogger());

    System.out.println("Découverte de l'émetteur : " + ISSUER);
    discovery.getProviderMetadata();

    // 2. Configuration du Service avec Résilience
    final OidcService service =
        (OidcService)
            new OidcServiceBuilder(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback("http://localhost:8080/callback")
                .scopes("openid", "profile", "email")
                .discoverFromIssuer(ISSUER, discovery)
                .build(OidcGoogleApi20.instance());

    // On ajoute le retry et le logger au service principal
    service.setLogger(verboseLogger());
    service.setRetryPolicy(new OAuthRetryPolicy(3, 1000));

    // 3. Flux Interactif
    final String authUrl = service.getAuthorizationUrl();
    System.out.println("\n1. Connectez-vous ici : " + authUrl);
    final String code = readInput("2. Collez le code ici");

    // 4. Échange et Validation
    final OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));
    final IdToken idToken = service.validateIdToken(token, null);

    // 5. Utilisation des StandardClaims pour un accès propre
    final StandardClaims claims = new StandardClaims(idToken.getClaims());

    System.out.println("\n🎉 Authentification réussie !");
    System.out.println("Email  : " + claims.getEmail().orElse("Inconnu"));
    System.out.println("Nom    : " + claims.getName().orElse("Inconnu"));
    System.out.println("Locale : " + claims.getLocale().orElse("Non spécifiée"));
  }
}
