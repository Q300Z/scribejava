package com.github.scribejava.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OpenID Provider Metadata.
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

    public OidcProviderMetadata(String issuer, String authorizationEndpoint, String tokenEndpoint, String jwksUri,
                                List<String> responseTypesSupported, List<String> subjectTypesSupported,
                                List<String> idTokenSigningAlgValuesSupported, String userinfoEndpoint,
                                String registrationEndpoint, List<String> scopesSupported,
                                List<String> responseModesSupported, List<String> grantTypesSupported,
                                String revocationEndpoint, String introspectionEndpoint,
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
                getAsList(node.get("dpop_signing_alg_values_supported"))
        );
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
}
