# Migration Guide: Moving to ScribeJava 8.3.4+ (SOLID Refactor)

ScribeJava 8.3.4 introduces a major architectural refactor to better adhere to SOLID principles. While we maintain backward compatibility for now, many methods in `OAuth20Service` are deprecated.

## 1. OAuth 2.0 Grants (Strategy Pattern)

Instead of calling specific methods for each grant type, you should now use the unified `getAccessToken(OAuth20Grant)` method.

### Authorization Code Grant
**Old way:**
```java
OAuth2AccessToken token = service.getAccessToken(code);
// or
OAuth2AccessToken token = service.getAccessToken(AccessTokenRequestParams.create(code));
```

**New way:**
```java
OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));
```

### Refresh Token Grant
**Old way:**
```java
OAuth2AccessToken token = service.refreshAccessToken(refreshToken);
```

**New way:**
```java
OAuth2AccessToken token = service.getAccessToken(new RefreshTokenGrant(refreshToken));
```

### Client Credentials Grant
**Old way:**
```java
OAuth2AccessToken token = service.getAccessTokenClientCredentialsGrant();
```

**New way:**
```java
OAuth2AccessToken token = service.getAccessToken(new ClientCredentialsGrant());
```

### Password Grant
**Old way:**
```java
OAuth2AccessToken token = service.getAccessTokenPasswordGrant(username, password);
```

**New way:**
```java
OAuth2AccessToken token = service.getAccessToken(new PasswordGrant(username, password));
```

## 2. Asynchronous Requests

The new `getAccessTokenAsync(OAuth20Grant)` method replaces all specific async variants.

**Example:**
```java
CompletableFuture<OAuth2AccessToken> future = service.getAccessTokenAsync(new AuthorizationCodeGrant(code));
```

## 3. Automatic Discovery (OIDC)

You no longer need to manually look up endpoints for OIDC providers. Use the `ServiceBuilder` discovery feature.

**New way:**
```java
OAuth20Service service = new ServiceBuilder(apiKey)
    .discoverFromIssuer("https://accounts.google.com", new OidcDiscoveryService(...))
    .build(DefaultOidcApi20.instance());
```

## 4. Error Handling

We've introduced more granular exceptions. Update your catch blocks to benefit from better error details.

*   `OAuthRateLimitException`: Thrown when the server returns a 429 status code.
*   `OAuthProtocolException`: Thrown when the server returns a malformed response or protocol violation.

**Example:**
```java
try {
    service.getAccessToken(grant);
} catch (OAuthRateLimitException e) {
    // Handle 429 specifically
} catch (OAuthProtocolException e) {
    // Handle malformed JSON/JWT
}
```

## 5. Architecture Changes

If you were extending `OAuth20Service` or manually signing requests:
*   Signature logic has moved to `OAuth20RequestSigner`.
*   Revocation logic has moved to `OAuth20RevocationHandler`.
*   Device Flow logic has moved to `OAuth20DeviceFlowHandler`.
*   PAR logic has moved to `OAuth20PushedAuthHandler`.
