# Architecture & Responsabilités des Classes ScribeJava

Ce document décrit la structure du projet et la répartition des responsabilités, suite à la refactorisation SOLID.

## 🛠️ Core (scribejava-core)

Ce module contient le cœur du moteur OAuth.

| Composant | Rôle & Responsabilité (SRP) |
| :--- | :--- |
| **`OAuth20Service`** | **Orchestrateur principal**. Il ne contient plus de logique métier complexe mais délègue aux handlers spécialisés. Il reste le point d'entrée unique pour l'utilisateur. |
| **`ServiceBuilder`** | Construction fluide des services. Gère la configuration et l'auto-découverte (Discovery) des endpoints. |
| **`OAuth20Grant`** | **[Strategy]** Interface définissant comment construire une requête d'access token. Implémentations : `AuthorizationCodeGrant`, `PasswordGrant`, etc. |
| **`OAuth20RequestSigner`** | Gère exclusivement la signature des requêtes HTTP (Bearer) et l'ajout des preuves DPoP. |
| **`OAuth20RevocationHandler`** | Encapsule toute la logique de révocation de token (RFC 7009). |
| **`OAuth20DeviceFlowHandler`** | Gère le flux complexe "Device Authorization Grant" (RFC 8628) et le polling d'attente. |
| **`OAuth20PushedAuthHandler`** | Gère les "Pushed Authorization Requests" (PAR - RFC 9126) pour sécuriser les paramètres d'autorisation. |
| **`OAuthRequest`** | Modèle anémique représentant une requête HTTP agnostique (paramètres, headers, payload). |
| **`HttpClient`** | Abstraction du transport réseau (Bridge vers JDK, OkHttp, Armeria). |
| **`TokenExtractor`** | Interface fonctionnelle pour transformer une réponse brute (JSON/String) en objet `OAuth2AccessToken`. |

## 🛡️ OpenID Connect (scribejava-oidc)

Module d'extension pour le support de l'identité (OIDC).

| Composant | Rôle & Responsabilité (SRP) |
| :--- | :--- |
| **`OidcService`** | Extension de `OAuth20Service`. Ajoute la gestion de l'UserInfo et coordonne la validation des tokens. |
| **`IdTokenValidator`** | **Composant Critique**. Valide la sécurité des `id_token` (Signature RSA/HMAC, Audience, Issuer, Expiration, Nonce). |
| **`OidcDiscoveryService`** | Client pour le document de découverte (`/.well-known/openid-configuration`). Récupère les métadonnées et les clés JWKS. |
| **`OidcRegistrationService`** | Client pour l'enregistrement dynamique (Dynamic Client Registration). |
| **`IdToken`** | Wrapper autour du JWT brut, facilitant l'accès typé aux claims standards (sub, iss, aud, email, etc.). |

## 📐 Principes SOLID Appliqués

1.  **Single Responsibility (SRP)** : 
    *   Chaque classe a une raison unique d'exister et d'évoluer.
2.  **Open/Closed (OCP)** :
    *   Le système de `Grant` est extensible par simple ajout de classe, sans modifier le cœur du service.
3.  **Liskov Substitution (LSP)** :
    *   Toutes les implémentations de `HttpClient` sont strictement interchangeables.
4.  **Interface Segregation (ISP)** :
    *   Interfaces fines pour les extracteurs et les signataires.
5.  **Dependency Inversion (DIP)** :
    *   Injection de dépendances via le constructeur. Dépendance vers des abstractions plutôt que des implémentations.
