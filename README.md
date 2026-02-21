# Bienvenue sur ScribeJava, la bibliothèque OAuth simple pour Java !

[![Faire un don](https://www.paypalobjects.com/en_US/RU/i/btn/btn_donateCC_LG.gif)](https://github.com/scribejava/scribejava/blob/master/donate.md) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.scribejava/scribejava/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.scribejava/scribejava)

ScribeJava est une bibliothèque client OAuth simple, thread-safe et modulaire pour Java.

## 🚀 Pourquoi utiliser ScribeJava ?

### Simplicité (Pattern Strategy Moderne)
La configuration de ScribeJava est intuitive et extensible. Utilisez notre nouveau pattern Strategy pour un code plus propre :

```java
OAuth20Service service = new ServiceBuilder(VOTRE_CLIENT_ID)
    .apiSecret(VOTRE_CLIENT_SECRET)
    .callback("https://app.com/callback")
    .build(LinkedInApi20.instance());

// Récupération du token via Strategy
OAuth2AccessToken token = service.getAccessToken(new AuthorizationCodeGrant(code));
```

### Sécurité Avancée & OIDC
*   **Support DPoP** : Support natif pour "Demonstrating Proof-of-Possession".
*   **OpenID Connect (OIDC)** : Découverte automatique (Discovery), enregistrement dynamique et validation rigoureuse des ID Tokens.
*   **Pushed Authorization Requests (PAR)** : Sécurité renforcée du flux d'autorisation.

### Modulaire & Haute Performance
*   **Compatible Java 8+** : Optimisé pour les environnements modernes tout en maintenant la compatibilité.
*   **Client JDK Natif** : Aucune dépendance externe requise par défaut.
*   **Support Asynchrone** : Adaptateurs disponibles pour OkHttp, Armeria et Apache HttpClient.

## 📦 Installation

ScribeJava est distribué exclusivement via **[GitHub Releases](https://github.com/Q300Z/scribejava/releases)**. Comme la bibliothèque n'est pas publiée sur un dépôt public (Maven Central), vous devez installer les JARs manuellement.

### 1. Téléchargement
Récupérez les fichiers `.jar` de la dernière version sur la page des [Releases](https://github.com/Q300Z/scribejava/releases).

*   **scribejava-core.jar** (Obligatoire) : Le cœur du moteur.
*   **scribejava-oidc.jar** (Optionnel) : Pour le support OpenID Connect.
*   **scribejava-apis.jar** (Optionnel) : Pour les configurations fournisseurs (Google, GitHub, etc.).

### 2. Installation locale (Recommandé pour Maven)
Pour utiliser ces JARs dans un projet Maven, installez-les dans votre répertoire local `.m2` :

```bash
mvn install:install-file -Dfile=scribejava-core-9.0.0.jar -DgroupId=com.github.scribejava -DartifactId=scribejava-core -Dversion=9.0.0 -Dpackaging=jar
```

Vous pourrez ensuite les déclarer normalement dans votre `pom.xml`.

## 🛠️ Fonctionnalités

*   **Flux OAuth 2.0 via Strategy** : `AuthorizationCodeGrant`, `PasswordGrant`, `ClientCredentialsGrant`, `RefreshTokenGrant`, `DeviceCodeGrant`.
*   **OIDC Discovery** : Support de `.discoverFromIssuer(issuerUri, discoveryService)` dans le `ServiceBuilder`.
*   **Exceptions Riches** : Gestion d'erreurs granulaire avec `OAuthRateLimitException` et `OAuthProtocolException`.
*   **Prêt pour le Multi-tenant** : Conçu pour la scalabilité et une architecture propre.

## 📚 Documentation

*   **[Guide de Migration](MIGRATION_GUIDE.md)** - Comment passer des versions legacy à la v9+.
*   **[Guide du Contributeur](CONTRIBUTING.md)** - Architecture, Sécurité, Dépannage et Extensibilité.
*   **[Processus de Release](RELEASES.md)** - Comment sont gérées les versions sur GitHub.
*   **[Liste des APIs](scribejava-apis/README.md)** - Fournisseurs pré-configurés.

## ⚡ Démarrage Rapide : Découverte OpenID Connect

Évitez la configuration manuelle des endpoints en utilisant l'auto-découverte OIDC :

```java
OidcDiscoveryService discovery = new OidcDiscoveryService(issuerUri, httpClient, userAgent);

OAuth20Service service = new ServiceBuilder(clientId)
    .apiSecret(clientSecret)
    .discoverFromIssuer(issuerUri, discovery)
    .build(DefaultOidcApi20.instance());
```

## 🛡️ Licence
ScribeJava est publié sous licence MIT.
