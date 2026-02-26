# 🔐 Support OpenID Connect (OIDC)

Ce module fournit une implémentation complète et sécurisée du protocole OpenID Connect 1.0.

---

## 🌟 Fonctionnalités Clés

* **Auto-découverte (Discovery)** : Récupération dynamique des endpoints via `/.well-known/openid-configuration`.
* **Validation d'ID Token** : Vérification rigoureuse de la signature (RS256, etc.), de l'émetteur (`iss`), de
  l'audience (`aud`) et de l'expiration (`exp`).
* **Gestion des JWKS** : Support de la rotation des clés publiques du fournisseur.
* **UserInfo** : Récupération et parsing des claims utilisateur (email, profile, etc.).

---

## 🚀 Exemple de Flux Complet

Voici comment implémenter un flux OIDC standard (Discovery + Auth + Validation) :

```java
// 1. Initialisation avec Auto-découverte (Google par exemple)
OidcDiscoveryService discovery = new OidcDiscoveryService();
OidcProviderMetadata metadata = discovery.discover("https://accounts.google.com");

OidcService service = new OidcServiceBuilder(clientId)
    .apiSecret(clientSecret)
    .callback(callbackUrl)
    .defaultScope("openid profile email")
    .build(new DefaultOidcApi20(metadata));

// 2. Génération de l'URL d'autorisation
String authUrl = service.getAuthorizationUrl();

// 3. Échange du code contre des jetons
OpenIdOAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));

// 4. Validation de l'ID Token (Signature + Claims)
IdTokenValidator validator = new IdTokenValidator(metadata.getIssuer(), clientId);
IdToken idToken = service.extractIdToken(token);
validator.validate(idToken, nonce, System.currentTimeMillis());

System.out.println("Utilisateur authentifié : " + idToken.getSubject());
```

## 🛡️ Sécurité & OIDC

L'utilisation d'OpenID Connect nécessite souvent une sécurité renforcée :

* Consultez le guide **[Sécurité Avancée (DPoP/PAR)](../ADVANCED_SECURITY.md)** pour protéger vos jetons OIDC.
* Utilisez systématiquement le **PKCE** pour prévenir l'injection de code.

---

## 🚀 Utilisation Avancée

### Validation manuelle d'un ID Token

```java
IdTokenValidator validator = new IdTokenValidator(expectedIssuer, clientId);
IdToken idToken = IdToken.parse(rawIdToken);
validator.validate(idToken);
```

### Gestion des Claims (UserInfo)

```java
OAuthRequest request = new OAuthRequest(Verb.GET, service.getMetadata().getUserinfoEndpoint());
service.signRequest(token, request);

try (Response response = service.execute(request)) {
    UserInfoJsonExtractor extractor = UserInfoJsonExtractor.instance();
    Map<String, Object> claims = extractor.extract(response.getBody());
    System.out.println("Email : " + claims.get("email"));
}
```

---
[🏠 Accueil](../README.md) | [🔌 APIs](../scribejava-apis/README.md) | [🔐 OIDC](../scribejava-oidc/README.md) | [🛡️ Sécurité](../ADVANCED_SECURITY.md)
