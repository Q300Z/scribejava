# 🛠️ ScribeJava Integration Helpers

Ce module fournit des outils de haut niveau pour intégrer ScribeJava dans des systèmes d'authentification existants (Spring, Quarkus, Jakarta EE, etc.).

## 📖 Fonctionnalités

1.  **`TokenAutoRenewer`** : Gère le rafraîchissement automatique des jetons de manière thread-safe.
2.  **`AuthorizedClientService`** : Orchestre le renewer pour exécuter des appels API signés de manière transparente.
3.  **`AuthFlowCoordinator`** : Coordonne la fin du flux (Callback), validant le `state` et sauvant le jeton.
4.  **`TokenRepository`** : Interface d'abstraction pour stocker vos jetons (DB, Redis, Session).
5.  **`StateGenerator`** : Générateur sécurisé de paramètre `state` pour la protection CSRF.
6.  **`ExpiringTokenWrapper`** : Encapsule un jeton avec sa date d'expiration calculée.

---

## 🚀 Guide d'Utilisation

### 1. Stockage des Jetons (`TokenRepository`)

Implémentez l'interface pour brancher votre système de stockage :

```java
public class RedisTokenRepository implements TokenRepository<String, ExpiringTokenWrapper> {
    @Override
    public void save(String userId, ExpiringTokenWrapper wrapper) {
        redis.set(userId, serialize(wrapper));
    }
    // ... implémenter findByKey et deleteByKey
}
```

### 2. Rafraîchissement Automatique (`TokenAutoRenewer`)

Utilisez le renewer pour obtenir systématiquement un jeton valide. S'il est expiré, il sera rafraîchi automatiquement.

```java
TokenAutoRenewer<String> renewer = new TokenAutoRenewer<>(
    repository, 
    oldToken -> service.refreshAccessToken(oldToken.getRefreshToken())
);

// Dans votre logique métier :
OAuth2AccessToken validToken = renewer.getValidToken(userId);
// L'appel ci-dessus est thread-safe : si deux threads appellent en même temps pour le même utilisateur,
// un seul fera l'appel réseau de rafraîchissement.
```

### 3. Protection CSRF (`StateGenerator`)

```java
StateGenerator stateGenerator = new StateGenerator();

// 1. Avant redirection
String state = stateGenerator.generate();
session.setAttribute("oauth_state", state);

// 2. Au retour du serveur
String returnedState = request.getParameter("state");
if (!state.equals(returnedState)) {
    throw new SecurityException("CSRF Detected!");
}
```

### 4. Client Automatisé (`AuthorizedClientService`)

Ce service est le point d'entrée recommandé pour faire vos appels API. Il s'occupe de tout : récupération, rafraîchissement si nécessaire, signature et exécution.

```java
AuthorizedClientService<String> client = new AuthorizedClientService<>(service, renewer);

OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");
try (Response response = client.execute(userId, request)) {
    System.out.println(response.getBody());
}
```

### 5. Coordinateur de Callback (`AuthFlowCoordinator`)

Simplifie la réception du code d'autorisation.

```java
AuthFlowCoordinator<String> coordinator = new AuthFlowCoordinator<>(service, repository);

// Appelé dans votre contrôleur de callback /oauth/callback
try {
    AuthResult result = coordinator.finishAuthorization(userId, code, stateParam, sessionState);
    System.out.println("Authentification réussie !");
} catch (SecurityException e) {
    // Tentative de CSRF détectée
}
```

---

## 🔒 Bonnes Pratiques

*   **Expiration Buffer** : Par défaut, `TokenAutoRenewer` rafraîchit le jeton s'il expire dans moins de 60 secondes. Vous pouvez ajuster ce délai dans le constructeur.
*   **Sécurité du State** : Utilisez toujours `StateGenerator` pour garantir une entropie suffisante (32 octets par défaut).

---
[⬅️ Retour à l'accueil](../README.md)
