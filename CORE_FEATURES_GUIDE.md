# ⚙️ Guide de Référence ScribeJava Core

Le module `scribejava-core` est le socle de la bibliothèque. Il fournit une implémentation robuste et performante d'OAuth 2.0, conçue selon les principes SOLID et sans aucune dépendance externe au runtime.

---

## 🏗️ 1. Initialisation & Construction (`ServiceBuilder`)

Le `ServiceBuilder` est votre point d'entrée unique. Il permet de configurer le client de manière fluide.

### Configuration de base
```java
OAuth20Service service = new ServiceBuilder("my-client-id")
    .apiSecret("my-secret")
    .callback("https://my-app.com/callback")
    .scopes("profile", "email") // Nouveau : support des varargs
    .build(GitHubApi.instance());
```

### Options Avancées du Builder
- **`.pkce(true)`** : Active automatiquement le flux PKCE (recommandé).
- **`.userAgent(string)`** : Définit une signature personnalisée pour vos appels réseau.
- **`.httpClientConfig(config)`** : Permet de régler les timeouts, proxies, et SSL.
- **`.dpop(proofCreator)`** : Active le mécanisme DPoP pour lier les jetons au client.

---

## 🔑 2. Système de Concession (`Grants`)

ScribeJava utilise le patron de conception **Strategy** pour gérer les différents flux d'obtention de jetons.

### Flux Standards
```java
// Authorization Code
service.getAccessToken(new AuthorizationCodeGrant(code, pkce));

// Client Credentials
service.getAccessToken(new ClientCredentialsGrant("optional-scope"));

// Password Grant
service.getAccessToken(new PasswordGrant("user", "pass"));

// Refresh Token
service.refreshAccessToken("my-refresh-token");
```

---

## 🛰️ 3. Requêtes & Debugging (`OAuthRequest`)

L'objet `OAuthRequest` est une abstraction puissante du protocole HTTP.

### Manipulation des Payloads
ScribeJava supporte nativement plusieurs types de corps de requête :
- **Textuel** : `.setPayload(String)` (ex: JSON, XML).
- **Binaire** : `.setPayload(byte[])`.
- **Fichier** : `.setPayload(File)`.
- **Multipart** : `.setMultipartPayload(multipart)` pour l'envoi de fichiers complexes.

### Outils de Diagnostic (DX Premium)
Plus besoin de deviner ce qui est envoyé au serveur :
- **`toCurlCommand()`** : Génère une commande shell prête à l'emploi (secrets masqués par défaut).
- **`toDebugString()`** : Affiche un résumé structuré de la requête pour vos logs.

---

## 🛡️ 4. Résilience & Observabilité

Conçu pour la production, le moteur Core intègre des mécanismes de survie réseau.

### Auto-Retry
Gérez les erreurs transitoires sans code supplémentaire.
```java
// Tente 3 fois avec un délai de 500ms entre chaque essai (cible 429 et 5xx)
service.setRetryPolicy(new OAuthRetryPolicy(3, 500));
```

### Logging Structuré (`OAuthLogger`)
Branchez votre propre système de surveillance (SLF4J, ELK, Datadog).
```java
service.setLogger(new OAuthLogger() {
    public void logRequest(OAuthRequest req) { ... }
    public void logResponse(Response resp) { ... }
});
```

### Gestion des Quotas (`RateLimitListener`)
Soyez notifié dès qu'un serveur d'API renvoie des informations de limite.
```java
service.setRateLimitListener((remaining, resetAt, response) -> {
    if (remaining < 10) warn("Quota presque épuisé !");
});
```

---

## 🏗️ 5. Moteur JSON Natif

Pour rester **Zero-Dependency**, ScribeJava embarque son propre moteur JSON sécurisé.

- **`JsonBuilder`** : Pour créer vos requêtes sans erreurs de syntaxe ni injection.
  ```java
  String json = new JsonBuilder().add("id", 1).add("active", true).build();
  ```
- **`JsonObject`** : Pour lire les réponses de manière sûre.
  ```java
  JsonObject json = new JsonObject(parsedMap);
  Optional<Instant> exp = json.getInstant("expires_at");
  ```

---

## ❌ 6. Hiérarchie des Exceptions

Le Core définit un typage précis pour faciliter le traitement des erreurs :
- **`OAuthNetworkException`** : Problème physique (DNS, Timeout, Connexion).
- **`OAuthResponseException`** : Erreur retournée par le serveur (401, 403, 400).
- **`OAuthProtocolException`** : Non-conformité aux spécifications.
- **`OAuthRateLimitException`** : Dépassement de quota (HTTP 429).
