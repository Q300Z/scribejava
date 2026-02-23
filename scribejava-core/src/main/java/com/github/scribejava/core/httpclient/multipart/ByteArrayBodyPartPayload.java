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

import java.util.Map;

/** Implémentation de {@link BodyPartPayload} pour les données en tableau d'octets (byte array). */
public class ByteArrayBodyPartPayload extends BodyPartPayload {

  private final byte[] payload;
  private final int off;
  private final int len;

  /**
   * Constructeur complet.
   *
   * @param payload Les données.
   * @param off Le décalage (offset) dans le tableau.
   * @param len La longueur des données à lire.
   * @param headers Les en-têtes de cette partie.
   */
  public ByteArrayBodyPartPayload(byte[] payload, int off, int len, Map<String, String> headers) {
    super(headers);
    this.payload = payload;
    this.off = off;
    this.len = len;
  }

  /**
   * Constructeur avec en-têtes.
   *
   * @param payload Les données.
   * @param headers Les en-têtes.
   */
  public ByteArrayBodyPartPayload(byte[] payload, Map<String, String> headers) {
    this(payload, 0, payload.length, headers);
  }

  /**
   * Constructeur avec type de contenu.
   *
   * @param payload Les données.
   * @param contentType Le type de contenu.
   */
  public ByteArrayBodyPartPayload(byte[] payload, String contentType) {
    this(payload, convertContentTypeToHeaders(contentType));
  }

  /**
   * Constructeur simple sans en-tête.
   *
   * @param payload Les données.
   */
  public ByteArrayBodyPartPayload(byte[] payload) {
    this(payload, (Map<String, String>) null);
  }

  /**
   * Retourne le tableau d'octets.
   *
   * @return Le tableau payload.
   */
  public byte[] getPayload() {
    return payload;
  }

  /**
   * Retourne le décalage.
   *
   * @return L'offset.
   */
  public int getOff() {
    return off;
  }

  /**
   * Retourne la longueur.
   *
   * @return La longueur.
   */
  public int getLen() {
    return len;
  }
}
