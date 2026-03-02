# 🛠️ Guide d'Intégration : ScribeJava Helpers

Le module `scribejava-integration-helpers` fournit une couche d'orchestration de haut niveau. Il automatise les tâches complexes (rafraîchissement, synchronisation, validation OIDC) pour permettre aux développeurs de se concentrer sur la logique métier.

---

## 🏗️ Architecture Opérationnelle

Le module est structuré autour de quatre piliers :
1. **Sécurisation du Flux** : Gestion du `state`, du `nonce` et du `PKCE`.
2. **Coordination du Callback** : Validation et échange automatique du code.
3. **Gestion de Session** : Persistance et calcul d'expiration des jetons.
4. **Exécution Transparente** : Appels API avec auto-refresh thread-safe.

---

## 1. Sécurisation du Flux d'Autorisation

### Préparation de la Redirection (`AuthSessionContext`)
Avant d'envoyer l'utilisateur vers le fournisseur, vous devez générer des secrets pour protéger la session.

```java
StateGenerator gen = new StateGenerator();
String state = gen.generate();
String nonce = gen.generate();
PKCE pkce = service.generatePKCE();

// Cet objet doit être persisté temporairement (ex: Session HTTP ou Cookie chiffré)
AuthSessionContext context = new AuthSessionContext(state, nonce, pkce);
saveInSession(context);

String authUrl = service.getAuthorizationUrl(pkce, state);
```

---

## 2. Traitement du Callback (`AuthFlowCoordinator`)

Le coordinateur automatise la validation de sécurité et l'obtention du jeton final.

### Version OIDC (Recommandée)
L' `OidcAuthFlowCoordinator` effectue des vérifications critiques :
- Validation du `state` (Anti-CSRF).
- Validation de l'ID Token (Signature, émetteur, audience).
- Validation du `nonce` (Anti-rejeu).
- **Fallback UserInfo** : Si l'email est manquant dans l'ID Token, il interroge automatiquement l'API UserInfo.

```java
OidcAuthFlowCoordinator<String> coordinator = new OidcAuthFlowCoordinator<>(oidcService, tokenRepository);
coordinator.setListener(myListener); // Pour l'audit

OidcAuthResult result = coordinator.finishAuthorization(
    userId, 
    codeFromRequest, 
    stateFromRequest, 
    savedContextFromSession
);

StandardClaims user = result.getUserInfoClaims();
```

---

## 3. Gestion Automatisée des Jetons

### Persistance (`TokenRepository`)
ScribeJava fournit l'interface, vous fournissez l'implémentation (Redis, JDBC, JPA, etc.).

```java
public class MyRepo implements TokenRepository<String, ExpiringTokenWrapper> {
    // findByKey, save, deleteByKey...
}
```

### Rafraîchissement Thread-Safe (`TokenAutoRenewer`)
Le `TokenAutoRenewer` est conçu pour les environnements à forte concurrence. 
- **Verrouillage Intelligent** : Si 10 requêtes concurrentes détectent que le jeton est expiré, **un seul appel réseau** de rafraîchissement est fait. Les 9 autres attendent et réutilisent le nouveau jeton.
- **Buffer d'Expiration** : Par défaut, il rafraîchit le jeton s'il expire dans moins de 60 secondes pour éviter les échecs en plein milieu d'une requête.
- **Sérialisation** : Les jetons et leurs wrappers implémentent `Serializable` pour être stockés en base de données ou en session.

```java
TokenAutoRenewer<String> renewer = new TokenAutoRenewer<>(
    repository,
    oldToken -> service.refreshAccessToken(oldToken.getRefreshToken())
);
```

### Cache de Découverte Persistant (`DiskOidcDiscoveryCache`)
Pour survivre aux redémarrages serveur sans surcharger l'émetteur (IdP), utilisez le cache sur disque.

```java
File cacheFile = new File("oidc-discovery-cache.json");
// TTL de 24h par défaut
DiskOidcDiscoveryCache diskCache = new DiskOidcDiscoveryCache(cacheFile);

// Lors du login :
OidcProviderMetadata metadata = diskCache.getMetadata("google", discoveryService);
```

### Exécution du Client (`AuthorizedClientService`)
C'est l'outil ultime pour le développeur. Il masque toute la complexité d'OAuth.

```java
AuthorizedClientService<String> client = new AuthorizedClientService<>(service, renewer);

// Vous ne vous souciez plus de rien :
// Le service récupère le jeton, le rafraîchit si besoin, signe la requête et l'envoie.
Response resp = client.execute(userId, new OAuthRequest(Verb.GET, "https://api.github.com/user"));
```

---

## 4. Multi-Tenant (`OAuthServiceRegistry`)

Pour les applications supportant plusieurs méthodes de connexion (Google + GitHub + Azure).

```java
OAuthServiceRegistry<String> registry = new OAuthServiceRegistry<>();
registry.register("google", googleClientService);
registry.register("github", githubClientService);

// Récupération facile par ID
AuthorizedClientService<String> srv = registry.getService("google");
```

---

## 📈 5. Monitoring & Audit (`AuthEventListener`)

Indispensable pour la conformité et le débogage en production.

```java
public class MyAuditListener implements AuthEventListener<String> {
    @Override
    public void onTokenRefreshed(String key, ExpiringTokenWrapper newToken) {
        // Loggez le renouvellement pour les stats
    }

    @Override
    public void onCsrfDetected(String key, String got, String expected) {
        // Alerte critique : tentative d'attaque
    }
}
```

---

## 💡 Résumé Technologique
| Composant | Rôle |
| :--- | :--- |
| **`ExpiringTokenWrapper`** | Calcule l'instant `T` d'expiration dès réception du jeton. |
| **`StateGenerator`** | Utilise `SecureRandom` pour garantir une entropie cryptographique. |
| **`OidcAuthResult`** | Fusionne les données du jeton et les claims utilisateur validés. |
