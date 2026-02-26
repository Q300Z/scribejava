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

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthResponseException;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthRetryPolicy;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/** [QUICKSTART] Device Authorization Flow (RFC 8628) - Version Enterprise. */
@SuppressWarnings("PMD.SystemPrintln")
public final class DeviceFlowQuickStart {

  private static final String CLIENT_ID = config("SCRIBE_CLIENT_ID", "votre_client_id");

  private DeviceFlowQuickStart() {}

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {

    final OAuth20Service service = new ServiceBuilder(CLIENT_ID).build(GitHubApi.instance());
    service.setLogger(verboseLogger());
    service.setRetryPolicy(new OAuthRetryPolicy(3, 500));

    System.out.println("=== QuickStart Enterprise : Device Flow ===");

    try {
      // 1. Demande des codes au serveur
      System.out.println("Récupération des codes...");
      final DeviceAuthorization codes = service.getDeviceAuthorizationCodes();

      System.out.println("\n--- ACTION REQUISE ---");
      System.out.println("1. Allez sur : " + codes.getVerificationUri());
      System.out.println("2. Entrez le code suivant : " + codes.getUserCode());
      System.out.println("\nEn attente de votre autorisation (polling automatisé)...");

      // 2. Scrutation (Polling) jusqu'à validation
      final OAuth2AccessToken token = service.pollAccessTokenDeviceAuthorizationGrant(codes);

      System.out.println("\n✅ Succès ! Autorisation accordée.");
      System.out.println("Access Token : " + token.getAccessToken());

    } catch (OAuthResponseException e) {
      System.err.println("\n❌ Échec du flux Device :");
      e.getErrorDetails()
          .ifPresent(
              details -> {
                System.err.println("Code : " + details.getString("error"));
                System.err.println("Desc : " + details.getString("error_description"));
              });
    }
  }
}
