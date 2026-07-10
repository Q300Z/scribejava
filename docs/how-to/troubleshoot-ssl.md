# 🛠️ Dépannage (Troubleshooting)

Ce guide répertorie les erreurs courantes rencontrées lors du développement ou de l'utilisation de ScribeJava.

---

## 1. Problèmes de Runtime (Exécution)

### ❌ `java.lang.NoClassDefFoundError` ou `ClassNotFoundException`

* **Cause** : Vous utilisez une version de Java incompatible ou il manque une dépendance optionnelle.

* **Solution** :
  * Si l'erreur concerne `jackson`, assurez-vous d'avoir ajouté une bibliothèque de JSON (Jackson est utilisé par
    défaut dans `apis` et `oidc`).
  * Si l'erreur survient lors des tests sous Java 8, vérifiez que vous utilisez JUnit 5.9.3 et Surefire 3.0.0-M7 (voir
    `pom.xml`).

### ❌ `SSLHandshakeException: Received fatal alert: handshake_failure`

* **Cause** : Votre JDK 8 est trop vieux et ne supporte pas les suites de chiffrement modernes demandées par le
  fournisseur (ex: Google, GitHub).

* **Solution** :
  * Mettez à jour votre JDK vers une version récente (ex: OpenJDK 8u251+).
  * Ou forcez TLS 1.2 au démarrage : `-Dhttps.protocols=TLSv1.2`.

---

## 2. Problèmes de Build (Maven)

### ❌ Échec du lintage (Checkstyle/PMD)

* **Erreur** : `Annotation 'Mock' should be alone on line` ou
  `Block tags have to appear in the order... [AtclauseOrder]`.

* **Cause** : Conflit entre les règles strictes de Checkstyle et le formatage automatique Spotless, ou mauvais ordre des
  balises `@param`, `@return`, `@deprecated` dans la Javadoc.

* **Solution** :
  * Lancez `make format` (ou `mvn spotless:apply`) pour le style.
  * Respectez l'ordre OIDC/Javadoc : `@param` -> `@return` -> `@throws` -> `@see` -> `@deprecated`.

### ❌ Avertissements de Build (Duplicate Plugin Declaration)

* **Avertissement** : `'build.pluginManagement.plugins.plugin.(groupId:artifactId)' must be unique`.

* **Cause** : Le plugin `maven-surefire-plugin` est déclaré deux fois avec des versions différentes dans le `pom.xml`.

* **Solution** : Supprimez la version obsolète (ex: 2.22.2) pour ne conserver que la version stable (ex: 3.0.0-M7) dans
  la section `pluginManagement`.

---

## 3. Problèmes OpenID Connect

### ❌ `Invalid ID Token: iss mismatch`

* **Cause** : L'émetteur (`iss`) présent dans le jeton ne correspond pas à l'URL configurée dans votre
  `OidcDiscoveryService`.

* **Solution** : Vérifiez que l'URL de l'issuer est exacte (attention aux slashs de fin : `https://accounts.google.com`
  vs `https://accounts.google.com/`).

---

## 4. Helpers d'Intégration

### ❌ `IllegalArgumentException: No token found for key`

* **Cause** : Vous tentez d'utiliser `TokenAutoRenewer.getValidToken(key)` pour un utilisateur qui n'a pas encore de jeton stocké dans votre `TokenRepository`.

* **Solution** : Avant d'utiliser le renewer, assurez-vous que l'utilisateur a effectué le flux d'autorisation complet et que vous avez sauvegardé son premier jeton via `repository.save(key, wrapper)`.

### ❌ `NoSuchMethodError` lors du build (Spotless) sous JDK 25

* **Cause** : Le plugin de formatage Spotless 2.43.0 (utilisant google-java-format 1.17.0) est incompatible avec les API internes du JDK 25.

* **Solution** : Utilisez le script `./ci-local.sh` qui isole le lintage sur le JDK 17 et ne lance que les tests sur le JDK 25 via Docker.

---

## 5. Débogage & Logs

### 🔍 Activer les logs de débogage réseau

ScribeJava intègre son propre système de log sans dépendance. Pour inspecter en détail les requêtes et réponses HTTP échangées (y compris les commandes `curl` générées et le masquage automatique des secrets) :

1. Appelez la méthode `.debug()` (pour afficher sur la sortie standard `System.out`) ou `.debugStream(PrintStream)` lors de la construction du service via le `ServiceBuilder` :

```java
OAuth20Service service = new ServiceBuilder("client-id")
    .apiSecret("secret")
    .debug() // Active automatiquement DefaultOAuthLogger sur System.out
    .build(GoogleApi20.instance());
```

2. Si vous préférez router les logs vers le système de logging standard du JDK (`java.util.logging`), utilisez un `JdkOAuthLogger` :

```java
OAuth20Service service = new ServiceBuilder("client-id")
    .apiSecret("secret")
    .logger(new JdkOAuthLogger(Logger.getLogger("scribejava")))
    .build(GoogleApi20.instance());
```

---

## 5. Outils de Débogage Externes

Parfois, le problème ne vient pas du code mais de la configuration du serveur. Utilisez ces outils pour valider vos
jetons et flux :

* **[JWT.io](https://jwt.io)** : Indispensable pour inspecter le contenu et la signature de vos `ID Tokens` ou jetons
  `DPoP`.

* **[OAuth 2.0 Playground (Google)](https://developers.google.com/oauthplayground/)** : Permet de tester les échanges de
  tokens étape par étape sans écrire une ligne de code.

* **[OpenID Connect Debugger](https://openidconnect.net/)** : Idéal pour tester la découverte (Discovery) et les
  redirections.

---
[⬅️ Retour au README principal](https://github.com/Q300Z/scribejava/blob/master/README.md)
