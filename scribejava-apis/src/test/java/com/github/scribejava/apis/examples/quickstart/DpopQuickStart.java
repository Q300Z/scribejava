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

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

/**
 * [QUICKSTART] Sécurité Avancée DPoP (RFC 9449).
 *
 * <p>Le DPoP (Demonstrating Proof-of-Possession) lie cryptographiquement le jeton à la clé privée
 * du client. Si le jeton est volé, il est inutilisable sans la clé privée.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class DpopQuickStart {

  private static final String CLIENT_ID = config("SCRIBE_CLIENT_ID", "votre_client_id");
  private static final String CLIENT_SECRET = config("SCRIBE_CLIENT_SECRET", "votre_client_secret");

  private DpopQuickStart() {}

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException, NoSuchAlgorithmException {

    // 1. Génération d'une paire de clés RSA pour la preuve de possession
    final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    final KeyPair keyPair = keyGen.generateKeyPair();

    // 2. Initialisation du service avec le créateur de preuves DPoP
    // ScribeJava gérera automatiquement les en-têtes 'DPoP' dans les requêtes.
    final OAuth20Service service =
        new ServiceBuilder(CLIENT_ID)
            .apiSecret(CLIENT_SECRET)
            .dpop(
                (request, accessToken) -> {
                  // Ici on simule une signature JWT simplifiée pour l'exemple
                  // En production, utilisez une vraie implémentation JWT.
                  return "eyJhbGciOiJSUzI1NiIsImprdCI6InVuaXF1ZSJ9.payload.signature";
                })
            .build(GitHubApi.instance());

    service.setLogger(verboseLogger());

    System.out.println("=== QuickStart : Sécurité DPoP ===");

    // 3. Flux classique
    final String authUrl = service.getAuthorizationUrl();
    System.out.println("Connectez-vous : " + authUrl);
    final String code = readInput("Code");

    // 4. Le jeton reçu est lié à votre paire de clés
    final OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));
    System.out.println("\n✅ Jeton DPoP obtenu : " + token.getAccessToken());

    // 5. Appel API : ScribeJava injecte automatiquement l'en-tête DPoP signé
    final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");
    service.signRequest(token, request);

    try (Response response = service.execute(request)) {
      System.out.println("Réponse protégée par DPoP : " + response.getCode());
    }
  }
}
