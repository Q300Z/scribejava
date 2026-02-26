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

import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.grant.ClientCredentialsGrant;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * [QUICKSTART] Client Credentials (Machine-to-Machine).
 *
 * <p>Ce flux est utilisé par les serveurs ou services automatisés sans interaction humaine. Il n'y
 * a pas d'URL d'autorisation, le client s'authentifie directement avec son secret.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class ClientCredentialsQuickStart {

  private static final String CLIENT_ID = "votre_client_id";
  private static final String CLIENT_SECRET = "votre_client_secret";

  private ClientCredentialsQuickStart() {}

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

    // 1. Initialisation du service (Exemple Azure AD)
    final OAuth20Service service =
        new ServiceBuilder(CLIENT_ID)
            .apiSecret(CLIENT_SECRET)
            .defaultScope("https://graph.microsoft.com/.default")
            .build(MicrosoftAzureActiveDirectory20Api.instance());

    System.out.println("=== QuickStart : Client Credentials (M2M) ===");

    // 2. Obtention directe du jeton
    System.out.println("Authentification du client...");
    final OAuth2AccessToken token = service.getAccessToken(new ClientCredentialsGrant());

    System.out.println("Succès !");
    System.out.println("Access Token : " + token.getAccessToken());
    System.out.println("Expire dans : " + token.getExpiresIn() + " secondes.");
  }
}
