# ⚡ Guide de Migration : ScribeJava v8.x ➡️ v9.0

ScribeJava v9 est une version majeure qui introduit des changements de rupture (breaking changes) pour améliorer la conformité aux principes SOLID et simplifier l'utilisation des extensions modernes (OIDC, PAR, DPoP).

---

## 🚀 Migration Rapide (En 3 minutes)

Si vous utilisiez le flux standard `getAccessToken(code)`, voici le changement :

### Avant (v8.x)
```java
OAuth2AccessToken token = service.getAccessToken(code);
```

### Après (v9.0)
```java
OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));
```

---

## 🛠️ Changements Majeurs

### 1. API basée sur les "Grants" (Stratégies)
Au lieu d'avoir des méthodes surchargées pour chaque paramètre (`code`, `refreshToken`, `username/password`), nous utilisons désormais un objet `Grant` qui encapsule la logique.

*   **Authorization Code** : `new AuthorizationCodeGrant(code)`
*   **Refresh Token** : `new RefreshTokenGrant(refreshToken)`
*   **Password** : `new PasswordGrant(user, password)`
*   **Client Credentials** : `ClientCredentialsGrant.INSTANCE`

### 2. Interface de Service Segmentée (ISP)
L'interface `OAuth20Service` a été découpée pour mieux séparer les opérations synchrones des opérations asynchrones.
*   Si vous utilisez un client HTTP synchrone (ex: `JDKHttpClient`), utilisez les méthodes synchrones classiques.
*   Si vous utilisez un client asynchrone (ex: `OkHttp`), les méthodes suffixées par `Async` retournent désormais systématiquement des `CompletableFuture`.

### 3. Gestion des Exceptions
Les méthodes synchrones ne lancent plus d' `ExecutionException`. Elles déballent automatiquement l'exception pour lancer directement une `IOException` ou une `OAuthException`, simplifiant vos blocs `try-catch`.

---

## 📦 Changements de Modules

*   **Extracteurs JSON** : La hiérarchie a été simplifiée. Si vous aviez des extracteurs personnalisés, ils doivent désormais hériter de `AbstractJsonExtractor<T extends Token>`.
*   **Device Authorization** : `DeviceAuthorization` hérite désormais de `Token`. Vous pouvez donc utiliser les extracteurs standards pour traiter les réponses du serveur.

---

## 🛡️ Nouveautés Sécurité

Nous recommandons fortement de profiter de la migration pour activer :
1.  **PKCE** : `.pkce(true)` dans le `ServiceBuilder`.
2.  **DPoP** : Via le `DPoPProofCreator` (module `scribejava-oidc`).

---
[⬅️ Retour à l'accueil](./README.md)
