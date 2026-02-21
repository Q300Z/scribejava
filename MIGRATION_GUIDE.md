# Guide de Migration : Passage à ScribeJava 9.0.0+ (Refactorisation SOLID)

ScribeJava 9.0.0 introduit une refonte architecturale majeure pour mieux adhérer aux principes SOLID. Bien que nous maintenions la compatibilité ascendante pour le moment, de nombreuses méthodes dans `OAuth20Service` sont désormais obsolètes (deprecated).

## 1. OAuth 2.0 Grants (Pattern Strategy)

Au lieu d'appeler des méthodes spécifiques pour chaque type de flux, vous devez maintenant utiliser la méthode unifiée `getAccessToken(OAuth20Grant)`.

### Flux "Authorization Code"
**Ancienne méthode :**
```java
OAuth2AccessToken token = service.getAccessToken(code);
// ou
OAuth2AccessToken token = service.getAccessToken(AccessTokenRequestParams.create(code));
```

**Nouvelle méthode :**
```java
OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));
```

### Flux "Refresh Token"
**Ancienne méthode :**
```java
OAuth2AccessToken token = service.refreshAccessToken(refreshToken);
```

**Nouvelle méthode :**
```java
OAuth2AccessToken token = service.getAccessToken(new RefreshTokenGrant(refreshToken));
```

### Flux "Client Credentials"
**Ancienne méthode :**
```java
OAuth2AccessToken token = service.getAccessTokenClientCredentialsGrant();
```

**Nouvelle méthode :**
```java
OAuth2AccessToken token = service.getAccessToken(new ClientCredentialsGrant());
```

### Flux "Password"
**Ancienne méthode :**
```java
OAuth2AccessToken token = service.getAccessTokenPasswordGrant(username, password);
```

**Nouvelle méthode :**
```java
OAuth2AccessToken token = service.getAccessToken(new PasswordGrant(username, password));
```

## 2. Requêtes Asynchrones

La nouvelle méthode `getAccessTokenAsync(OAuth20Grant)` remplace toutes les variantes asynchrones spécifiques.

**Exemple :**
```java
CompletableFuture<OAuth2AccessToken> future = service.getAccessTokenAsync(new AuthorizationCodeGrant(code));
```

## 3. Découverte Automatique (OIDC)

Vous n'avez plus besoin de rechercher manuellement les endpoints pour les fournisseurs OIDC. Utilisez la fonction de découverte du `ServiceBuilder`.

**Nouvelle méthode :**
```java
OAuth20Service service = new ServiceBuilder(apiKey)
    .discoverFromIssuer("https://accounts.google.com", new OidcDiscoveryService(...))
    .build(DefaultOidcApi20.instance());
```

## 4. Gestion des Erreurs

Nous avons introduit des exceptions plus granulaires. Mettez à jour vos blocs `catch` pour bénéficier de détails d'erreur plus précis.

*   `OAuthRateLimitException` : Lancée quand le serveur retourne un code 429.
*   `OAuthProtocolException` : Lancée quand le serveur retourne une réponse malformée ou une violation du protocole.

**Exemple :**
```java
try {
    service.getAccessToken(grant);
} catch (OAuthRateLimitException e) {
    // Gérer spécifiquement le 429 (trop de requêtes)
} catch (OAuthProtocolException e) {
    // Gérer un JSON ou un JWT malformé
}
```

## 5. Changements Architecturaux

Si vous étendiez `OAuth20Service` ou signiez manuellement les requêtes :
*   La logique de signature a été déplacée dans `OAuth20RequestSigner`.
*   La logique de révocation a été déplacée dans `OAuth20RevocationHandler`.
*   La logique du Device Flow a été déplacée dans `OAuth20DeviceFlowHandler`.
*   La logique PAR a été déplacée dans `OAuth20PushedAuthHandler`.

## 6. Configuration du Client HTTP

Vous pouvez désormais affiner les timeouts et les paramètres de proxy via `HttpClientConfig`.

**Exemple avec le client JDK :**
```java
JDKHttpClientConfig config = JDKHttpClientConfig.defaultConfig();
config.setConnectTimeout(5000);
config.setReadTimeout(5000);

OAuth20Service service = new ServiceBuilder(apiKey)
    .httpClientConfig(config)
    .build(api);
```

---
*Note : Pour une référence complète de l'API, vous pouvez générer la Javadoc localement via `mvn javadoc:aggregate`.*
