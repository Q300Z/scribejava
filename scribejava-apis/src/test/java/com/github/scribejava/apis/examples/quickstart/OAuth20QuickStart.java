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
 * [QUICKSTART] Flux OAuth 2.0 Standard avec PKCE.
 * 
 * Cet exemple montre comment utiliser ScribeJava pour une connexion interactive 
 * en ligne de commande. Il utilise le protocole PKCE (RFC 7636) qui est la norme 
 * de sécurité actuelle pour TOUS les clients (Publics et Confidentiels).
 */
@SuppressWarnings("PMD.SystemPrintln")
public class OAuth20QuickStart {

    private static final String CLIENT_ID = "votre_client_id";
    private static final String CLIENT_SECRET = "votre_client_secret";

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        
        // 1. Initialisation du service ScribeJava
        final OAuth20Service service = new ServiceBuilder(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback("http://localhost:8080/callback")
                .defaultScope("read:user")
                .build(GitHubApi.instance());

        System.out.println("=== QuickStart : OAuth 2.0 + PKCE ===");

        // 2. Préparation du PKCE (Proof Key for Code Exchange)
        final PKCE pkce = PKCEService.defaultInstance().generatePKCE();
        
        // 3. Génération de l'URL d'autorisation
        final String authUrl = service.createAuthorizationUrlBuilder()
                .pkce(pkce)
                .state("secret_random_state") // Anti-CSRF
                .build();

        System.out.println("1. Connectez-vous via cette URL :");
        System.out.println(authUrl);
        
        System.out.print("2. Collez le code reçu après redirection >> ");
        final String code = new Scanner(System.in, "UTF-8").nextLine();

        // 4. Échange du code contre un jeton d'accès
        System.out.println("Échange du code...");
        final AuthorizationCodeGrant grant = new AuthorizationCodeGrant(code);
        grant.setPkceCodeVerifier(pkce.getCodeVerifier());
        
        final OAuth2AccessToken token = service.getAccessToken(grant);
        System.out.println("Succès ! Token obtenu : " + token.getAccessToken());

        // 5. Exécution d'une requête signée
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");
        service.signRequest(token, request);

        try (Response response = service.execute(request)) {
            System.out.println("Réponse de l'API :");
            System.out.println(response.getBody());
        }
    }
}
