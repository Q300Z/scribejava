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
import static com.github.scribejava.apis.examples.quickstart.QuickStartUtils.verboseLogger;

import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthResponseException;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthRetryPolicy;
import com.github.scribejava.core.oauth2.grant.ClientCredentialsGrant;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * [QUICKSTART] Client Credentials (M2M) - Version Résiliente.
 *
 * <p>Ce flux machine-to-machine est optimisé ici pour la production : 1. Configuration sécurisée
 * (ENV). 2. Logging verbeux pour l'audit. 3. Politique de Retry (3 essais) pour les erreurs
 * transitoires. 4. Capture et analyse des erreurs JSON du serveur.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class ClientCredentialsQuickStart {

  private static final String CLIENT_ID = config("SCRIBE_CLIENT_ID", "votre_client_id");
  private static final String CLIENT_SECRET = config("SCRIBE_CLIENT_SECRET", "votre_client_secret");

  private ClientCredentialsQuickStart() {}

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {

    // 1. Initialisation avec Logging et Retry
    final OAuth20Service service =
        new ServiceBuilder(CLIENT_ID)
            .apiSecret(CLIENT_SECRET)
            .defaultScope("https://graph.microsoft.com/.default")
            .build(MicrosoftAzureActiveDirectory20Api.instance());

    service.setLogger(verboseLogger());
    service.setRetryPolicy(new OAuthRetryPolicy(3, 500));

    System.out.println("=== QuickStart Enterprise : Client Credentials (M2M) ===");

    try {
      // 2. Obtention directe du jeton
      System.out.println("Authentification du client...");
      final OAuth2AccessToken token = service.getAccessToken(new ClientCredentialsGrant());

      System.out.println("\n✅ Succès !");
      System.out.println("Access Token : " + token.getAccessToken());
      System.out.println("Expire dans  : " + token.getExpiresIn() + " secondes.");

    } catch (OAuthResponseException e) {
      // 3. Analyse propre des erreurs API
      System.err.println("\n❌ Erreur du serveur d'identité :");
      e.getErrorDetails()
          .ifPresent(
              details -> {
                System.err.println("Code : " + details.getString("error"));
                System.err.println("Desc : " + details.getString("error_description"));
              });
    }
  }
}
