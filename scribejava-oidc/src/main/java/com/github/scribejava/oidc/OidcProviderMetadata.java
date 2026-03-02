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
package com.github.scribejava.oidc;

import com.github.scribejava.core.utils.JsonUtils;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Représente les métadonnées d'un fournisseur OpenID (OpenID Provider Metadata). */
public class OidcProviderMetadata {

  private final String issuer;
  private final String authorizationEndpoint;
  private final String tokenEndpoint;
  private final String jwksUri;
  private final List<String> responseTypesSupported;
  private final List<String> subjectTypesSupported;
  private final List<String> idTokenSigningAlgValuesSupported;

  // Optional fields
  private final String userinfoEndpoint;
  private final String registrationEndpoint;
  private final List<String> scopesSupported;
  private final List<String> responseModesSupported;
  private final List<String> grantTypesSupported;
  private final String revocationEndpoint;
  private final String introspectionEndpoint;
  private final String pushedAuthorizationRequestEndpoint;
  private final List<String> dpopSigningAlgValuesSupported;
  private final String rawResponse;

  public OidcProviderMetadata(
      String issuer,
      String authorizationEndpoint,
      String tokenEndpoint,
      String jwksUri,
      List<String> responseTypesSupported,
      List<String> subjectTypesSupported,
      List<String> idTokenSigningAlgValuesSupported,
      String userinfoEndpoint,
      String registrationEndpoint,
      List<String> scopesSupported,
      List<String> responseModesSupported,
      List<String> grantTypesSupported,
      String revocationEndpoint,
      String introspectionEndpoint,
      String pushedAuthorizationRequestEndpoint,
      List<String> dpopSigningAlgValuesSupported) {
    this(issuer, authorizationEndpoint, tokenEndpoint, jwksUri, responseTypesSupported,
        subjectTypesSupported, idTokenSigningAlgValuesSupported, userinfoEndpoint,
        registrationEndpoint, scopesSupported, responseModesSupported, grantTypesSupported,
        revocationEndpoint, introspectionEndpoint, pushedAuthorizationRequestEndpoint,
        dpopSigningAlgValuesSupported, null);
  }

  public OidcProviderMetadata(
      String issuer,
      String authorizationEndpoint,
      String tokenEndpoint,
      String jwksUri,
      List<String> responseTypesSupported,
      List<String> subjectTypesSupported,
      List<String> idTokenSigningAlgValuesSupported,
      String userinfoEndpoint,
      String registrationEndpoint,
      List<String> scopesSupported,
      List<String> responseModesSupported,
      List<String> grantTypesSupported,
      String revocationEndpoint,
      String introspectionEndpoint,
      String pushedAuthorizationRequestEndpoint,
      List<String> dpopSigningAlgValuesSupported,
      String rawResponse) {
    this.issuer = issuer;
    this.authorizationEndpoint = authorizationEndpoint;
    this.tokenEndpoint = tokenEndpoint;
    this.jwksUri = jwksUri;
    this.responseTypesSupported = responseTypesSupported;
    this.subjectTypesSupported = subjectTypesSupported;
    this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
    this.userinfoEndpoint = userinfoEndpoint;
    this.registrationEndpoint = registrationEndpoint;
    this.scopesSupported = scopesSupported;
    this.responseModesSupported = responseModesSupported;
    this.grantTypesSupported = grantTypesSupported;
    this.revocationEndpoint = revocationEndpoint;
    this.introspectionEndpoint = introspectionEndpoint;
    this.pushedAuthorizationRequestEndpoint = pushedAuthorizationRequestEndpoint;
    this.dpopSigningAlgValuesSupported = dpopSigningAlgValuesSupported;
    this.rawResponse = rawResponse;
  }

  /**
   * Analyse une chaîne JSON.
   *
   * @param json JSON
   * @return Metadata
   * @throws IOException erreur
   */
  public static OidcProviderMetadata parse(String json) throws IOException {
    final Map<String, Object> node = JsonUtils.parse(json);

    return new OidcProviderMetadata(
        (String) node.get("issuer"),
        (String) node.get("authorization_endpoint"),
        (String) node.get("token_endpoint"),
        (String) node.get("jwks_uri"),
        getAsList(node.get("response_types_supported")),
        getAsList(node.get("subject_types_supported")),
        getAsList(node.get("id_token_signing_alg_values_supported")),
        (String) node.get("userinfo_endpoint"),
        (String) node.get("registration_endpoint"),
        getAsList(node.get("scopes_supported")),
        getAsList(node.get("response_modes_supported")),
        getAsList(node.get("grant_types_supported")),
        (String) node.get("revocation_endpoint"),
        (String) node.get("introspection_endpoint"),
        (String) node.get("pushed_authorization_request_endpoint"),
        getAsList(node.get("dpop_signing_alg_values_supported")),
        json);
  }

  @SuppressWarnings("unchecked")
  private static List<String> getAsList(Object node) {
    if (node instanceof List) {
      return Collections.unmodifiableList((List<String>) node);
    }
    return Collections.emptyList();
  }

  public String getIssuer() {
    return issuer;
  }

  public String getAuthorizationEndpoint() {
    return authorizationEndpoint;
  }

  public String getTokenEndpoint() {
    return tokenEndpoint;
  }

  public String getJwksUri() {
    return jwksUri;
  }

  public List<String> getResponseTypesSupported() {
    return responseTypesSupported;
  }

  public List<String> getSubjectTypesSupported() {
    return subjectTypesSupported;
  }

  public List<String> getIdTokenSigningAlgValuesSupported() {
    return idTokenSigningAlgValuesSupported;
  }

  public String getUserinfoEndpoint() {
    return userinfoEndpoint;
  }

  public String getRegistrationEndpoint() {
    return registrationEndpoint;
  }

  public List<String> getScopesSupported() {
    return scopesSupported;
  }

  public List<String> getResponseModesSupported() {
    return responseModesSupported;
  }

  public List<String> getGrantTypesSupported() {
    return grantTypesSupported;
  }

  public String getRevocationEndpoint() {
    return revocationEndpoint;
  }

  public String getIntrospectionEndpoint() {
    return introspectionEndpoint;
  }

  public String getPushedAuthorizationRequestEndpoint() {
    return pushedAuthorizationRequestEndpoint;
  }

  public List<String> getDpopSigningAlgValuesSupported() {
    return dpopSigningAlgValuesSupported;
  }

  public String getRawResponse() {
    return rawResponse;
  }
}
