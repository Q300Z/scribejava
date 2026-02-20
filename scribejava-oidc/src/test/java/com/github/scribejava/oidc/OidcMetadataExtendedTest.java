package com.github.scribejava.oidc;

import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OidcMetadataExtendedTest {

    @Test
    public void shouldParseCompleteMetadata() throws IOException {
        final String json = "{"
                + "\"issuer\":\"https://idp.com\","
                + "\"authorization_endpoint\":\"https://idp.com/auth\","
                + "\"token_endpoint\":\"https://idp.com/token\","
                + "\"jwks_uri\":\"https://idp.com/keys\","
                + "\"response_types_supported\":[\"code\", \"id_token\"],"
                + "\"subject_types_supported\":[\"public\"],"
                + "\"id_token_signing_alg_values_supported\":[\"RS256\"],"
                + "\"userinfo_endpoint\":\"https://idp.com/userinfo\","
                + "\"registration_endpoint\":\"https://idp.com/register\","
                + "\"scopes_supported\":[\"openid\", \"profile\"],"
                + "\"response_modes_supported\":[\"query\", \"form_post\"],"
                + "\"grant_types_supported\":[\"authorization_code\", \"refresh_token\"],"
                + "\"revocation_endpoint\":\"https://idp.com/revoke\","
                + "\"introspection_endpoint\":\"https://idp.com/introspect\","
                + "\"pushed_authorization_request_endpoint\":\"https://idp.com/par\","
                + "\"dpop_signing_alg_values_supported\":[\"RS256\", \"ES256\"]"
                + "}";

        final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);
        assertEquals("https://idp.com", metadata.getIssuer());
        assertEquals("https://idp.com/auth", metadata.getAuthorizationEndpoint());
        assertEquals("https://idp.com/userinfo", metadata.getUserinfoEndpoint());
        assertEquals("https://idp.com/register", metadata.getRegistrationEndpoint());
        assertEquals("https://idp.com/revoke", metadata.getRevocationEndpoint());
        assertEquals("https://idp.com/introspect", metadata.getIntrospectionEndpoint());
        assertEquals("https://idp.com/par", metadata.getPushedAuthorizationRequestEndpoint());
        assertTrue(metadata.getDpopSigningAlgValuesSupported().contains("ES256"));
        assertTrue(metadata.getScopesSupported().contains("profile"));
    }

    @Test
    public void shouldHandleEmptyMetadata() throws IOException {
        final String json = "{}";
        final OidcProviderMetadata metadata = OidcProviderMetadata.parse(json);
        assertNull(metadata.getIssuer());
        assertTrue(metadata.getScopesSupported().isEmpty());
    }
}
