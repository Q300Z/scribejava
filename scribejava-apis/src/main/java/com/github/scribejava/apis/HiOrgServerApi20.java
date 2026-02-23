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
package com.github.scribejava.apis;

import com.github.scribejava.core.builder.api.DefaultApi20;

/**
 * API OAuth 2.0 pour HiOrg-Server.
 *
 * @see <a href="https://wiki.hiorg-server.de/admin/oauth2">HiOrg-Server OAuth Documentation</a>
 */
public class HiOrgServerApi20 extends DefaultApi20 {

  private final String version;

  /** Constructeur utilisant la version par défaut ("v1"). */
  protected HiOrgServerApi20() {
    this("v1");
  }

  /**
   * Constructeur pour une version spécifique.
   *
   * @param version La version de l'API.
   */
  protected HiOrgServerApi20(String version) {
    this.version = version;
  }

  /**
   * Retourne l'instance unique (singleton) de l'API HiOrg-Server.
   *
   * @return L'instance de {@link HiOrgServerApi20}.
   */
  public static HiOrgServerApi20 instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return "https://www.hiorg-server.de/api/oauth2/" + version + "/token.php";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return "https://www.hiorg-server.de/api/oauth2/" + version + "/authorize.php";
  }

  private static class InstanceHolder {

    private static final HiOrgServerApi20 INSTANCE = new HiOrgServerApi20();
  }
}
