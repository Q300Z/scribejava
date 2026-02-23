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

import com.github.scribejava.apis.GitHubApi;
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
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Exemple complet d'implémentation pour l'API GitHub utilisant OAuth 2.0 et PKCE.
 *
 * <p>Ce programme console guide l'utilisateur à travers le flux d'autorisation standard : 1.
 * Initialisation du service ScribeJava. 2. Génération du challenge PKCE (Protection contre
 * l'interception de code). 3. Génération de l'URL d'autorisation GitHub. 4. Échange du code
 * d'autorisation contre un jeton d'accès (Access Token). 5. Appel d'une ressource protégée (/user)
 * pour vérifier l'identité.
 */
@SuppressWarnings("PMD.SystemPrintln")
public class GitHubExample {

  private static final String CLIENT_ID = "votre_client_id";
  private static final String CLIENT_SECRET = "votre_client_secret";

  private GitHubExample() {}

  /**
   * Point d'entrée de l'exemple.
   *
   * @param args Arguments de la ligne de commande (non utilisés).
   * @throws IOException Si une erreur E/S survient.
   * @throws InterruptedException Si l'exécution est interrompue.
   * @throws ExecutionException Si une erreur survient lors de l'exécution asynchrone.
   */
  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    // 1. Initialisation du service
    final OAuth20Service service =
        new ServiceBuilder(CLIENT_ID)
            .apiSecret(CLIENT_SECRET)
            .callback("http://localhost:8080/callback") // Doit correspondre à la config GitHub
            .build(GitHubApi.instance());

    final Scanner in = new Scanner(System.in, "UTF-8");

    System.out.println("=== Flux OAuth 2.0 GitHub avec PKCE ===");

    // 2. Génération du challenge PKCE
    // PKCE est recommandé même pour les clients confidentiels côté serveur.
    final PKCE pkce = PKCEService.defaultInstance().generatePKCE();
    System.out.println("PKCE généré.");

    // 3. Obtention de l'URL d'autorisation
    System.out.println("Récupération de l'URL d'autorisation...");
    final String authorizationUrl = service.createAuthorizationUrlBuilder().pkce(pkce).build();

    System.out.println("1. Ouvrez cette URL dans votre navigateur :");
    System.out.println(authorizationUrl);
    System.out.println(
        "2. Autorisez l'application et récupérez le paramètre 'code' dans l'URL de retour.");
    System.out.print("Collez le code ici >> ");
    final String code = in.nextLine();

    // 4. Échange du code contre un Access Token
    System.out.println("Échange du code contre un Access Token...");
    final AuthorizationCodeGrant grant = new AuthorizationCodeGrant(code);
    grant.setPkceCodeVerifier(pkce.getCodeVerifier());
    final OAuth2AccessToken accessToken = service.getAccessToken(grant);
    System.out.println("Jeton obtenu : " + accessToken.getAccessToken());

    // 5. Appel d'une ressource protégée
    System.out.println("Appel de l'API GitHub (/user)...");
    final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");
    service.signRequest(accessToken, request);

    try (Response response = service.execute(request)) {
      System.out.println("Statut HTTP : " + response.getCode());
      System.out.println("Corps de la réponse :");
      System.out.println(response.getBody());
    }

    System.out.println("=== Fin de l'exemple ===");
  }
}
