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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Représente les métadonnées d'un fournisseur OpenID (OpenID Provider Metadata).
 *
 * <p>Ces métadonnées sont généralement récupérées via le mécanisme de découverte (Discovery) à
 * l'URL {@code /.well-known/openid-configuration} et décrivent les capacités et les points de
 * terminaison (endpoints) du fournisseur.
 *
 * @see <a href="http://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">OpenID
 * Connect Discovery 1.0, Section 3 (OpenID Provider Metadata)</a>
 * @see <a href="https://tools.ietf.org/html/rfc8414">RFC 8414 (OAuth 2.0 Authorization Server
 * Metadata)</a>
 */
public class OidcProviderMetadata {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    /**
     * Constructeur complet.
     *
     * @param issuer                             L'identifiant de l'émetteur (iss).
     * @param authorizationEndpoint              L'URL du point de terminaison d'autorisation.
     * @param tokenEndpoint                      L'URL du point de terminaison de jeton.
     * @param jwksUri                            L'URL du document JWK Set contenant les clés de signature.
     * @param responseTypesSupported             Liste des types de réponse supportés.
     * @param subjectTypesSupported              Liste des types d'identifiant de sujet supportés.
     * @param idTokenSigningAlgValuesSupported   Liste des algorithmes de signature pour l'ID Token.
     * @param userinfoEndpoint                   URL du point de terminaison UserInfo (Optionnel).
     * @param registrationEndpoint               URL du point de terminaison d'enregistrement dynamique (Optionnel).
     * @param scopesSupported                    Liste des portées supportées (Optionnel).
     * @param responseModesSupported             Liste des modes de réponse supportés (Optionnel).
     * @param grantTypesSupported                Liste des types de concession supportés (Optionnel).
     * @param revocationEndpoint                 URL du point de terminaison de révocation (Optionnel).
     * @param introspectionEndpoint              URL du point de terminaison d'introspection (Optionnel).
     * @param pushedAuthorizationRequestEndpoint URL du point de terminaison PAR (Optionnel).
     * @param dpopSigningAlgValuesSupported      Liste des algorithmes DPoP supportés (Optionnel).
     */
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
    }

    /**
     * Analyse une chaîne JSON pour extraire les métadonnées.
     *
     * @param json Le contenu JSON du document de configuration.
     * @return Une instance de {@link OidcProviderMetadata}.
     * @throws IOException si le JSON est malformé.
     */
    public static OidcProviderMetadata parse(String json) throws IOException {
        final JsonNode node = OBJECT_MAPPER.readTree(json);

        return new OidcProviderMetadata(
                getAsString(node.get("issuer")),
                getAsString(node.get("authorization_endpoint")),
                getAsString(node.get("token_endpoint")),
                getAsString(node.get("jwks_uri")),
                getAsList(node.get("response_types_supported")),
                getAsList(node.get("subject_types_supported")),
                getAsList(node.get("id_token_signing_alg_values_supported")),
                getAsString(node.get("userinfo_endpoint")),
                getAsString(node.get("registration_endpoint")),
                getAsList(node.get("scopes_supported")),
                getAsList(node.get("response_modes_supported")),
                getAsList(node.get("grant_types_supported")),
                getAsString(node.get("revocation_endpoint")),
                getAsString(node.get("introspection_endpoint")),
                getAsString(node.get("pushed_authorization_request_endpoint")),
                getAsList(node.get("dpop_signing_alg_values_supported")));
    }

    private static String getAsString(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }

    private static List<String> getAsList(JsonNode node) {
        if (node == null || node.isNull() || !node.isArray()) {
            return Collections.emptyList();
        }
        final List<String> list = new ArrayList<>();
        for (JsonNode item : node) {
            list.add(item.asText());
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * @return L'identifiant de l'émetteur.
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * @return L'URL du point de terminaison d'autorisation.
     */
    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    /**
     * @return L'URL du point de terminaison de jeton.
     */
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    /**
     * @return L'URL du point de terminaison JWK Set.
     */
    public String getJwksUri() {
        return jwksUri;
    }

    /**
     * @return Liste des types de réponse supportés.
     */
    public List<String> getResponseTypesSupported() {
        return responseTypesSupported;
    }

    /**
     * @return Liste des types d'identifiant de sujet supportés (public, pairwise).
     */
    public List<String> getSubjectTypesSupported() {
        return subjectTypesSupported;
    }

    /**
     * @return Liste des algorithmes de signature supportés pour l'ID Token.
     */
    public List<String> getIdTokenSigningAlgValuesSupported() {
        return idTokenSigningAlgValuesSupported;
    }

    /**
     * @return L'URL du point de terminaison UserInfo.
     */
    public String getUserinfoEndpoint() {
        return userinfoEndpoint;
    }

    /**
     * @return L'URL du point de terminaison d'enregistrement dynamique.
     */
    public String getRegistrationEndpoint() {
        return registrationEndpoint;
    }

    /**
     * @return Liste des portées supportées.
     */
    public List<String> getScopesSupported() {
        return scopesSupported;
    }

    /**
     * @return Liste des modes de réponse supportés.
     */
    public List<String> getResponseModesSupported() {
        return responseModesSupported;
    }

    /**
     * @return Liste des types de concession supportés.
     */
    public List<String> getGrantTypesSupported() {
        return grantTypesSupported;
    }

    /**
     * @return L'URL du point de terminaison de révocation.
     */
    public String getRevocationEndpoint() {
        return revocationEndpoint;
    }

    /**
     * @return L'URL du point de terminaison d'introspection.
     */
    public String getIntrospectionEndpoint() {
        return introspectionEndpoint;
    }

    /**
     * @return L'URL du point de terminaison des requêtes d'autorisation poussées (PAR).
     */
    public String getPushedAuthorizationRequestEndpoint() {
        return pushedAuthorizationRequestEndpoint;
    }

    /**
     * @return Liste des algorithmes supportés pour les preuves DPoP.
     */
    public List<String> getDpopSigningAlgValuesSupported() {
        return dpopSigningAlgValuesSupported;
    }
}
