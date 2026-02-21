# ScribeJava :: OpenID Connect (OIDC)

Ce module étend ScribeJava Core pour supporter le protocole OpenID Connect 1.0.

## 🛡️ Sécurité & Validation

La validation d'identité est la partie la plus critique d'OIDC. Le `IdTokenValidator` de ScribeJava effectue plusieurs vérifications rigoureuses :

### 1. Le paramètre `nonce`
Pour prévenir les attaques par rejeu, vous **devez** fournir un `nonce` lors de la requête d'autorisation et le vérifier à la réception de l'`id_token`.
```java
// 1. Générer et stocker le nonce
String nonce = UUID.randomUUID().toString();
// 2. Ajouter à l'URL d'auth
String url = service.createAuthorizationUrlBuilder().nonce(nonce).build();
// 3. Valider après le callback
IdToken idToken = service.getAccessToken(grant).getIdToken();
validator.validateNonce(idToken, nonce);
```

### 2. Multi-audience & `azp`
Si un ID Token contient plusieurs audiences, le claim `azp` (Authorized Party) est obligatoire et doit correspondre à votre `client_id`. ScribeJava l'impose automatiquement.

### 3. Signatures Cryptographiques
Nous supportons :
* **RSA** (RS256, RS384, RS512)
* **HMAC** (HS256, HS384, HS512) utilisant votre `client_secret`.

## 🚀 Fonctionnalités Clés

* **Découverte Automatique** : Utilisez `OidcDiscoveryService` pour récupérer métadonnées et JWKS.
* **Claims Standards** : Accès facile via `IdToken.getStandardClaims()`.
* **Enregistrement Dynamique** : Support de la RFC 7591 via `OidcRegistrationService`.
