# 📖 Catalogue des Fonctionnalités ScribeJava v9.2+

Ce document répertorie l'ensemble des capacités exploitables par module. ScribeJava est conçu pour être **Zero-Dependency** au runtime, offrant une isolation totale et une sécurité native.

---

## ⚙️ 1. Module `scribejava-core` (Le Moteur)
C'est le socle obligatoire. Il gère le protocole OAuth 2.0 et l'infrastructure commune.

### 🛡️ Sécurité & Protocoles
- **OAuth 2.0 (RFC 6749)** : Support complet des flux *Authorization Code*, *Client Credentials*, *Password*, et *Refresh Token*.
- **PKCE (RFC 7636)** : Protection contre l'interception de code, activable via `.pkce(true)`.
- **DPoP (RFC 9449)** : Preuve de possession de jeton pour lier mathématiquement un token à un client.
- **PAR (RFC 9126)** : Envoi sécurisé des paramètres d'autorisation via POST au lieu de l'URL.

### 🛰️ Observabilité & Diagnostic
- **`OAuthLogger`** : Interface pour injecter votre propre système de logs (SLF4J, Logback, etc.).
- **`toCurlCommand()`** : Génère une commande shell pour reproduire une requête (secrets masqués auto).
- **`OAuthRetryPolicy`** : Politique de réessai automatique configurable (429, 5xx).
- **`RateLimitListener`** : Détection automatique des en-têtes de quota (`X-RateLimit-*`).
- **`OAuthNetworkException`** : Typage précis des erreurs physiques (DNS, Timeout) vs logiques.

### 🏗️ Manipulation de Données
- **`JsonBuilder`** : Génération fluide et sécurisée de JSON sans concaténation.
- **`JsonObject`** : Accès type-safe aux réponses JSON avec support des `Optional`.

---

## 🔐 2. Module `scribejava-oidc` (Identité Enterprise)
Extension native pour OpenID Connect 1.0, sans aucune dépendance externe (Zéro Nimbus).

### 🛰️ Automatisation
- **Auto-Discovery** : Configuration automatique du service via l'URL `/.well-known/openid-configuration`.
- **Metadata Caching** : Mise en cache intelligente des points de terminaison pour la performance.
- **JWKS Auto-Rotation** : Gestion et rechargement automatique des clés publiques du fournisseur.

### 🛡️ Validation & Cryptographie
- **`IdTokenValidator`** : Vérification native (RSA/EC) de la signature, de l'émetteur, de l'audience et de l'expiration.
- **Support Courbes Elliptiques** : Validation des signatures **ES256** native au JDK.
- **Protection Nonce** : Gestion intégrée contre les attaques par rejeu.

### 👤 Données Utilisateur
- **`StandardClaims`** : Accès typé aux informations profil (email, name, picture, etc.).
- **`UserInfo`** : Support de l'extraction des claims depuis le point de terminaison dédié.

---

## 🛠️ 3. Module `scribejava-integration-helpers` (Orchestration)
Outils de haut niveau pour les applications en production (Spring, Quarkus, etc.).

- **`TokenAutoRenewer`** : Rafraîchissement automatique et thread-safe des jetons expirés.
- **`AuthorizedClientService`** : Exécute des requêtes signées en gérant tout le cycle de vie (refresh + execute) de manière transparente.
- **`AuthFlowCoordinator`** : Coordonne la réception du callback, valide le `state` (CSRF) et persiste le jeton.
- **`StateGenerator`** : Génération cryptographique de paramètres de sécurité.

---

## 🔌 4. Module `scribejava-apis` (Catalogue)
Plus de **50 configurations pré-remplies** pour les services populaires :
- **Big Tech** : Google, Microsoft (Azure/Entra ID), GitHub, Facebook, LinkedIn.
- **Services** : Slack, Discord, Dropbox, Dropbox, GitLab, etc.

---

## 🚀 5. Modules de Transport (`httpclient-*`)
Choix du moteur réseau selon vos contraintes :
- **JDK (Par défaut)** : Inclus dans `core`, 100% natif, zéro dépendance.
- **OkHttp** : Pour une intégration avec l'écosystème Square.
- **Armeria** : Pour les architectures réactives (non-bloquantes) haute performance.

---

## 📚 Documentation Embarquée
- **Javadoc Interne** : Chaque JAR contient ses fichiers HTML `/apidocs`. L'aide contextuelle est disponible hors-ligne directement dans votre IDE.
- **Exemples de code** : Chaque classe majeure contient des exemples `{@code ...}` intégrés à sa Javadoc.
