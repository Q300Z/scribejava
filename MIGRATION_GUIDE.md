# ⚡ Guide de Migration ScribeJava

## v9.0 ➡️ v9.1 (OIDC Enterprise & Autonomie)

### 🛡️ Autonomie OIDC (Zéro Dépendance Runtime)
Le module `scribejava-oidc` ne dépend plus de Nimbus à l'exécution. Les types ont été simplifiés pour utiliser le JDK.

#### Validation des jetons
L'API de `IdTokenValidator` a été modifiée pour être native.

**Ancien code (v9.0) :**
```java
// Nécessitait des classes Nimbus (ClientID, JWKSet, Nonce)
IdTokenValidator validator = new IdTokenValidator(issuer, new ClientID(clientId), JWSAlgorithm.RS256, jwkSet);
validator.validate(token, new Nonce(expectedNonce), 0);
```

**Nouveau code (v9.1) :**
```java
// Utilise des String et des types natifs ScribeJava (OidcKey, OidcNonce)
Map<String, OidcKey> keys = discovery.getJwks(jwksUri);
IdTokenValidator validator = new IdTokenValidator(issuer, clientId, "RS256", keys);
validator.validate(token, new OidcNonce(expectedNonce), 0);
```

### 🤖 Migration vers le Coordinateur Automatisé
Si vous gériez manuellement les sessions et les validations :

**Avant (Manuel) :**
```java
String state = (String) session.getAttribute("state");
if (!state.equals(paramState)) throw new SecurityException();
OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));
// ... validation manuelle du JWT
```

**Après (Automatisé avec v9.1) :**
```java
OidcAuthFlowCoordinator coordinator = new OidcAuthFlowCoordinator(oidcService, repository);
OidcAuthResult result = coordinator.finishAuthorization(userId, code, paramState, sessionContext);
// Tout est validé (State, PKCE, Nonce, JWT).
```

---

## v8.x ➡️ v9.0 (Version majeure)

ScribeJava v9 introduit des changements de rupture pour améliorer la conformité aux principes SOLID.

### 🚀 Changement Rapide
**Avant (v8.x) :**
```java
OAuth2AccessToken token = service.getAccessToken(code);
```
**Après (v9.0) :**
```java
OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));
```

### 🛠️ Changements Architecturaux
1.  **API basée sur les "Grants"** : Utilisez `AuthorizationCodeGrant`, `RefreshTokenGrant`, `PasswordGrant` ou `ClientCredentialsGrant`.
2.  **Interface de Service Segmentée** : Meilleure séparation synchrone / asynchrone (CompletableFuture).
3.  **Gestion des Exceptions** : Les méthodes synchrones déballent automatiquement les `ExecutionException`.

---
[⬅️ Retour à l'accueil](./README.md)
