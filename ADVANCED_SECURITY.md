# 🛡️ Sécurité & Robustesse Avancée

ScribeJava v9.1+ intègre les derniers standards de sécurité de l'IETF avec une implémentation 100% native.

---

## 🔐 1. PKCE (Proof Key for Code Exchange)

Le PKCE est obligatoire pour les applications mobiles et SPAs, et fortement recommandé pour tous les clients afin de prévenir l'interception de code.

### Fonctionnement en 3 étapes :

```java
// 1. Générer le secret local (Code Verifier)
PKCE pkce = service.generatePKCE();

// 2. Envoyer le Challenge (Hash du secret) au serveur pour l'autorisation
String authUrl = service.getAuthorizationUrl(pkce, state);

// 3. Envoyer le Verifier original lors de l'échange final
AuthorizationCodeGrant grant = new AuthorizationCodeGrant(code, pkce);
OAuth2AccessToken token = service.getAccessToken(grant);
```

---

## 🔐 2. Cryptographie Native (Zéro Nimbus)

Contrairement à la v9.0, la validation des signatures ne nécessite plus de bibliothèque externe au runtime. Tout passe par `java.security`.

### Signatures Supportées
- **RS256 (RSA)** : Standard pour la plupart des ID Tokens.
- **ES256 (Elliptic Curve)** : Supporté nativement avec les courbes NIST (P-256).

---

## 🛰️ 3. OIDC Discovery : Configuration Dynamique

Ne gérez plus les URLs d'API à la main. En utilisant l'auto-découverte, votre client reste résilient aux changements d'infrastructure du fournisseur d'identité.

```java
OidcServiceBuilder builder = new OidcServiceBuilder(clientId)
    .baseOnDiscovery("https://accounts.google.com", httpClient, userAgent);
```
ScribeJava se chargera de trouver les URLs pour l'autorisation, les tokens et les clés publiques (JWKS).

---

## 🛰️ 4. PAR (Pushed Authorization Requests)

Le PAR (RFC 9126) évite de faire transiter les paramètres sensibles (client_id, scope) par l'URL du navigateur.

```java
PushedAuthorizationResponse response = service.pushAuthorizationRequestAsync(...).get();
String authUrl = response.getRequestUri();
```
