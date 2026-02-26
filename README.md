# ScribeJava :: OAuth & OIDC Zero-Dependency Edition

[![Tests](https://github.com/Q300Z/scribejava/actions/workflows/maven.yml/badge.svg)](https://github.com/Q300Z/scribejava/actions)
[![Dernière Release](https://img.shields.io/github/v/release/Q300Z/scribejava)](https://github.com/Q300Z/scribejava/releases)
[![Licence MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/Q300Z/scribejava/blob/master/LICENSE.txt)

ScribeJava est la bibliothèque OAuth/OpenID Connect la plus légère, thread-safe et modulaire pour Java. Elle est conçue pour les systèmes critiques exigeant un contrôle total, une sécurité maximale et **zéro dépendance au runtime**.

---

## 🏗️ Architecture Modulaire

ScribeJava repose sur un découpage strict respectant les principes SOLID. Chaque module a une responsabilité unique :

```mermaid
graph TD
    subgraph "Application Couche"
        App[Votre App]
    end

    subgraph "ScribeJava Ecosystem"
        Builder[ServiceBuilder / OidcServiceBuilder]
        Service[OAuth20Service / OidcService]
        
        subgraph "Core Engine (scribejava-core)"
            JSON[JsonUtils / JsonBuilder]
            Request[OAuthRequest / execute]
            Retry[OAuthRetryPolicy]
            Logger[OAuthLogger]
        end

        subgraph "Extensions"
            OIDC[scribejava-oidc]
            APIs[scribejava-apis]
        end
        
        subgraph "Transport layer"
            JDK[JDK native Client]
            OkHttp[OkHttp Adapter]
            Armeria[Armeria Adapter]
        end
    end

    App --> Builder
    Builder --> Service
    Service --> Core
    Service --> OIDC
    Service --> APIs
    Core --> JDK
    Core --> OkHttp
    Core --> Armeria
```

---

## 🚀 Démarrage Rapide

### 1. OAuth 2.0 avec DX Premium
```java
// Configuration fluide
OAuth20Service service = new ServiceBuilder(clientId)
    .apiSecret(clientSecret)
    .callback("https://app.com/cb")
    .scopes("profile", "email")
    .build(GitHubApi.instance());

// Nouveau: Reproduction de bug facile (Secrets masqués par défaut)
System.out.println("Commande pour reproduire : " + request.toCurlCommand());
```

### 2. OpenID Connect "Enterprise Ready"
```java
// Auto-configuration via Discovery (Natif)
OidcServiceBuilder builder = new OidcServiceBuilder(clientId)
    .baseOnDiscovery("https://accounts.google.com", httpClient, userAgent);

OAuth20Service service = builder.build(new DefaultOidcApi20());

// Accès typé et sécurisé aux claims
IdToken idToken = service.extractIdToken(token);
String name = idToken.getStandardClaims().getGivenName().orElse("Utilisateur");
```

---

## 🛰️ Observabilité & Monitoring

ScribeJava v9.1 intègre des outils industriels pour la production :
- **Auto-Retry** : Encapsulation transparente des erreurs 429 (Rate Limit) et 5xx.
- **OAuthLogger** : Interface de logging structurée prête pour SLF4J/ELK.
- **RateLimitListener** : Surveillance proactive de vos quotas d'appels API.
- **OAuthNetworkException** : Distinction nette entre panne réseau et erreur protocolaire.

---

## 📦 Installation (Maven)

```xml
<dependency>
    <groupId>com.github.scribejava</groupId>
    <artifactId>scribejava-core</artifactId>
    <version>9.2.3</version>
</dependency>
```

---
## 💡 F.A.Q Technique

### Comment est générée l'URL d'autorisation ?
Elle est assemblée dynamiquement par le service en combinant l'URL de base du fournisseur avec les paramètres `client_id`, `redirect_uri` et `response_type`. Vous pouvez y ajouter vos propres paramètres via une `Map` ou le builder :
```java
service.createAuthorizationUrlBuilder()
       .additionalParameters(myMap)
       .build();
```

### Comment fonctionne le PKCE ?
ScribeJava génère un `code_verifier` aléatoire et calcule son `code_challenge` (SHA-256). Le challenge est envoyé dans l'URL d'autorisation, et le verifier est envoyé lors de l'échange final pour prouver que vous êtes bien l'initiateur de la requête.

### Peut-on s'authentifier uniquement via l'Issuer URL ?
**Oui.** Grâce au module OIDC, la méthode `.baseOnDiscovery(issuerUrl)` se charge de télécharger le fichier `.well-known/openid-configuration`, d'extraire les URLs d'autorisation/token et de configurer le service automatiquement sans aucune saisie manuelle d'URL.

---
⭐ **Soutenez-nous !** Mettez une étoile sur le projet pour nous aider à grandir.

