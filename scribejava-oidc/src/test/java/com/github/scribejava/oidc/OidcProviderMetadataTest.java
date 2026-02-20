package com.github.scribejava.oidc;

import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OidcProviderMetadataTest {

    private static final String JSON_CONFIG = "{" +
            "\"issuer\":\"https://server.example.com\"," +
            "\"authorization_endpoint\":\"https://server.example.com/authorize\"," +
            "\"token_endpoint\":\"https://server.example.com/token\"," +
            "\"jwks_uri\":\"https://server.example.com/jwks.json\"," +
            "\"response_types_supported\":[\"code\",\"id_token\"]," +
            "\"subject_types_supported\":[\"public\"]," +
            "\"id_token_signing_alg_values_supported\":[\"RS256\"]" +
            "}";

    @Test
    public void shouldParseMetadataFromJson() throws IOException {
        final OidcProviderMetadata metadata = OidcProviderMetadata.parse(JSON_CONFIG);

        assertNotNull(metadata);
        assertEquals("https://server.example.com", metadata.getIssuer());
        assertEquals("https://server.example.com/authorize", metadata.getAuthorizationEndpoint());
        assertEquals("https://server.example.com/token", metadata.getTokenEndpoint());
        assertEquals("https://server.example.com/jwks.json", metadata.getJwksUri());
        assertTrue(metadata.getResponseTypesSupported().contains("code"));
        assertTrue(metadata.getSubjectTypesSupported().contains("public"));
        assertTrue(metadata.getIdTokenSigningAlgValuesSupported().contains("RS256"));
    }
}
