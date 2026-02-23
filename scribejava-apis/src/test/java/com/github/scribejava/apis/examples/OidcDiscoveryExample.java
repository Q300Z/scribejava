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

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import com.github.scribejava.oidc.DefaultOidcApi20;
import com.github.scribejava.oidc.IdToken;
import com.github.scribejava.oidc.OidcDiscoveryService;
import com.github.scribejava.oidc.OidcProviderMetadata;
import com.github.scribejava.oidc.OidcService;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Exemple complet d'implémentation pour OpenID Connect (OIDC) avec découverte dynamique.
 *
 * <p>Ce programme démontre la puissance de ScribeJava pour OIDC :
 * 1. Découverte automatique des endpoints via l'Issuer URI (ex: Google).
 * 2. Création dynamique de l'API à partir des métadonnées récupérées.
 * 3. Flux d'autorisation standard.
 * 4. Validation de l'ID Token récupéré.
 * 5. Appel de l'endpoint UserInfo.
 */
@SuppressWarnings("PMD.SystemPrintln")
public class OidcDiscoveryExample {

    private static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String CLIENT_ID = "votre_client_id.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "votre_client_secret";

    private OidcDiscoveryExample() {
    }

    /**
     * Point d'entrée de l'exemple.
     *
     * @param args Arguments de la ligne de commande (non utilisés).
     * @throws IOException Si une erreur E/S survient.
     * @throws InterruptedException Si l'exécution est interrompue.
     * @throws ExecutionException Si une erreur survient lors de l'exécution asynchrone.
     */
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        System.out.println("=== Découverte Dynamique OIDC avec Google ===");

        // 1. Découverte des métadonnées
        System.out.println("Récupération de la configuration depuis : " + GOOGLE_ISSUER);
        final OidcDiscoveryService discoveryService = new OidcDiscoveryService(
                GOOGLE_ISSUER,
                new com.github.scribejava.core.httpclient.jdk.JDKHttpClient(
                        com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig.defaultConfig()),
                "ScribeJavaExample/9.0.0"
        );
        final OidcProviderMetadata metadata = discoveryService.getProviderMetadata();
        System.out.println("Métadonnées récupérées (Authorization Endpoint: " + metadata.getAuthorizationEndpoint() + ")");

        // 2. Initialisation du service OIDC
        final OidcService service = (OidcService) new ServiceBuilder(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .defaultScope("openid profile email")
                .callback("http://localhost:8080/callback")
                .build(new DefaultOidcApi20(metadata) {
                    @Override
                    public String getIssuer() {
                        return GOOGLE_ISSUER;
                    }
                });

        final Scanner in = new Scanner(System.in, "UTF-8");

        // 3. Obtention de l'URL d'autorisation
        final String authorizationUrl = service.getAuthorizationUrl();
        System.out.println("1. Ouvrez cette URL dans votre navigateur :");
        System.out.println(authorizationUrl);
        System.out.print("2. Collez le code reçu ici >> ");
        final String code = in.nextLine();

        // 4. Échange du code contre un jeton (Access Token + ID Token)
        System.out.println("Échange du code...");
        final com.github.scribejava.core.model.OAuth2AccessToken token = service.getAccessToken(
                new AuthorizationCodeGrant(code)
        );

        // 5. Validation et lecture de l'ID Token
        if (token instanceof com.github.scribejava.apis.openid.OpenIdOAuth2AccessToken) {
            final String rawIdToken = ((com.github.scribejava.apis.openid.OpenIdOAuth2AccessToken) token).getOpenIdToken();
            System.out.println("ID Token (JWT) : " + rawIdToken);

            System.out.println("Validation de l'ID Token...");
            final IdToken idToken = service.validateIdToken(rawIdToken);
            System.out.println("Utilisateur identifié (Subject) : " + idToken.getSubject());
            System.out.println("Email (depuis JWT) : " + idToken.getClaimsSet().getStringClaim("email"));
        }

        // 6. Récupération des UserInfo (Appel API haut niveau)
        System.out.println("Récupération des UserInfo complètes...");
        final com.github.scribejava.oidc.StandardClaims claims = service.getUserInfoAsync(token).get();
        System.out.println("Nom : " + claims.getName());
        System.out.println("Email : " + claims.getEmail());

        System.out.println("=== Fin de l'exemple ===");
    }
}
