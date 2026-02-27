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

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.AuthorizationUrlBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import com.github.scribejava.core.revoke.TokenTypeHint;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Exemple complet d'implémentation pour l'API GitHub utilisant OAuth 2.0 et PKCE.
 *
 * <p>Ce programme console guide l'utilisateur à travers le flux d'autorisation standard : 1.
 * Initialisation du service ScribeJava (Config via ENV). 2. Génération automatique du PKCE. 3.
 * Échange du code contre un jeton. 4. Appel d'une ressource protégée. 5. Déconnexion (Logout via
 * Révocation).
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class GitHubExample {

  private static final String CLIENT_ID = config("SCRIBE_CLIENT_ID", "votre_client_id");
  private static final String CLIENT_SECRET = config("SCRIBE_CLIENT_SECRET", "votre_client_secret");

  private GitHubExample() {}

  /**
   * Point d'entrée de l'exemple.
   *
   * @param args Arguments
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   */
  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {

    // 1. Initialisation du service
    final OAuth20Service service =
        new ServiceBuilder(CLIENT_ID)
            .apiSecret(CLIENT_SECRET)
            .callback("http://localhost:8080/callback")
            .build(GitHubApi.instance());

    service.setLogger(verboseLogger());

    System.out.println("=== Flux OAuth 2.0 GitHub (Enterprise Edition) ===");

    // 2. Préparation de l'URL avec PKCE automatique
    final AuthorizationUrlBuilder urlBuilder = service.createAuthorizationUrlBuilder().initPKCE();
    final String authorizationUrl = urlBuilder.build();

    System.out.println("\n1. Connectez-vous via cette URL :");
    System.out.println(authorizationUrl);
    final String code = readInput("2. Collez le code reçu");

    // 3. Échange du code contre un Access Token
    System.out.println("\nÉchange du code...");
    final AuthorizationCodeGrant grant = new AuthorizationCodeGrant(code);
    grant.setPkceCodeVerifier(urlBuilder.getPkce().getCodeVerifier());

    final OAuth2AccessToken accessToken = service.getAccessToken(grant);
    System.out.println("Jeton obtenu : " + accessToken.getAccessToken());

    // 4. Appel d'une ressource protégée
    System.out.println("\nAppel de l'API GitHub (/user)...");
    final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");
    service.signRequest(accessToken, request);

    try (Response response = service.execute(request)) {
      System.out.println("Statut HTTP : " + response.getCode());
      System.out.println("Corps de la réponse :");
      System.out.println(response.getBody());
    }

    // 5. Logout (Révocation)
    System.out.println("\n3. DÉCONNEXION (Logout)...");
    service.revokeToken(accessToken.getAccessToken(), TokenTypeHint.ACCESS_TOKEN);
    System.out.println("✅ Session révoquée proprement.");

    System.out.println("\n=== Fin de l'exemple ===");
  }
}
