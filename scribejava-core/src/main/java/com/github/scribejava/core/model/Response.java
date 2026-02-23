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

import com.github.scribejava.core.utils.StreamUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Représente une réponse HTTP.
 *
 * <p>Cette classe contient le code de statut, les en-têtes et le corps de la réponse. Elle
 * implémente {@link Closeable} et doit être fermée pour libérer les ressources réseau (le flux du
 * corps).
 */
public class Response implements Closeable {

  private final int code;
  private final String message;
  private final Map<String, String> headers;
  private String body;
  private InputStream stream;
  private Closeable[] closeables;
  private boolean closed;

  private Response(int code, String message, Map<String, String> headers) {
    this.code = code;
    this.message = message;
    this.headers = headers;
  }

  /**
   * Constructeur avec flux de données.
   *
   * @param code Le code de statut HTTP.
   * @param message Le message de statut.
   * @param headers Le dictionnaire des en-têtes.
   * @param stream Le flux d'entrée du corps.
   * @param closeables Objets à fermer lors de la fermeture de la réponse.
   */
  public Response(
      int code,
      String message,
      Map<String, String> headers,
      InputStream stream,
      Closeable... closeables) {
    this(code, message, headers);
    this.stream = stream;
    this.closeables = closeables;
  }

  /**
   * Constructeur avec corps textuel déjà lu.
   *
   * @param code Le code de statut HTTP.
   * @param message Le message de statut.
   * @param headers Le dictionnaire des en-têtes.
   * @param body Le contenu du corps sous forme de chaîne.
   */
  public Response(int code, String message, Map<String, String> headers, String body) {
    this(code, message, headers);
    this.body = body;
  }

  private String parseBodyContents() throws IOException {
    if (stream == null) {
      return null;
    }
    if ("gzip".equals(getHeader("Content-Encoding"))) {
      body = StreamUtils.getGzipStreamContents(stream);
    } else {
      body = StreamUtils.getStreamContents(stream);
    }
    return body;
  }

  /**
   * Indique si la requête a réussi (code entre 200 et 399).
   *
   * @return true si le code de statut est un succès.
   */
  public boolean isSuccessful() {
    return code >= 200 && code < 400;
  }

  /**
   * Retourne le corps de la réponse sous forme de chaîne de caractères.
   *
   * <p>Cette méthode ferme automatiquement le flux de données sous-jacent.
   *
   * @return Le contenu du corps.
   * @throws IOException en cas d'erreur de lecture.
   */
  public String getBody() throws IOException {
    return body == null ? parseBodyContents() : body;
  }

  /**
   * Retourne le flux de données brut de la réponse.
   *
   * @return L'{@link InputStream} du corps.
   */
  public InputStream getStream() {
    return stream;
  }

  /**
   * Retourne le code de statut HTTP.
   *
   * @return Le code (ex: 200, 404).
   */
  public int getCode() {
    return code;
  }

  /**
   * Retourne le message de statut HTTP.
   *
   * @return Le message textuel (ex: "OK", "Not Found").
   */
  public String getMessage() {
    return message;
  }

  /**
   * Retourne l'ensemble des en-têtes de la réponse.
   *
   * @return Un dictionnaire des en-têtes HTTP.
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * Récupère la valeur d'un en-tête spécifique.
   *
   * @param name Le nom de l'en-tête.
   * @return La valeur de l'en-tête, ou null si absent.
   */
  public String getHeader(String name) {
    return headers.get(name);
  }

  @Override
  public String toString() {
    return "Response{"
        + "code="
        + code
        + ", message='"
        + message
        + '\''
        + ", body='"
        + body
        + '\''
        + ", headers="
        + headers
        + '}';
  }

  /**
   * Ferme la réponse et libère les ressources associées (flux, connexions).
   *
   * @throws IOException en cas d'erreur lors de la fermeture.
   */
  @Override
  public void close() throws IOException {
    if (closed) {
      return;
    }
    IOException ioException = null;
    if (closeables != null) {
      for (Closeable closeable : closeables) {
        if (closeable == null) {
          continue;
        }
        try {
          closeable.close();
        } catch (IOException ioE) {
          if (ioException != null) {
            ioException = ioE;
          }
        }
      }
    }
    if (ioException != null) {
      throw ioException;
    }
    closed = true;
  }
}
