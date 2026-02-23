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
package com.github.scribejava.core.httpclient.multipart;

import com.github.scribejava.core.httpclient.HttpClient;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Implémentation de {@link BodyPartPayload} simulant un fichier à partir d'un tableau d'octets. */
public class FileByteArrayBodyPartPayload extends ByteArrayBodyPartPayload {

  /**
   * Constructeur simple.
   *
   * @param payload Les données.
   */
  public FileByteArrayBodyPartPayload(byte[] payload) {
    this(payload, null);
  }

  /**
   * Constructeur avec décalage et longueur.
   *
   * @param payload Les données.
   * @param off Le décalage.
   * @param len La longueur.
   */
  public FileByteArrayBodyPartPayload(byte[] payload, int off, int len) {
    this(payload, off, len, null);
  }

  /**
   * Constructeur avec nom de champ.
   *
   * @param payload Les données.
   * @param name Le nom du champ de formulaire.
   */
  public FileByteArrayBodyPartPayload(byte[] payload, String name) {
    this(payload, name, null);
  }

  /**
   * Constructeur avec décalage, longueur et nom.
   *
   * @param payload Les données.
   * @param off Le décalage.
   * @param len La longueur.
   * @param name Le nom du champ.
   */
  public FileByteArrayBodyPartPayload(byte[] payload, int off, int len, String name) {
    this(payload, off, len, name, null);
  }

  /**
   * Constructeur avec nom de champ et nom de fichier.
   *
   * @param payload Les données.
   * @param name Le nom du champ.
   * @param filename Le nom du fichier.
   */
  public FileByteArrayBodyPartPayload(byte[] payload, String name, String filename) {
    this(null, payload, name, filename);
  }

  /**
   * Constructeur complet sans type de contenu.
   *
   * @param payload Les données.
   * @param off Le décalage.
   * @param len La longueur.
   * @param name Le nom du champ.
   * @param filename Le nom du fichier.
   */
  public FileByteArrayBodyPartPayload(
      byte[] payload, int off, int len, String name, String filename) {
    this(null, payload, off, len, name, filename);
  }

  /**
   * Constructeur avec type de contenu.
   *
   * @param contentType Le type de contenu.
   * @param payload Les données.
   */
  public FileByteArrayBodyPartPayload(String contentType, byte[] payload) {
    this(contentType, payload, null);
  }

  /**
   * Constructeur avec type de contenu, décalage et longueur.
   *
   * @param contentType Le type de contenu.
   * @param payload Les données.
   * @param off Le décalage.
   * @param len La longueur.
   */
  public FileByteArrayBodyPartPayload(String contentType, byte[] payload, int off, int len) {
    this(contentType, payload, off, len, null);
  }

  /**
   * Constructeur avec type de contenu et nom.
   *
   * @param contentType Le type de contenu.
   * @param payload Les données.
   * @param name Le nom du champ.
   */
  public FileByteArrayBodyPartPayload(String contentType, byte[] payload, String name) {
    this(contentType, payload, name, null);
  }

  /**
   * Constructeur avec type de contenu, décalage, longueur et nom.
   *
   * @param contentType Le type de contenu.
   * @param payload Les données.
   * @param off Le décalage.
   * @param len La longueur.
   * @param name Le nom du champ.
   */
  public FileByteArrayBodyPartPayload(
      String contentType, byte[] payload, int off, int len, String name) {
    this(contentType, payload, off, len, name, null);
  }

  /**
   * Constructeur avec type de contenu, données, nom et nom de fichier.
   *
   * @param contentType Le type de contenu.
   * @param payload Les données.
   * @param name Le nom du champ.
   * @param filename Le nom du fichier.
   */
  public FileByteArrayBodyPartPayload(
      String contentType, byte[] payload, String name, String filename) {
    super(payload, composeHeaders(contentType, name, filename));
  }

  /**
   * Constructeur complet avec type de contenu.
   *
   * @param contentType Le type de contenu.
   * @param payload Les données.
   * @param off Le décalage.
   * @param len La longueur.
   * @param name Le nom du champ.
   * @param filename Le nom du fichier.
   */
  public FileByteArrayBodyPartPayload(
      String contentType, byte[] payload, int off, int len, String name, String filename) {
    super(payload, off, len, composeHeaders(contentType, name, filename));
  }

  private static Map<String, String> composeHeaders(
      String contentType, String name, String filename) {

    String contentDispositionHeader = "form-data";
    if (name != null) {
      contentDispositionHeader += "; name=\"" + name + '"';
    }
    if (filename != null) {
      contentDispositionHeader += "; filename=\"" + filename + '"';
    }
    if (contentType == null) {
      return Collections.singletonMap("Content-Disposition", contentDispositionHeader);
    } else {
      final Map<String, String> headers = new HashMap<>();
      headers.put(HttpClient.CONTENT_TYPE, contentType);
      headers.put("Content-Disposition", contentDispositionHeader);
      return headers;
    }
  }
}
