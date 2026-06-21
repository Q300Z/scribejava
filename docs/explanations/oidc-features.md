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

### A. Extraction et Validation

Une fois le jeton d'accès obtenu, vous pouvez extraire et valider l'ID Token de manière native via le service en lui fournissant le nonce attendu.

```java
// ScribeJava OIDC valide et extrait l'ID Token lors du getAccessToken,
// ou vous pouvez le valider explicitement via le service avec le nonce de session :
IdToken idToken = service.validateIdToken(accessToken, sessionState.getNonce());
String rawJwt = idToken.getRawResponse(); // Le JWT brut (Base64)
```

### B. Validation Native (Signature & Claims)

Le validateur vérifie la signature (RS256/ES256), l'émetteur, l'audience et l'expiration.

```java
// Initialisation du validateur avec cache et service de découverte
IdTokenValidator validator = new IdTokenValidator(
    issuer, 
    clientId, 
    "RS256", 
    keys, 
    discoveryService, 
    jwksUri
);

// Validation manuelle complète (avec protection contre le rejeu et âge maximum)
validator.validate(idToken, new OidcNonce(expectedNonce), maxAuthAgeSeconds);
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

## 🛡️ 3. Mécanismes de Durcissement (Security Hardening)

ScribeJava v9.4+ intègre plusieurs fonctionnalités avancées de sécurisation et de résilience réseau :

### A. Cache de Clés partagé (`OidcKeyCache`)

Permet de brancher un cache partagé (ex. Redis) pour stocker les clés publiques JWKS de l'IDP et éviter les appels réseau redondants et les incohérences en cluster multi-instances.

### B. Validateur d'Émetteur Multi-Tenant (`IssuerValidator`)

Facilite la validation dynamique des identités d'entreprise multi-tenant (ex. Microsoft Entra ID, Okta) en résolvant à la volée des placeholders comme `{tenantid}` ou `{tenant}` via le claim `tid` du jeton.

### C. Gestion d'État de Session (`OidcSessionStateStore`)

Gère et corrèle de bout en bout l'état d'autorisation (`state`, `nonce` et PKCE `code_verifier`) de façon unifiée dans un stockage persistant sécurisé (ex. Redis), empêchant l'injection CSRF et les attaques par rejeu.

### D. Validation de Signature personnalisée (`SignatureVerifier`)

Permet d'étendre la couche cryptographique sous-jacente en enregistrant des algorithmes personnalisés ou en spécifiant des fournisseurs JCA (ex. HSM, Bouncy Castle).

### E. Résilience Réseau (Timeouts & Retries)

L'auto-découverte (OIDC Discovery) et l'interrogation de clés JWKS gèrent la configuration fine de timeouts et de tentatives asynchrones avec backoff exponentiel non-bloquant.

### F. Cooldown de Resolution Anti-DoS

Protège l'application et les points d'accès de l'IDP contre les attaques DoS en appliquant un cooldown de 5 minutes sur la résolution réseau de clés inconnues (`kid`) et le rechargement global du JWKS.

---

## 🛡️ 4. Sécurité Avancée

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

## 🛰️ 5. Enregistrement Dynamique (DCR)

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

## 🚪 6. Gestion de Session & Logout

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

Génerez l'iframe de déconnexion pour informer le fournisseur.

```java
String logoutIframe = OidcSessionHelper.getFrontChannelLogoutIframeHtml(
    myLogoutUri,
    issuer,
    sid
);
```

---

## 🔌 7. APIs OIDC Prêtes à l'Emploi

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
| **Auto-Rotation** | Le validateur recharge automatiquement les clés JWKS lors des rotations. |
| **Optional Access** | Les claims utilisent `java.util.Optional` pour éviter les `NullPointerException`. |
| **Durcissement Multi-Tenant** | Validation dynamic via `{tenantid}` / `{tenant}` et custom `IssuerValidator`. |
| **Résilience Réseau** | Retries avec backoff exponentiel et configuration de timeouts. |
| **Anti-DoS Cooldown** | Limite le spam de clés `kid` inexistantes avec un cooldown de 5 min. |
