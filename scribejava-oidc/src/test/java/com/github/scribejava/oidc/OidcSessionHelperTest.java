package com.github.scribejava.oidc;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OidcSessionHelperTest {

    @Test
    public void shouldGenerateSessionManagementHtml() {
        final String html = OidcSessionHelper.getSessionManagementIframeHtml(
                "https://idp.com/check-session",
                "client-123",
                "session-state-abc"
        );

        assertTrue(html.contains("https://idp.com/check-session"));
        assertTrue(html.contains("client-123"));
        assertTrue(html.contains("session-state-abc"));
        assertTrue(html.contains("postMessage"));
    }

    @Test
    public void shouldGenerateFrontChannelLogoutHtml() {
        final String html = OidcSessionHelper.getFrontChannelLogoutIframeHtml(
                "https://client.com/logout",
                "https://idp.com",
                "sid-123"
        );

        assertTrue(html.contains("<iframe src=\"https://client.com/logout?iss=https%3A%2F%2Fidp.com&sid=sid-123\""));
        assertTrue(html.contains("style=\"display:none\""));
    }
}
