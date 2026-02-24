# 🛠️ ScribeJava Integration Helpers

Ce module fournit des outils de haut niveau pour intégrer ScribeJava dans des systèmes d'authentification existants (Spring, Quarkus, Jakarta EE, etc.).

## 📖 Fonctionnalités

1.  **`TokenAutoRenewer`** : Gère le rafraîchissement automatique des jetons de manière thread-safe.
2.  **`TokenRepository`** : Interface d'abstraction pour stocker vos jetons (DB, Redis, Session).
3.  **`StateGenerator`** : Générateur sécurisé de paramètre `state` pour la protection CSRF.
4.  **`ExpiringTokenWrapper`** : Encapsule un jeton avec sa date d'expiration calculée.

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

---

## 🔒 Bonnes Pratiques

*   **Expiration Buffer** : Par défaut, `TokenAutoRenewer` rafraîchit le jeton s'il expire dans moins de 60 secondes. Vous pouvez ajuster ce délai dans le constructeur.
*   **Sécurité du State** : Utilisez toujours `StateGenerator` pour garantir une entropie suffisante (32 octets par défaut).

---
[⬅️ Retour à l'accueil](../README.md)
