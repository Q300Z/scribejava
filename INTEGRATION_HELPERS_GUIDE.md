# 🛠️ Guide d'Intégration ScribeJava Helpers

Le module `scribejava-integration-helpers` fournit une couche d'orchestration de haut niveau. Il automatise les tâches répétitives et sécurise les flux d'authentification pour les environnements de production.

---

## 🏗️ Architecture Globale

Le module repose sur trois piliers :
1. **L'Orchestration du Flux** : Sécurisation de la redirection et du callback.
2. **La Gestion de Session** : Stockage et rafraîchissement automatique des jetons.
3. **L'Observabilité** : Audit et monitoring des événements critiques.

---

## 1. Sécurisation du Flux d'Autorisation

### A. Préparation de la Redirection (`StateGenerator` & `AuthSessionContext`)
Avant de rediriger l'utilisateur vers le fournisseur (IDP), vous devez générer et stocker un contexte de sécurité.

```java
StateGenerator stateGen = new StateGenerator();
String state = stateGen.generate(); // 32 octets d'entropie (Base64URL)
String nonce = stateGen.generate(); // Optionnel (requis pour OIDC)
PKCE pkce = service.generatePKCE(); // Optionnel (recommandé)

// Stockez cet objet dans votre session HTTP ou un cookie sécurisé
AuthSessionContext context = new AuthSessionContext(state, nonce, pkce);
session.setAttribute("OAUTH_CONTEXT", context);

// Générez l'URL avec ces paramètres
String url = service.getAuthorizationUrl(pkce, state);
```

### B. Traitement du Callback (`AuthFlowCoordinator`)
Le coordinateur simplifie la réception du code, valide le `state` (protection CSRF) et récupère le jeton.

```java
// Pour OAuth 2.0 Standard
AuthFlowCoordinator<String> coordinator = new AuthFlowCoordinator<>(service, repository);

// Pour OpenID Connect (Gère auto la validation du Nonce et de l'ID Token)
OidcAuthFlowCoordinator<String> oidcCoordinator = new OidcAuthFlowCoordinator<>(oidcService, repository);

AuthResult result = oidcCoordinator.finishAuthorization(
    userId, 
    codeRecu, 
    stateRecu, 
    contextPrecedemmentSauve
);
```

---

## 2. Gestion Intelligente des Jetons

### A. Persistance (`TokenRepository`)
Vous devez implémenter cette interface pour l'adapter à votre infrastructure (SQL, Redis, NoSQL).

```java
public class MyDatabaseRepository implements TokenRepository<String, ExpiringTokenWrapper> {
    public Optional<ExpiringTokenWrapper> findByKey(String userId) { ... }
    public void save(String userId, ExpiringTokenWrapper token) { ... }
    public void deleteByKey(String userId) { ... }
}
```

### B. Rafraîchissement Transparent (`TokenAutoRenewer`)
Le `TokenAutoRenewer` résout le problème des jetons expirés de manière **thread-safe**. Si deux requêtes concurrentes détectent un jeton expiré, une seule effectuera l'appel réseau de rafraîchissement.

```java
TokenAutoRenewer<String> renewer = new TokenAutoRenewer<>(
    repository,
    oldToken -> service.refreshAccessToken(oldToken.getRefreshToken())
);
```

### C. Client API Automatisé (`AuthorizedClientService`)
C'est le point d'entrée recommandé pour vos appels métier. Il combine le service et le renewer.

```java
AuthorizedClientService<String> client = new AuthorizedClientService<>(service, renewer);

// Exécution transparente : Récupération -> Refresh (si besoin) -> Signature -> Envoi
Response resp = client.execute(userId, new OAuthRequest(Verb.GET, "https://api.com/user"));
```

---

## 3. Support Multi-Tenant (`OAuthServiceRegistry`)

Si votre application supporte plusieurs fournisseurs (ex: Connexion via Google ET GitHub), utilisez le registre pour centraliser vos services.

```java
OAuthServiceRegistry<String> registry = new OAuthServiceRegistry<>();
registry.register("google", googleAuthorizedService);
registry.register("github", githubAuthorizedService);

// Utilisation
AuthorizedClientService<String> srv = registry.getService("google");
```

---

## 📈 4. Observabilité & Audit (`AuthEventListener`)

Branchez-vous sur le cycle de vie pour surveiller la santé de votre intégration.

```java
service.setListener(new AuthEventListener<String>() {
    @Override
    public void onTokenRefreshed(String userId, ExpiringTokenWrapper newToken) {
        log.info("Jeton rafraîchi pour : " + userId);
    }

    @Override
    public void onCsrfDetected(String userId, String got, String expected) {
        log.error("ATTENTION : Tentative CSRF détectée !");
    }
    
    @Override
    public void onRefreshFailed(String userId, Exception e) {
        log.error("Échec rafraîchissement. L'utilisateur doit se reconnecter.");
    }
});
```

---

## 💡 Résumé des Bénéfices
| Feature | Problème résolu |
| :--- | :--- |
| **`ExpiringTokenWrapper`** | Évite les erreurs 401 en anticipant l'expiration (buffer de 60s). |
| **`TokenAutoRenewer`** | Évite les "Refresh Storms" (rafraîchissements multiples simultanés). |
| **`OidcAuthFlowCoordinator`** | Garantit une validation cryptographique conforme aux specs OIDC. |
| **`StateGenerator`** | Empêche les attaques par fixation de session et CSRF. |
