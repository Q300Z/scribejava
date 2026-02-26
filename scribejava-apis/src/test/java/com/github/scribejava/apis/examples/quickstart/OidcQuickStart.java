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

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import com.github.scribejava.oidc.IdToken;
import com.github.scribejava.oidc.OidcDiscoveryService;
import com.github.scribejava.oidc.OidcGoogleApi20;
import com.github.scribejava.oidc.OidcService;
import com.github.scribejava.oidc.OidcServiceBuilder;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * [QUICKSTART] OpenID Connect (OIDC) avec Découverte Dynamique.
 *
 * <p>Cet exemple montre l'autonomie totale de ScribeJava (Zéro-Dépendance) : 1. Découverte
 * automatique des URLs (RFC 8414). 2. Échange du code. 3. Validation native de l'ID Token (RSA/EC)
 * via java.security.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class OidcQuickStart {

  private static final String ISSUER = "https://accounts.google.com"; // Ex: Google
  private static final String CLIENT_ID = "votre_client_id";
  private static final String CLIENT_SECRET = "votre_client_secret";

  private OidcQuickStart() {}

  /**
   * Point d'entrée de l'exemple.
   *
   * @param args arguments
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   */
  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {

    System.out.println("=== QuickStart : OpenID Connect (Natif) ===");

    // 1. Découverte dynamique des métadonnées du fournisseur
    System.out.println("Découverte de l'émetteur : " + ISSUER);
    final OidcDiscoveryService discovery = new OidcDiscoveryService(ISSUER, null, null);
    discovery.getProviderMetadata();

    // 2. Configuration du service via le builder spécialisé OIDC
    // baseOnDiscovery() va configurer les endpoints de l'API passée à build()
    final OidcService service =
        (OidcService)
            new OidcServiceBuilder(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback("http://localhost:8080/callback")
                .scopes("openid", "profile", "email")
                .discoverFromIssuer(ISSUER, discovery)
                .build(OidcGoogleApi20.instance());

    // 3. Flux d'autorisation interactif
    final String authUrl = service.getAuthorizationUrl();
    System.out.println("1. Connectez-vous ici : " + authUrl);
    System.out.print("2. Collez le code ici >> ");
    final String code = new Scanner(System.in, "UTF-8").nextLine();

    // 4. Obtention et Validation des jetons
    final OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));

    // Validation native (Signature, Issuer, Audience, Expiration)
    final IdToken idToken = service.validateIdToken(token, null);

    System.out.println("Authentification réussie pour : " + idToken.getClaims().get("email"));
    System.out.println("Nom : " + idToken.getClaims().get("name"));
  }
}
