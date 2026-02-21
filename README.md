# Welcome to the home of ScribeJava, the simple OAuth client Java lib!

[![Donate](https://www.paypalobjects.com/en_US/RU/i/btn/btn_donateCC_LG.gif)](https://github.com/scribejava/scribejava/blob/master/donate.md) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.scribejava/scribejava/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.scribejava/scribejava)

ScribeJava is a simple, threadsafe and modular OAuth client library for Java.

## 🚀 Why use ScribeJava?

### Dead Simple (Modern Strategy Pattern)
Configuring ScribeJava is intuitive and extensible. Use our new Strategy pattern for cleaner code:

```java
OAuth20Service service = new ServiceBuilder(YOUR_CLIENT_ID)
    .apiSecret(YOUR_CLIENT_SECRET)
    .callback("https://app.com/callback")
    .build(LinkedInApi20.instance());

// Get token using Strategy
OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));
```

### Advanced Security & OIDC
*   **DPoP Support**: Out-of-the-box support for Demonstrating Proof-of-Possession.
*   **OpenID Connect (OIDC)**: Automatic Discovery, Dynamic Registration, and rigorous ID Token validation.
*   **Pushed Authorization Requests (PAR)**: Enhanced security for the authorization flow.

### Modular & High Performance
*   **Java 8+ Compatible**: Optimized for modern environments while maintaining compatibility.
*   **Native JDK Client**: No external dependencies required by default.
*   **Async Support**: OkHttp, Armeria, and Apache HttpClient adapters available.

## 📦 Installation

Add the core dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.scribejava</groupId>
    <artifactId>scribejava-core</artifactId>
    <version>8.3.4-SNAPSHOT</version>
</dependency>
```

## 🛠️ Features

*   **OAuth 2.0 Strategy Flow**: `AuthorizationCodeGrant`, `PasswordGrant`, `ClientCredentialsGrant`, `RefreshTokenGrant`, `DeviceCodeGrant`.
*   **OIDC Discovery**: `.discoverFromIssuer(issuerUri, discoveryService)` support in `ServiceBuilder`.
*   **Rich Exceptions**: Granular error handling with `OAuthRateLimitException` and `OAuthProtocolException`.
*   **Multi-tenant Ready**: Built for scalability and clean architecture.

## 📚 Documentation

*   **[Migration Guide](MIGRATION_GUIDE.md)** - How to upgrade from legacy versions to 8.3.4+.
*   [Architecture Overview](ARCHITECTURE.md) - Deep dive into our SOLID implementation.
*   [Contributing Guide](CONTRIBUTING.md) - Learn how to contribute and our coding standards.
*   [Supported APIs List](scribejava-apis/README.md) - List of 50+ pre-configured providers.

## ⚡ Quick Start: OpenID Connect Discovery

Skip manual endpoint configuration by using OIDC Discovery:

```java
OidcDiscoveryService discovery = new OidcDiscoveryService(issuerUri, httpClient, userAgent);

OAuth20Service service = new ServiceBuilder(clientId)
    .apiSecret(clientSecret)
    .discoverFromIssuer(issuerUri, discovery)
    .build(DefaultOidcApi20.instance());
```

## 🛡️ License
ScribeJava is released under the MIT License.
