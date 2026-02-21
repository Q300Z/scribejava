package com.github.scribejava.core.oauth;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for discovery services.
 */
public interface DiscoveryService {
    CompletableFuture<DiscoveredEndpoints> discoverAsync(String issuer);
}
