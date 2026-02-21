# Journal des modifications (Changelog)

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

## [8.3.4-SNAPSHOT] - 21/02/2026

### Ajouté
- **Pattern Strategy** pour les flux OAuth 2.0 (`AuthorizationCodeGrant`, `RefreshTokenGrant`, etc.).
- **Auto-découverte OIDC** dans `ServiceBuilder` via `.discoverFromIssuer()`.
- **Hiérarchie d'exceptions riche** : `OAuthRateLimitException` et `OAuthProtocolException`.
- **Support DPoP** (Demonstrating Proof-of-Possession) pour une sécurité accrue.
- **Support PAR** (Pushed Authorization Requests) (RFC 9126).
- **Guides d'Architecture et de Contribution**.

### Modifié
- **Refactorisation SRP** : Extraction des Handlers (`Revocation`, `DeviceFlow`, `PushedAuth`) et du `RequestSigner` depuis `OAuth20Service`.
- **Modernisation des APIs** : Mise à jour de plus de 30 fournisseurs dans `scribejava-apis`.
- **Performance** : Optimisation de la suite de tests pour exécution parallèle (temps de build ~19s).

### Obsolète (Deprecated)
- Les méthodes legacy `getAccessToken(...)` et `refreshAccessToken(...)` dans `OAuth20Service` au profit du pattern Strategy.

## [8.3.3] - 2024-xx-xx
### Corrigé
- Divers correctifs dans la génération de signatures.
- Correction du mapping des claims OIDC pour Google.
