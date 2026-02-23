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

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class OidcMetadataExtendedTest {

    @Test
    public void shouldParseCompleteMetadata() throws IOException {
        final String json =
                "{"
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
