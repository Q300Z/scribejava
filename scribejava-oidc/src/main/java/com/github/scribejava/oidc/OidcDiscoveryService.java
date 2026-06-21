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

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuthService;
import com.github.scribejava.oidc.model.JwksParser;
import com.github.scribejava.oidc.model.OidcKey;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Service gérant la découverte (Discovery) OpenID Connect et la récupération des clés JWKS. */
public class OidcDiscoveryService extends OAuthService
    implements com.github.scribejava.core.oauth.DiscoveryService {

  private static final String OIDC_DISCOVERY_PATH = "/.well-known/openid-configuration";
  private static final JwksParser JWKS_PARSER = new JwksParser();

  // Cache simple pour les métadonnées
  private static final Map<String, OidcProviderMetadata> METADATA_CACHE = new ConcurrentHashMap<>();

  // Cache pour le JWKS supportant RFC 7234 et ETag
  private static class CachedJwks {
    final Map<String, OidcKey> keys;
    final String etag;
    final long expiresAt;

    CachedJwks(Map<String, OidcKey> keys, String etag, long expiresAt) {
      this.keys = keys;
      this.etag = etag;
      this.expiresAt = expiresAt;
    }
  }

  private final Map<String, CachedJwks> jwksCache = new ConcurrentHashMap<>();

  private final String issuerUri;
  private IssuerValidator issuerValidator = new DefaultIssuerValidator();

  // Network Resilience properties
  private Integer connectTimeout;
  private Integer readTimeout;
  private int maxAttempts = 3;
  private long initialDelayMs = 1000L;
  private double backoffMultiplier = 2.0;

  private static final ScheduledExecutorService SCHEDULER =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "OidcDiscoveryService-Scheduler");
            thread.setDaemon(true);
            return thread;
          });

  /**
   * @param issuerUri émetteur
   * @param httpClient client
   * @param userAgent user agent
   */
  public OidcDiscoveryService(
      final String issuerUri, final HttpClient httpClient, final String userAgent) {
    super(null, null, null, null, userAgent, null, httpClient);
    this.issuerUri = issuerUri;
  }

  @Override
  public CompletableFuture<com.github.scribejava.core.oauth.DiscoveredEndpoints> discoverAsync() {
    return getProviderMetadataAsync()
        .thenApply(
            metadata ->
                new com.github.scribejava.core.oauth.DiscoveredEndpoints(
                    metadata.getAuthorizationEndpoint(), metadata.getTokenEndpoint()));
  }

  /**
   * Métadonnées asynchrones avec support des retries et du backoff.
   *
   * @return future
   */
  public CompletableFuture<OidcProviderMetadata> getProviderMetadataAsync() {
    applyTimeouts(getHttpClientReflection());

    final OidcProviderMetadata cached = METADATA_CACHE.get(issuerUri);
    if (cached != null) {
      return CompletableFuture.completedFuture(cached);
    }

    java.util.function.Supplier<CompletableFuture<OidcProviderMetadata>> fetchSupplier =
        () -> {
          String base = issuerUri;
          if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
          }
          final String discoveryEndpoint = base + OIDC_DISCOVERY_PATH;
          final OAuthRequest request = new OAuthRequest(Verb.GET, discoveryEndpoint);
          request.addHeader("Cache-Control", "no-cache");
          request.addHeader("Pragma", "no-cache");

          return execute(
              request,
              null,
              response -> {
                try (Response resp = response) {
                  if (resp.getCode() != 200) {
                    throw new OAuthException(
                        "Failed to fetch OIDC Provider Metadata. Status: " + resp.getCode());
                  }
                  final OidcProviderMetadata metadata = OidcProviderMetadata.parse(resp.getBody());

                  if (issuerValidator != null) {
                    if (!issuerValidator.isValid(
                        issuerUri, metadata.getIssuer(), java.util.Collections.emptyMap())) {
                      throw new OAuthException(
                          "Issuer mismatch. Expected: "
                              + issuerUri
                              + ", Got: "
                              + metadata.getIssuer());
                    }
                  } else {
                    if (!isIssuerMatching(issuerUri, metadata.getIssuer())) {
                      throw new OAuthException(
                          "Issuer mismatch. Expected: "
                              + issuerUri
                              + ", Got: "
                              + metadata.getIssuer());
                    }
                  }

                  METADATA_CACHE.put(issuerUri, metadata);
                  return metadata;
                } catch (final IOException e) {
                  throw new OAuthException("Error parsing OIDC Metadata", e);
                }
              });
        };

    return retryAsync(fetchSupplier, maxAttempts, initialDelayMs, backoffMultiplier);
  }

  /** Vide le cache des métadonnées (pour tests ou rafraîchissement forcé). */
  public static void clearCache() {
    METADATA_CACHE.clear();
  }

  /**
   * Métadonnées.
   *
   * @return metadata
   * @throws ExecutionException erreur
   * @throws InterruptedException interruption
   */
  public OidcProviderMetadata getProviderMetadata()
      throws ExecutionException, InterruptedException {
    return getProviderMetadataAsync().get();
  }

  /**
   * Clés JWKS asynchrones avec support du cache RFC 7234, des ETags et des retries.
   *
   * @param jwksUri URI
   * @return future
   */
  public CompletableFuture<Map<String, OidcKey>> getJwksAsync(final String jwksUri) {
    applyTimeouts(getHttpClientReflection());

    final CachedJwks cached = jwksCache.get(jwksUri);
    final long now = System.currentTimeMillis();
    if (cached != null && cached.expiresAt > 0 && now < cached.expiresAt) {
      return CompletableFuture.completedFuture(cached.keys);
    }

    java.util.function.Supplier<CompletableFuture<Map<String, OidcKey>>> fetchSupplier =
        () -> {
          final OAuthRequest request = new OAuthRequest(Verb.GET, jwksUri);
          if (cached != null && cached.etag != null) {
            request.addHeader("If-None-Match", cached.etag);
          }

          return execute(
              request,
              null,
              response -> {
                try (Response resp = response) {
                  final int code = resp.getCode();
                  if (code == 304) {
                    if (cached != null) {
                      long newExpiresAt = parseExpiresAt(resp);
                      if (newExpiresAt <= 0) {
                        newExpiresAt = cached.expiresAt;
                      }
                      final CachedJwks updated =
                          new CachedJwks(cached.keys, cached.etag, newExpiresAt);
                      jwksCache.put(jwksUri, updated);
                      return cached.keys;
                    }
                    throw new OAuthException("Got 304 Not Modified but no cached JWKS found");
                  }
                  if (code != 200) {
                    throw new OAuthException("Failed to fetch JWKS. Status: " + code);
                  }
                  final String body = resp.getBody();
                  final Map<String, OidcKey> keys = JWKS_PARSER.parse(body);
                  final long expiresAt = parseExpiresAt(resp);
                  final String etag = resp.getHeader("ETag");
                  final CachedJwks newCached = new CachedJwks(keys, etag, expiresAt);
                  jwksCache.put(jwksUri, newCached);
                  return keys;
                } catch (final IOException e) {
                  throw new OAuthException("Error parsing JWKS", e);
                }
              });
        };

    return retryAsync(fetchSupplier, maxAttempts, initialDelayMs, backoffMultiplier);
  }

  /**
   * Clés JWKS.
   *
   * @param jwksUri URI
   * @return keys
   * @throws ExecutionException erreur
   * @throws InterruptedException interruption
   */
  public Map<String, OidcKey> getJwks(final String jwksUri)
      throws ExecutionException, InterruptedException {
    return getJwksAsync(jwksUri).get();
  }

  private long parseExpiresAt(Response response) {
    long maxAge = -1;
    final String cacheControl = response.getHeader("Cache-Control");
    if (cacheControl != null) {
      final String[] directives = cacheControl.split(",");
      for (String directive : directives) {
        directive = directive.trim().toLowerCase();
        if (directive.startsWith("max-age=")) {
          try {
            maxAge = Long.parseLong(directive.substring("max-age=".length()).trim());
          } catch (NumberFormatException e) {
            // Ignore
          }
        }
      }
    }

    if (maxAge >= 0) {
      return System.currentTimeMillis() + (maxAge * 1000);
    }

    final String expiresHeader = response.getHeader("Expires");
    if (expiresHeader != null) {
      try {
        final ZonedDateTime zonedDateTime =
            ZonedDateTime.parse(expiresHeader, DateTimeFormatter.RFC_1123_DATE_TIME);
        return zonedDateTime.toInstant().toEpochMilli();
      } catch (Exception e) {
        // Ignore parsing errors
      }
    }
    return -1;
  }

  private boolean isIssuerMatching(String expected, String got) {
    if (issuerValidator != null) {
      return issuerValidator.isValid(expected, got, java.util.Collections.emptyMap());
    }
    final String normExpected = normalizeIssuer(expected);
    final String normGot = normalizeIssuer(got);
    if (normExpected.equals(normGot)) {
      return true;
    }
    return matchMultiTenant(normExpected, normGot) || matchMultiTenant(normGot, normExpected);
  }

  private boolean matchMultiTenant(String pattern, String value) {
    if (pattern.contains("{tenantid}")
        || pattern.contains("common")
        || pattern.contains("organizations")
        || pattern.contains("consumers")) {
      String marked =
          pattern
              .replace("{tenantid}", "###TENANT###")
              .replace("common", "###TENANT###")
              .replace("organizations", "###TENANT###")
              .replace("consumers", "###TENANT###");
      String regexPattern =
          "^" + java.util.regex.Pattern.quote(marked).replace("###TENANT###", "\\E[^/]+\\Q") + "$";
      return value.matches(regexPattern);
    }
    return false;
  }

  private String normalizeIssuer(String url) {
    if (url == null) {
      return "";
    }
    String normalized = url.trim();
    while (normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  // Network timeouts reflection
  private HttpClient getHttpClientReflection() {
    try {
      java.lang.reflect.Field clientField = OAuthService.class.getDeclaredField("httpClient");
      clientField.setAccessible(true);
      return (HttpClient) clientField.get(this);
    } catch (Exception e) {
      return null;
    }
  }

  private void applyTimeouts(HttpClient client) {
    if (client instanceof JDKHttpClient) {
      try {
        java.lang.reflect.Field configField = JDKHttpClient.class.getDeclaredField("config");
        configField.setAccessible(true);
        final JDKHttpClientConfig clientConfig = (JDKHttpClientConfig) configField.get(client);
        if (clientConfig != null) {
          if (connectTimeout != null) {
            clientConfig.setConnectTimeout(connectTimeout);
          }
          if (readTimeout != null) {
            clientConfig.setReadTimeout(readTimeout);
          }
        }
      } catch (Exception e) {
        // Ignore
      }
    }
  }

  // Non-blocking retry helper
  private <T> CompletableFuture<T> retryAsync(
      java.util.function.Supplier<CompletableFuture<T>> action,
      int maxAttempts,
      long initialDelayMs,
      double multiplier) {
    return retryAsyncInternal(action, 1, maxAttempts, initialDelayMs, multiplier);
  }

  private <T> CompletableFuture<T> retryAsyncInternal(
      java.util.function.Supplier<CompletableFuture<T>> action,
      int currentAttempt,
      int maxAttempts,
      long delayMs,
      double multiplier) {
    return action
        .get()
        .handle(
            (result, error) -> {
              if (error == null) {
                return CompletableFuture.completedFuture(result);
              }
              if (currentAttempt >= maxAttempts) {
                final CompletableFuture<T> failed = new CompletableFuture<>();
                failed.completeExceptionally(error);
                return failed;
              }
              final CompletableFuture<T> retryFuture = new CompletableFuture<>();
              SCHEDULER.schedule(
                  () -> {
                    retryAsyncInternal(
                            action,
                            currentAttempt + 1,
                            maxAttempts,
                            (long) (delayMs * multiplier),
                            multiplier)
                        .whenComplete(
                            (r, e) -> {
                              if (e != null) {
                                retryFuture.completeExceptionally(e);
                              } else {
                                retryFuture.complete(r);
                              }
                            });
                  },
                  delayMs,
                  TimeUnit.MILLISECONDS);
              return retryFuture;
            })
        .thenCompose(f -> f);
  }

  // Getters/setters for settings
  /**
   * Gets the issuer validator.
   *
   * @return the {@link IssuerValidator}
   */
  public IssuerValidator getIssuerValidator() {
    return issuerValidator;
  }

  /**
   * Sets the issuer validator.
   *
   * @param issuerValidator the {@link IssuerValidator} to use
   */
  public void setIssuerValidator(IssuerValidator issuerValidator) {
    this.issuerValidator = issuerValidator;
  }

  /**
   * Gets the connection timeout in milliseconds.
   *
   * @return the connection timeout, or {@code null} if default is used
   */
  public Integer getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Sets the connection timeout in milliseconds.
   *
   * @param connectTimeout the connection timeout, or {@code null} to use default
   */
  public void setConnectTimeout(Integer connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  /**
   * Gets the read timeout in milliseconds.
   *
   * @return the read timeout, or {@code null} if default is used
   */
  public Integer getReadTimeout() {
    return readTimeout;
  }

  /**
   * Sets the read timeout in milliseconds.
   *
   * @param readTimeout the read timeout, or {@code null} to use default
   */
  public void setReadTimeout(Integer readTimeout) {
    this.readTimeout = readTimeout;
  }

  /**
   * Gets the maximum number of network retry attempts.
   *
   * @return the maximum number of retry attempts
   */
  public int getMaxAttempts() {
    return maxAttempts;
  }

  /**
   * Sets the maximum number of network retry attempts.
   *
   * @param maxAttempts the maximum number of retry attempts
   */
  public void setMaxAttempts(int maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  /**
   * Gets the initial delay in milliseconds for retrying network operations.
   *
   * @return the initial delay in milliseconds
   */
  public long getInitialDelayMs() {
    return initialDelayMs;
  }

  /**
   * Sets the initial delay in milliseconds for retrying network operations.
   *
   * @param initialDelayMs the initial delay in milliseconds
   */
  public void setInitialDelayMs(long initialDelayMs) {
    this.initialDelayMs = initialDelayMs;
  }

  /**
   * Gets the backoff multiplier used for calculated retry delay.
   *
   * @return the backoff multiplier
   */
  public double getBackoffMultiplier() {
    return backoffMultiplier;
  }

  /**
   * Sets the backoff multiplier used for calculated retry delay.
   *
   * @param backoffMultiplier the backoff multiplier
   */
  public void setBackoffMultiplier(double backoffMultiplier) {
    this.backoffMultiplier = backoffMultiplier;
  }

  /** Clears the JWKS cache of this instance (mainly used for testing). */
  public void clearJwksCache() {
    jwksCache.clear();
  }
}
