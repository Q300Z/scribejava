# Journal des modifications (Changelog)

Toutes les modifications notables de ce projet à partir de la version 9.0.0 sont documentées dans ce fichier.

## [9.0.0] - 21/02/2026

Cette version marque une transition majeure vers une architecture strictement **SOLID** et l'abandon des méthodes monolithiques au profit de composants spécialisés.

### Ajouté
- **Architecture SOLID** : Refonte complète de `OAuth20Service`.
- **Pattern Strategy** : Introduction de `OAuth20Grant` pour tous les flux (Authorization Code, Refresh Token, Client Credentials, Password, Device Code).
- **Handlers Spécialisés** :
    - `OAuth20RevocationHandler` (Révocation de token).
    - `OAuth20DeviceFlowHandler` (Flux pour appareils).
    - `OAuth20PushedAuthHandler` (PAR - Pushed Authorization Requests).
- **Signature & Sécurité** :
    - `OAuth20RequestSigner` pour la signature Bearer et le support **DPoP**.
- **Auto-découverte OIDC** : Support natif de l'OIDC Discovery dans le `ServiceBuilder`.
- **Typage des erreurs** : Nouvelles exceptions `OAuthRateLimitException` et `OAuthProtocolException`.
- **Qualité & Tests** : Intégration de **PITest** pour le Mutation Testing.
- **Documentation Javadoc** : Couverture à 100% sur les classes et méthodes publiques.

### Modifié
- **Performance** : Suite de tests optimisée pour une exécution parallèle (~19s).
- **Modernisation** : Mise à jour exhaustive des 50+ fournisseurs dans `scribejava-apis`.

### Obsolète (Deprecated)
- Toutes les méthodes `getAccessToken` et `refreshAccessToken` spécifiques de `OAuth20Service` (utiliser `getAccessToken(OAuth20Grant)`).
