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
package com.github.scribejava.core.model;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Implémentation par défaut de {@link OAuthLogger} qui formate et écrit les requêtes et réponses
 * OAuth sur un flux de sortie, tout en masquant de manière sécurisée les secrets.
 */
public class DefaultOAuthLogger implements OAuthLogger {

  private static final String[] SENSITIVE_FIELDS = {
    "access_token", "refresh_token", "id_token", "code", "client_secret", "password", "assertion"
  };

  private final PrintStream out;

  /**
   * Constructeur avec un {@link OutputStream}.
   *
   * @param out Le flux de sortie sur lequel écrire les logs.
   */
  public DefaultOAuthLogger(OutputStream out) {
    PrintStream tempOut;
    if (out instanceof PrintStream) {
      tempOut = (PrintStream) out;
    } else {
      try {
        tempOut = new PrintStream(out, true, StandardCharsets.UTF_8.name());
      } catch (UnsupportedEncodingException e) {
        // UTF-8 est garanti d'être supporté par la JVM
        tempOut = new PrintStream(out, true);
      }
    }
    this.out = tempOut;
  }

  /** Constructeur par défaut écrivant sur la sortie standard (System.out). */
  public DefaultOAuthLogger() {
    this(System.out);
  }

  @Override
  public void logRequest(OAuthRequest request) {
    if (request == null) {
      return;
    }
    out.println("[ScribeJava] ---> HTTP REQUEST");
    out.println(request.toDebugString().trim());
    out.println("cURL: " + request.toCurlCommand(true));
    out.println("[ScribeJava] ---> END HTTP REQUEST");
  }

  @Override
  public void logResponse(Response response) {
    if (response == null) {
      return;
    }
    out.println("[ScribeJava] <--- HTTP RESPONSE");
    out.println("Status Code: " + response.getCode());

    out.println("Headers:");
    out.println("  [Response Headers]");
    // Nous n'avons pas d'accès direct au dictionnaire des en-têtes de réponse mais nous pouvons
    // les intercepter si nécessaire ou lister ce qu'on a. Dans Response, getHeader est disponible.
    // Puisque Response.toString() affiche code, message, body et headers de manière brute, nous
    // pouvons plutôt formater proprement. Malheureusement, la map headers de Response n'a pas de
    // getter public,
    // mais toString() de Response expose les headers bruts si on l'appelle, ce qui peut fuiter !
    // C'est pourquoi nous allons extraire le corps et masquer les secrets.

    try {
      String body = response.getBody();
      if (body != null) {
        out.println("Body:");
        out.println("  " + sanitizeResponseBody(body).trim());
      }
    } catch (IOException e) {
      out.println("Body: [Error reading response body: " + e.getMessage() + "]");
    }
    out.println("[ScribeJava] <--- END HTTP RESPONSE");
  }

  /**
   * Masque les jetons et secrets présents dans le corps de la réponse (JSON ou form URL-encoded).
   *
   * @param body Le corps de réponse brut.
   * @return Le corps de réponse nettoyé.
   */
  public String sanitizeResponseBody(String body) {
    if (body == null) {
      return "";
    }
    String sanitized = body;
    for (String field : SENSITIVE_FIELDS) {
      // Expression régulière pour masquer les propriétés JSON "field": "value"
      sanitized =
          sanitized.replaceAll("(?i)(\"" + field + "\"\\s*:\\s*\")[^\"]*(\")", "$1[MASKED]$2");
      // Expression régulière pour masquer les paramètres de formulaire field=value
      sanitized = sanitized.replaceAll("(?i)(" + field + "=)[^&]*", "$1[MASKED]");
    }
    return sanitized;
  }
}
