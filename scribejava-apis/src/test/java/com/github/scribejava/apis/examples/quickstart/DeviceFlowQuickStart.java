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
import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * [QUICKSTART] Device Authorization Flow (RFC 8628).
 *
 * <p>Ce flux est conçu pour les appareils n'ayant pas de navigateur local (Smart TV, IoT, Terminaux
 * console). L'utilisateur autorise l'accès depuis un autre appareil (Smartphone/PC).
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class DeviceFlowQuickStart {

  private static final String CLIENT_ID = "votre_client_id";

  private DeviceFlowQuickStart() {}

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

    final OAuth20Service service = new ServiceBuilder(CLIENT_ID).build(GitHubApi.instance());

    System.out.println("=== QuickStart : Device Flow (RFC 8628) ===");

    // 1. Demande des codes au serveur
    System.out.println("Récupération des codes...");
    final DeviceAuthorization codes = service.getDeviceAuthorizationCodes();

    System.out.println("1. Allez sur : " + codes.getVerificationUri());
    System.out.println("2. Entrez le code suivant : " + codes.getUserCode());
    System.out.println("\nEn attente de votre autorisation (polling)...");

    // 2. Scrutation (Polling) jusqu'à validation par l'utilisateur
    final OAuth2AccessToken token = service.pollAccessTokenDeviceAuthorizationGrant(codes);

    System.out.println("Succès ! Autorisation accordée.");
    System.out.println("Access Token : " + token.getAccessToken());
  }
}
