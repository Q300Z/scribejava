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

## 4. Débogage & Logs

### 🔍 Inspecter les échanges réseau

ScribeJava utilise **SLF4J** pour son logging. Pour voir le détail des requêtes/réponses (très utile pour le débogage
OAuth) :

1. Ajoutez une implémentation SLF4J (ex: `logback` ou `slf4j-simple`).
2. Passez le niveau de log à `DEBUG` pour le package `com.github.scribejava`.

**Exemple logback.xml :**

```xml
<logger name="com.github.scribejava" level="DEBUG" />
```

### 🛰️ Mode "Verbose" (Sortie standard)

Si vous ne voulez pas configurer de framework de log, vous pouvez activer la sortie standard sur le `OAuthRequest` :

```java
request.setCharset("UTF-8");
// Le debug se fait ensuite via votre logger SLF4J configuré
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
[⬅️ Retour au README principal](./README.md)
