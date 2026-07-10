# ⚙️ Guide de Référence : ScribeJava Core

Le module `scribejava-core` est le socle de la bibliothèque. Il fournit une implémentation robuste d'OAuth 2.0, conçue selon les principes SOLID et optimisée pour la production industrielle.

---

## 🏗️ 1. Configuration Avancée (`ServiceBuilder`)

Le `ServiceBuilder` utilise une interface fluide pour configurer votre service sans complexité.

### Options du Builder

- **`.scopes(String...)`** : Support des varargs pour une déclaration propre.

- **`.discoverFromIssuer(url, service)`** : Active la découverte automatique (OIDC/RFC 8414).

- **`AuthorizationUrlBuilder.initPKCE()`** : Active automatiquement le challenge PKCE (RFC 7636) lors de la génération de l'URL.

- **`.dpop(proofCreator)`** : Active le mécanisme DPoP (RFC 9449).

- **`.debug()`** : Active les logs détaillés sur `System.out`.

```java

OAuth20Service service = new ServiceBuilder("client-id")
    .apiSecret("secret")
    .scopes("openid", "profile", "email")
    .httpClientConfig(JDKHttpClientConfig.defaultConfig()) // Tuning timeouts
    .build(GitHubApi.instance());

```

---

## 🔑 2. Système de Concession (`Grants`)

ScribeJava v9 sépare la logique d'obtention de jetons via le patron **Strategy**.

### Utilisation des Grants

```java

// Authorization Code (Standard)
service.getAccessToken(new AuthorizationCodeGrant(code));

// Client Credentials (Machine-to-Machine)
service.getAccessToken(new ClientCredentialsGrant());

// Password Grant (Legacy/Trust)
service.getAccessToken(new PasswordGrant("user", "pass"));

// Device Code (Appareils sans navigateur)
service.pollAccessTokenDeviceAuthorizationGrant(deviceAuth);

```

---

## 🛰️ 3. Maîtrise des Requêtes (`OAuthRequest`)

L'objet `OAuthRequest` est une abstraction complète de la couche HTTP.

### Payloads Intelligents

- **Textuel** : `.setPayload(String)` (JSON, XML).

- **Binaire** : `.setPayload(byte[])`.

- **Fichier** : `.setPayload(File)`.

- **Multipart** : Le support de la charge utile Multipart est géré par les clients HTTP (`HttpClient.execute()`) via la classe `MultipartPayload` et sa méthode `.addBodyPart(new ByteArrayBodyPartPayload(...))`. *(Note : `OAuthRequest` n'expose pas de méthode d'injection directe, c'est le client HTTP de transport qui prend en charge l'objet `MultipartPayload`).*

### Diagnostic Premium (DX)

Ne devinez plus le contenu de vos requêtes :

- **`toCurlCommand()`** : Génère une commande `curl` prête à l'emploi avec masquage automatique des secrets (`[REDACTED]`).

- **`toDebugString()`** : Résumé structuré pour vos fichiers de logs.

---

## 🛡️ 4. Résilience & Observabilité

### Auto-Retry

Gérez les erreurs réseau transitoires (HTTP 429, 5xx) de manière transparente.

```java

service.setRetryPolicy(new OAuthRetryPolicy(3, 1000)); // 3 essais, 1s de pause

```

### Rate Limiting

Soyez notifié en temps réel de l'état de vos quotas d'API.

```java

service.setRateLimitListener((remaining, resetAt, response) -> {
    log.info("Requêtes restantes : " + remaining);
});

```

### Logging Structuré & Observabilité

ScribeJava propose une observabilité de premier ordre sans aucune dépendance externe (zéro dépendance tierce) pour le diagnostic en production :

- **`DefaultOAuthLogger`** : Formate de manière lisible les requêtes et réponses HTTP (avec commandes cURL prêtes à copier-coller) et effectue un masquage automatique et ultra-performant des secrets (`client_secret`, `access_token`, en-têtes d'autorisation, etc.) via des expressions régulières JDK natives.
- **`JdkOAuthLogger`** : Route les logs formatés et masqués directement vers le framework standard de logging de Java (`java.util.logging.Logger`).

#### Configuration Automatique
L'appel à `.debug()` ou `.debugStream(PrintStream)` sur le `ServiceBuilder` branche automatiquement un `DefaultOAuthLogger` écrivant sur `System.out` ou sur le flux spécifié :

```java
OAuth20Service service = new ServiceBuilder("client-id")
    .apiSecret("secret")
    .debug() // Active automatiquement DefaultOAuthLogger sur System.out avec masquage automatique des secrets
    .build(GitHubApi.instance());
```

#### Configuration Personnalisée
Pour rediriger vers votre propre infrastructure de logs (SLF4J, Log4j2) ou vers le système standard du JDK, utilisez `.logger(OAuthLogger)` :

```java
// Logger JDK standard (JUL) avec masquage automatique des secrets
OAuth20Service service = new ServiceBuilder("client-id")
    .apiSecret("secret")
    .logger(new JdkOAuthLogger(Logger.getLogger("monApp.scribejava")))
    .build(GitHubApi.instance());
```

#### Logs Système Internes (JUL)
En dehors de l'interception réseau, ScribeJava utilise le framework de logging standard du JDK (`java.util.logging`) pour vous notifier d'événements critiques d'arrière-plan sans interrompre l'exécution de votre application (mode dégradé résilient).

Par exemple, le composant `com.github.scribejava.oidc.IdTokenValidator` loggue un avertissement de niveau `WARNING` si la rotation automatique et le rechargement d'arrière-plan des clés de signature depuis le serveur JWKS du fournisseur d'identité OIDC échouent (ex. : coupure réseau).

---

## 🏗️ 5. Moteur JSON Natif (Zéro-Dépendance)

ScribeJava embarque son propre moteur JSON ultra-sécurisé :

- **`JsonBuilder`** : Génération fluide et sécurisée (échappement automatique).

- **`JsonObject`** : Accès typé aux données (`getInstant`, `getLong`, `getStringList`).

- **`JsonUtils`** : Parsing récursif performant supportant l'Unicode.

```java

String json = new JsonBuilder().add("id", 123).add("tags", Arrays.asList("a", "b")).build();
JsonObject obj = new JsonObject(JsonUtils.parse(responseBody));

```

---

## ❌ 6. Hiérarchie des Exceptions

Traitement différencié selon la nature de l'erreur :

- **`OAuthNetworkException`** : Problème physique (DNS, Timeout, Connexion).

- **`OAuthResponseException`** : Le serveur a répondu une erreur (401, 400). Accès aux détails via `getErrorDetails()`.

- **`OAuthProtocolException`** : Réponse malformée ou non-conforme.

- **`OAuthRateLimitException`** : HTTP 429 détecté.
