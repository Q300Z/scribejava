package com.github.scribejava.core.builder;

import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.DiscoveredEndpoints;
import com.github.scribejava.core.oauth.DiscoveryService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceBuilderDiscoveryTest {

    @Test
    public void shouldDiscoverAndBuildService() throws Exception {
        final String issuer = "http://idp.com";

        final DiscoveryService mockDiscovery = new DiscoveryService() {
            @Override
            public CompletableFuture<DiscoveredEndpoints> discoverAsync(String issuer) {
                return CompletableFuture.completedFuture(
                        new DiscoveredEndpoints("http://idp.com/auth", "http://idp.com/token")
                );
            }
        };

        final OAuth20Service service = new ServiceBuilder("api-key")
                .httpClient(new JDKHttpClient())
                .discoverFromIssuer(issuer, mockDiscovery)
                .build(new com.github.scribejava.core.builder.api.DefaultApi20() {
                    @Override
                    public String getAccessTokenEndpoint() {
                        return "fallback";
                    }

                    @Override
                    public String getAuthorizationBaseUrl() {
                        return "fallback";
                    }
                });

        assertThat(service.getApi().getAccessTokenEndpoint()).isEqualTo("http://idp.com/token");
        assertThat(service.getApi().getAuthorizationBaseUrl()).isEqualTo("http://idp.com/auth");
    }
}
