# 🗝️ Support OAuth 1.0a

Bien que plus ancien, le protocole OAuth 1.0a reste utilisé par des services majeurs comme Twitter (API v1.1) ou Trello. Ce module fournit une implémentation robuste et thread-safe.

---

## 🚀 Utilisation (Cycle en 3 étapes)

### 1. Obtenir un "Request Token"
```java
OAuth10aService service = new ServiceBuilder(apiKey)
    .apiSecret(apiSecret)
    .build(TwitterApi.instance());

OAuth1RequestToken requestToken = service.getRequestToken();
```

### 2. Autorisation
Redirigez l'utilisateur vers :
```java
String authUrl = service.getAuthorizationUrl(requestToken);
```

### 3. Échange contre un "Access Token"
```java
OAuth1AccessToken accessToken = service.getAccessToken(requestToken, oauthVerifier);
```

---

## 🏗️ Signature des requêtes
Le module gère automatiquement la création complexe du header `Authorization` (incluant `nonce`, `timestamp` et `signature`).

```java
OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.twitter.com/1.1/account/verify_credentials.json");
service.signRequest(accessToken, request);
```

---
[🏠 Accueil](../README.md) | [🔌 APIs](../scribejava-apis/README.md) | [🔐 OIDC](../scribejava-oidc/README.md) | [🛡️ Sécurité](../ADVANCED_SECURITY.md)
