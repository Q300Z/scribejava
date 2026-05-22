# 🔐 Guide OpenID Connect (OIDC) ScribeJava

Le module `scribejava-oidc` est une implémentation native et autonome du protocole OpenID Connect 1.0. Il élimine le besoin de bibliothèques tierces (comme Nimbus ou Jackson) au runtime, garantissant une isolation totale.

---

## 🏗️ 1. Configuration Dynamique (Discovery)

C'est la méthode recommandée. ScribeJava interroge le fournisseur pour obtenir automatiquement tous les points de terminaison.

```java

// Plus besoin de copier-coller les URLs !
OidcServiceBuilder builder = new OidcServiceBuilder(clientId)
    .apiSecret(clientSecret)
    .baseOnDiscovery("https://accounts.google.com", httpClient, userAgent);

OAuth20Service service = builder.build(new DefaultOidcApi20());

```

---

## 🔑 2. Gestion de l'ID Token

L'ID Token est un JWT contenant les preuves de l'authentification de l'utilisateur.

### A. Extraction et Parsing

Une fois le jeton d'accès obtenu, vous pouvez extraire l'ID Token de manière native.

```java

IdToken idToken = service.extractIdToken(accessToken);
String rawJwt = idToken.getRawResponse(); // Le JWT brut (Base64)

```

### B. Validation Native (Signature & Claims)

Le validateur vérifie la signature (RS256/ES256), l'émetteur, l'audience et l'expiration.

```java

IdTokenValidator validator = new IdTokenValidator(issuer, clientId, "RS256", keys);

// Validation complète (avec protection contre le rejeu via Nonce)
validator.validate(idToken, expectedNonce, maxAuthAge);

```

### C. Accès aux Claims (Données Utilisateur)

ScribeJava fournit un accès typé aux informations de profil standard.

```java

StandardClaims claims = idToken.getStandardClaims();

String sub = claims.getSub().orElseThrow();
String email = claims.getEmail().orElse("non-fourni");
boolean isVerified = claims.isEmailVerified().orElse(false);

```

---

## 🛡️ 3. Sécurité Avancée

### A. JAR (JWT-Secured Authorization Request)

Signez vos paramètres d'autorisation pour empêcher toute altération.

```java

OidcServiceBuilder builder = new OidcServiceBuilder(clientId)
    .jwtSecuredAuthorizationRequest(audience, privateKey, keyId, signer);

```

### B. DPoP (Proof-of-Possession)

Liez le jeton d'accès à une clé privée détenue par votre client.

```java

builder.dpop(new DefaultDPoPProofCreator(keyPair));

```

---

## 🛰️ 4. Enregistrement Dynamique (DCR)

Si votre application doit s'enregistrer à la volée auprès du fournisseur (RFC 7591).

```java

OidcRegistrationService regService = new OidcRegistrationService(httpClient, userAgent);

Map<String, Object> result = regService.registerClientAsync(
    registrationEndpoint,
    Arrays.asList("https://app.com/cb"),
    "Mon Application",
    "client_secret_post"
).get();

String newClientId = (String) result.get("client_id");

```

---

## 🚪 5. Gestion de Session & Logout

### A. Surveillance de Session

ScribeJava aide à générer l'iframe nécessaire pour détecter une déconnexion chez le fournisseur.

```java

String html = OidcSessionHelper.getSessionManagementIframeHtml(
    opCheckSessionIframeUrl,
    clientId,
    sessionState
);
// Injectez ce HTML dans une iframe masquée de votre page

```

### B. Front-Channel Logout

Générez l'iframe de déconnexion pour informer le fournisseur.

```java

String logoutIframe = OidcSessionHelper.getFrontChannelLogoutIframeHtml(
    myLogoutUri,
    issuer,
    sid
);

```

---

## 🔌 6. APIs OIDC Prêtes à l'Emploi

ScribeJava inclut des classes optimisées pour les grands fournisseurs :

- **`OidcGoogleApi20`** : Émetteur `https://accounts.google.com`.

- **`OidcMicrosoftAzureActiveDirectory20Api`** : Support multi-tenant pour Microsoft Entra ID.

- **`OidcGitHubApi20`** : Pour les intégrations OIDC de GitHub Actions.

---

## 💡 Résumé des Bénéfices OIDC

| Feature | Avantage |
| :--- | :--- |
| **Zéro Nimbus** | Pas de failles de sécurité liées à des dépendances complexes au runtime. |
| **ES256 Support** | Support natif des Courbes Elliptiques (P-256) pour une crypto plus moderne. |
| **Auto-Rotation** | Le validateur peut recharger automatiquement les clés JWKS en cas de rotation. |
| **Optional Access** | Les claims utilisent `java.util.Optional` pour éviter les `NullPointerException`. |
