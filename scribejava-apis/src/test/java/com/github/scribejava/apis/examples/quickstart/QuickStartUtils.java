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

import com.github.scribejava.core.model.OAuthLogger;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import java.util.Scanner;

/** Utilitaires partagés pour les QuickStarts. */
@SuppressWarnings("PMD.SystemPrintln")
public final class QuickStartUtils {

  private QuickStartUtils() {}

  /**
   * Lit une configuration depuis l'environnement ou retourne une valeur par défaut.
   *
   * @param key Clé (ex: SCRIBE_CLIENT_ID)
   * @param defaultValue Valeur de secours
   * @return La valeur configurée
   */
  public static String config(String key, String defaultValue) {
    final String val = System.getenv(key);
    return val == null || val.isEmpty() ? defaultValue : val;
  }

  /**
   * Logger simple pour afficher les flux OAuth dans la console.
   *
   * @return Une instance de OAuthLogger
   */
  public static OAuthLogger verboseLogger() {
    return new OAuthLogger() {
      @Override
      public void logRequest(OAuthRequest request) {
        System.out.println("\n[DEBUG] >> ENVOI : " + request.getVerb() + " " + request.getUrl());
      }

      @Override
      public void logResponse(Response response) {
        try {
          System.out.println("[DEBUG] << REÇU  : " + response.getCode());
        } catch (Exception e) {
          System.err.println("Erreur log : " + e.getMessage());
        }
      }
    };
  }

  /**
   * Lit une entrée utilisateur depuis la console.
   *
   * @param prompt Message à afficher
   * @return La chaîne saisie
   */
  public static String readInput(String prompt) {
    System.out.print(prompt + " >> ");
    return new Scanner(System.in, "UTF-8").nextLine();
  }
}
