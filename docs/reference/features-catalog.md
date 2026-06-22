# 📖 Catalogue des Fonctionnalités ScribeJava v9.4+

Ce document répertorie l'ensemble des capacités exploitables par module. ScribeJava est conçu pour être **Zero-Dependency** au runtime, offrant une isolation totale et une sécurité native.

---

## ⚙️ 1. Module `scribejava-core` (Le Moteur)

### 🔗 Construction de l'URL d'autorisation

C'est le point de départ de tout flux utilisateur. ScribeJava offre une flexibilité totale :

- **Simple** : `service.createAuthorizationUrlBuilder().build()` génère l'URL standard.

- **Paramètres personnalisés** : Ajoutez des paramètres spécifiques au fournisseur (ex: `prompt`, `access_type`) via une `Map<String, String>`.

  ```java
  Map<String, String> params = new HashMap<>();
  params.put("prompt", "select_account");
  String url = service.createAuthorizationUrlBuilder()
      .additionalParams(params)
      .build();
  ```

- **Builder Fluide** : Utilisez `service.createAuthorizationUrlBuilder()` pour chaîner les options (`state`, `scope`, `additionalParams`).

### 🛡️ Sécurité & PKCE (RFC 7636)

Le PKCE est géré de manière transparente pour sécuriser les échanges :

1. **Génération & Autorisation** : Activez PKCE via `initPKCE()` sur l' `AuthorizationUrlBuilder` lors de la construction de l'URL :
   ```java
   AuthorizationUrlBuilder builder = service.createAuthorizationUrlBuilder()
       .initPKCE();
   String url = builder.build();
   PKCE pkce = builder.getPkce(); // Récupération de l'objet PKCE généré contenant le code_verifier
   ```

2. **Échange** : Passez le code_verifier à l' `AuthorizationCodeGrant` :
   ```java
   AuthorizationCodeGrant grant = new AuthorizationCodeGrant(code);
   grant.setPkceCodeVerifier(pkce.getCodeVerifier());
   OAuth2AccessToken token = service.getAccessToken(grant);
   ```

---

## 🔐 2. Module `scribejava-oidc` (Identité Enterprise)

### 🛰️ Automatisation via OIDC Discovery

La fonctionnalité la plus puissante pour les administrateurs système.

- **Concept** : Fournissez uniquement l'URL de l'émetteur (Issuer), ex: `https://accounts.google.com`.

- **Action** : Le `OidcServiceBuilder` télécharge le JSON `openid-configuration`, découvre les endpoints et configure le service automatiquement.

- **Avantage** : Aucune URL d'API n'est écrite en dur dans votre code. Si le fournisseur déplace ses services, votre application s'adapte dynamiquement.

  ```java
  builder.baseOnDiscovery(issuerUri, httpClient, userAgent);
  ```

### 🛡️ Validation, Cryptographie & Durcissement (Hardening)

- **`IdTokenValidator`** : Vérification native (RSA/EC) sans dépendances externes.
- **Support Courbes Elliptiques** : Validation des signatures **ES256** (NIST P-256).
- **`OidcKeyCache`** : Cache extensible et personnalisable pour le stockage des clés publiques JWKS (avec implémentation `DefaultOidcKeyCache` en mémoire vive, extensible vers Redis ou Base de données).
- **`IssuerValidator`** : Abstraction de validation de l'émetteur (`iss`), gérant de manière dynamique les IDPs multi-tenant (ex. Entra ID, Okta) sans correspondances rigides dans le code source via `DefaultIssuerValidator`.
- **`OidcSessionStateStore`** : Magasin de gestion centralisée du cycle de vie de la session OIDC (`state`, `nonce`, PKCE `code_verifier`) pour automatiser la corrélation et bloquer les injections CSRF.
- **`SignatureVerifier`** : Interface d'extension cryptographique permettant la configuration de Providers JCA (ex: HSM, Bouncy Castle) et l'enregistrement d'algorithmes de signature spécifiques.
- **Résilience Réseau (Timeouts & Retries)** : Gestion paramétrable des timeouts HTTP et boucles de retries non-bloquantes avec backoff exponentiel pour l'interrogation de la découverte et du JWKS.
- **Limitation de Débit (Anti-DoS)** : Cooldown global et individuel par clé inconnue (`kid`) de 5 minutes pour prémunir l'application contre les attaques DoS ciblant le JWKS.

---

## 🛠️ 3. Module `scribejava-integration-helpers` (Orchestration)

- **`TokenAutoRenewer`** : Rafraîchissement automatique et thread-safe.

- **`AuthorizedClientService`** : Exécute des requêtes signées en gérant tout le cycle de vie.
