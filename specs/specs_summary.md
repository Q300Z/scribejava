# Synthèse des Spécifications OAuth 2.0 & OpenID Connect

Ce document résume les 20 spécifications retenues pour l'implémentation du client OIDC (RP - *Relying Party*).

## I. OAuth 2.0 (12 documents)

| Spécification | Titre / Rôle         | Points Clés                                                                                         |
|:--------------|:---------------------|:----------------------------------------------------------------------------------------------------|
| **RFC 6749**  | OAuth 2.0 Framework  | Le cœur du protocole. Définit le flux **Authorization Code** (Section 4.1).                         |
| **RFC 6750**  | Bearer Token Usage   | Définit comment utiliser le jeton d'accès dans les requêtes HTTP (En-tête `Authorization: Bearer`). |
| **RFC 7009**  | Token Revocation     | Permet au client d'invalider ses jetons (Access & Refresh) lors de la déconnexion.                  |
| **RFC 7523**  | JWT Client Auth      | Permet l'authentification sécurisée du client via JWT (`private_key_jwt`).                          |
| **RFC 7636**  | PKCE                 | **Obligatoire** pour prévenir l'interception du code d'autorisation.                                |
| **RFC 7662**  | Token Introspection  | Permet au client de vérifier l'état (actif/inactif) d'un jeton auprès de l'IDP.                     |
| **RFC 8414**  | Auth Server Metadata | Définit le format JSON pour exposer les capacités du serveur (endpoints, algorithmes).              |
| **RFC 8705**  | mTLS                 | Utilisation de certificats clients pour l'authentification et la liaison de jetons.                 |
| **RFC 9101**  | JAR (JWT Request)    | Envoi sécurisé des paramètres de requête d'autorisation sous forme de JWT signé.                    |
| **RFC 9126**  | PAR (Pushed Auth)    | Envoi direct des paramètres d'autorisation côté serveur avant redirection du navigateur.            |
| **RFC 9207**  | Issuer Identifier    | Sécurise la réponse d'autorisation en y incluant l'identifiant de l'émetteur (`iss`).               |
| **RFC 9449**  | DPoP                 | Liaison cryptographique des jetons au client pour empêcher leur réutilisation en cas de vol.        |

## II. OpenID Connect (8 documents)

| Spécification        | Titre / Rôle           | Points Clés                                                                          |
|:---------------------|:-----------------------|:-------------------------------------------------------------------------------------|
| **Core 1.0**         | Spécification Centrale | Définit l'**ID Token** (JWT), le flux d'authentification et l'endpoint **UserInfo**. |
| **Discovery 1.0**    | Auto-configuration     | Permet au client de découvrir les endpoints via `/.well-known/openid-configuration`. |
| **Form Post**        | Response Mode          | Envoi de la réponse d'autorisation (code/jeton) via un formulaire POST HTML masqué.  |
| **Multi-Resp Types** | Flexibilité            | Permet de demander plusieurs types de jetons (`code id_token token`) simultanément.  |
| **Session Mgmt**     | Gestion de Session     | Surveillance de l'état de session via iframe (technique historique).                 |
| **RP-Initiated**     | Déconnexion            | Standard pour que le client demande à l'IDP de fermer la session utilisateur.        |
| **Front-Channel**    | Déconnexion HTTP       | Notification de déconnexion via le navigateur (iframe/image).                        |
| **Back-Channel**     | Déconnexion Serveur    | Notification de déconnexion directe de serveur à serveur (plus fiable).              |

---
*Date de mise à jour : 20 Février 2026*
