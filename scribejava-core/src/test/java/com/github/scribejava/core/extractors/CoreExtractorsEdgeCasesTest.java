package com.github.scribejava.core.extractors;

import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.Response;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CoreExtractorsEdgeCasesTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectEmptyResponseInAccessTokenExtractor() throws IOException {
        final Response response = new Response(200, "OK", Collections.emptyMap(), "");
        OAuth2AccessTokenJsonExtractor.instance().extract(response);
    }

    @Test
    public void shouldExtractAccessTokenWithMissingOptionalParams() throws IOException {
        final String json = "{\"access_token\":\"token123\"}";
        final Response response = new Response(200, "OK", Collections.emptyMap(), json);
        final OAuth2AccessToken token = OAuth2AccessTokenJsonExtractor.instance().extract(response);
        assertEquals("token123", token.getAccessToken());
        assertNull(token.getExpiresIn());
    }

    @Test(expected = OAuth2AccessTokenErrorResponse.class)
    public void shouldHandleOAuth2ErrorResponse() throws IOException {
        final String json = "{\"error\":\"invalid_request\", \"error_description\":\"bad stuff\"}";
        final Response response = new Response(400, "Bad Request", Collections.emptyMap(), json);
        OAuth2AccessTokenJsonExtractor.instance().extract(response);
    }

    @Test
    public void shouldExtractDeviceAuthorization() throws IOException {
        final String json = "{"
                + "\"device_code\":\"dc123\","
                + "\"user_code\":\"uc456\","
                + "\"verification_uri\":\"https://uri\","
                + "\"expires_in\":600,"
                + "\"interval\":5"
                + "}";
        final Response response = new Response(200, "OK", Collections.emptyMap(), json);
        final DeviceAuthorization auth = DeviceAuthorizationJsonExtractor.instance().extract(response);
        assertEquals("dc123", auth.getDeviceCode());
        assertEquals(5, auth.getIntervalSeconds());
    }

    @Test(expected = com.github.scribejava.core.exceptions.OAuthException.class)
    public void shouldThrowExceptionWhenRequiredParamMissingInDeviceAuth() throws IOException {
        final String json = "{\"device_code\":\"dc123\"}";
        final Response response = new Response(200, "OK", Collections.emptyMap(), json);
        DeviceAuthorizationJsonExtractor.instance().extract(response);
    }
}
