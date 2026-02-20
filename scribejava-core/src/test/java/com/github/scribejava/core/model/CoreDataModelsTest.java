package com.github.scribejava.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreDataModelsTest {

    @Test
    public void testOAuth2AccessToken() {
        final OAuth2AccessToken token = new OAuth2AccessToken("access", "bearer", 3600, "refresh", "scope", "raw");
        assertThat(token.getAccessToken()).isEqualTo("access");
        assertThat(token.getTokenType()).isEqualTo("bearer");
        assertThat(token.getExpiresIn()).isEqualTo(3600);
        assertThat(token.getRefreshToken()).isEqualTo("refresh");
        assertThat(token.getScope()).isEqualTo("scope");
        assertThat(token.getRawResponse()).isEqualTo("raw");

        final OAuth2AccessToken simpleToken = new OAuth2AccessToken("access");
        assertThat(simpleToken.getAccessToken()).isEqualTo("access");
        assertThat(simpleToken.getRefreshToken()).isNull();
    }

    @Test
    public void testDeviceAuthorization() {
        final DeviceAuthorization auth = new DeviceAuthorization("d_code", "u_code", "uri", 600);
        assertThat(auth.getDeviceCode()).isEqualTo("d_code");
        assertThat(auth.getUserCode()).isEqualTo("u_code");
        assertThat(auth.getVerificationUri()).isEqualTo("uri");
        assertThat(auth.getExpiresInSeconds()).isEqualTo(600);

        auth.setIntervalSeconds(10);
        assertThat(auth.getIntervalSeconds()).isEqualTo(10);

        auth.setVerificationUriComplete("uri_complete");
        assertThat(auth.getVerificationUriComplete()).isEqualTo("uri_complete");
    }

    @Test
    public void testPushedAuthorizationResponse() {
        final PushedAuthorizationResponse par = new PushedAuthorizationResponse("urn:uri", 90L, "raw_resp");
        assertThat(par.getRequestUri()).isEqualTo("urn:uri");
        assertThat(par.getExpiresIn()).isEqualTo(90L);
        assertThat(par.getRawResponse()).isEqualTo("raw_resp");
    }

    @Test
    public void testOAuth2Authorization() {
        final OAuth2Authorization auth = new OAuth2Authorization();
        auth.setCode("code123");
        auth.setState("state123");
        assertThat(auth.getCode()).isEqualTo("code123");
        assertThat(auth.getState()).isEqualTo("state123");
    }
}
