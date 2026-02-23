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

import com.github.scribejava.apis.microsoftazureactivedirectory.BaseMicrosoftAzureActiveDirectoryApi;
import com.github.scribejava.apis.microsoftazureactivedirectory.MicrosoftAzureActiveDirectory20BearerSignature;
import com.github.scribejava.core.oauth2.bearersignature.BearerSignature;

/**
 * Microsoft Azure Active Directory Api v 2.0
 *
 * @see <a
 *     href="https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-protocols-oauth-code">
 *     Understand the OAuth 2.0 authorization code flow in Azure AD | Microsoft Docs</a>
 * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/v1-overview">Microsoft
 *     Graph REST API v1.0 reference</a>
 * @see <a href="https://portal.azure.com">https://portal.azure.com</a>
 */

/**
 * API Microsoft Azure Active Directory v2.0.
 *
 * @see <a
 *     href="https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-protocols-oauth-code">
 *     Azure AD OAuth 2.0 Documentation</a>
 */
public class MicrosoftAzureActiveDirectory20Api extends BaseMicrosoftAzureActiveDirectoryApi {

  /** Constructeur utilisant le tenant par défaut. */
  protected MicrosoftAzureActiveDirectory20Api() {
    this(COMMON_TENANT);
  }

  /**
   * Constructeur pour un tenant spécifique.
   *
   * @param tenant L'identifiant du tenant.
   */
  protected MicrosoftAzureActiveDirectory20Api(String tenant) {
    super(tenant);
  }

  /**
   * Retourne l'instance unique (singleton) pour le tenant par défaut.
   *
   * @return L'instance de {@link MicrosoftAzureActiveDirectory20Api}.
   */
  public static MicrosoftAzureActiveDirectory20Api instance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Crée une instance pour un tenant personnalisé.
   *
   * @param tenant L'identifiant du tenant.
   * @return Une nouvelle instance de {@link MicrosoftAzureActiveDirectory20Api}.
   */
  public static MicrosoftAzureActiveDirectory20Api custom(String tenant) {
    return new MicrosoftAzureActiveDirectory20Api(tenant);
  }

  @Override
  public BearerSignature getBearerSignature() {
    return MicrosoftAzureActiveDirectory20BearerSignature.instance();
  }

  @Override
  protected String getEndpointVersionPath() {
    return "/v2.0";
  }

  private static class InstanceHolder {

    private static final MicrosoftAzureActiveDirectory20Api INSTANCE =
        new MicrosoftAzureActiveDirectory20Api();
  }
}
