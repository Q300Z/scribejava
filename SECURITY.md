# Politique de Sécurité

## 🛡️ Signaler une Vulnérabilité

Si vous découvrez une vulnérabilité de sécurité dans ScribeJava, merci de **ne pas ouvrir de ticket public**. Envoyez plutôt un email aux mainteneurs. Nous accuserons réception de votre rapport et travaillerons sur un correctif prioritaire.

## 🔒 Bonnes Pratiques de Sécurité

### 1. Gestion des Secrets
**Ne codez jamais de clés API ou de secrets en dur.** Utilisez des variables d'environnement ou un Vault sécurisé.
```java
// À NE PAS FAIRE
String secret = "sk_live_123456"; 

// À FAIRE
String secret = System.getenv("OAUTH_CLIENT_SECRET");
```

### 2. Stockage des Tokens
*   **Applications Web** : Stockez les tokens dans un cookie sécurisé (HTTP-only, encrypted) ou une session côté serveur.
*   **Applications Natives** : Utilisez le stockage sécurisé de l'OS (Keychain, Keystore).
*   **Éviter** : Ne stockez jamais de tokens dans le `localStorage` ou dans des bases de données non chiffrées.

### 3. HTTPS
ScribeJava requiert HTTPS pour toute communication en production. Assurez-vous que votre JVM est à jour pour supporter les versions modernes de TLS (1.2+).

### 4. PKCE (RFC 7636)
Utilisez toujours **PKCE** pour les clients publics (Mobile, SPA) et même pour les applications serveur pour prévenir l'injection de code d'autorisation.
