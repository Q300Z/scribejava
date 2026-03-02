# 📖 Catalogue des Fonctionnalités ScribeJava v9.2+

Ce document répertorie l'ensemble des capacités exploitables par module. ScribeJava est conçu pour être **Zero-Dependency** au runtime, offrant une isolation totale et une sécurité native.

---

## ⚙️ 1. Module `scribejava-core` (Le Moteur)

### 🔗 Construction de l'URL d'autorisation

C'est le point de départ de tout flux utilisateur. ScribeJava offre une flexibilité totale :

- **Simple** : `service.getAuthorizationUrl()` génère l'URL standard.

- **Paramètres personnalisés** : Ajoutez des paramètres spécifiques au fournisseur (ex: `prompt`, `access_type`) via une `Map<String, String>`.
  ```java
  Map<String, String> params = new HashMap<>();
  params.put("prompt", "select_account");
  String url = service.getAuthorizationUrl(params);

  ```

- **Builder Fluide** : Utilisez `service.createAuthorizationUrlBuilder()` pour chaîner les options (`state`, `scopes`, `additionalParameters`).

### 🛡️ Sécurité & PKCE (RFC 7636)

Le PKCE est géré de manière transparente pour sécuriser les échanges :

1. **Génération** : `PKCE pkce = service.generatePKCE()` crée le `code_verifier` (secret) et le `code_challenge`.

2. **Autorisation** : Passez l'objet `pkce` à `getAuthorizationUrl()`. ScribeJava inclut automatiquement le challenge dans l'URL.

3. **Échange** : Passez le même objet à `AuthorizationCodeGrant`. ScribeJava envoie le verifier au serveur pour prouver l'identité du client.

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

### 🛡️ Validation & Cryptographie

- **`IdTokenValidator`** : Vérification native (RSA/EC) sans dépendances externes.

- **Support Courbes Elliptiques** : Validation des signatures **ES256** (NIST P-256).

---

## 🛠️ 3. Module `scribejava-integration-helpers` (Orchestration)

- **`TokenAutoRenewer`** : Rafraîchissement automatique et thread-safe.

- **`AuthorizedClientService`** : Exécute des requêtes signées en gérant tout le cycle de vie.
