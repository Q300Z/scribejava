# Guide de Contribution ScribeJava

Ce document rassemble les instructions pour contribuer, les détails de l'architecture, ainsi que les guides de sécurité et de dépannage.

---

## 🏗️ Architecture & Responsabilités

### Modules Maven
*   **`scribejava-core`** : Le moteur OAuth agnostique (Protocole, Signature, Abstraction HTTP).
*   **`scribejava-oidc`** : Support d'OpenID Connect (Discovery, Registration, Validation).
*   **`scribejava-apis`** : Fournisseurs concrets (Google, GitHub, etc.).
*   **`scribejava-httpclient-*`** : Adaptateurs réseau (OkHttp, Armeria, etc.).

### Responsabilités des Classes (Core)
| Composant | Rôle |
| :--- | :--- |
| **`OAuth20Service`** | Orchestrateur principal. Délègue la logique aux handlers spécialisés. |
| **`OAuth20Grant`** | [Pattern Strategy] Encapsule la création de requêtes pour chaque flux (Code, Password, etc.). |
| **`OAuth20RequestSigner`** | Gère la signature HTTP et les preuves DPoP. |
| **`OAuth20RevocationHandler`** | Gère la révocation de token (RFC 7009). |
| **`OAuth20DeviceFlowHandler`** | Gère le flux "Device Authorization" (RFC 8628). |
| **`OAuth20PushedAuthHandler`** | Gère les requêtes PAR (RFC 9126). |

### Principes SOLID Appliqués
1.  **SRP** : Chaque Handler a une responsabilité unique.
2.  **OCP** : Nouveau flux ? Ajoutez un `OAuth20Grant` sans toucher au service.
3.  **DIP** : Dépendance vers des interfaces (`HttpClient`, `TokenExtractor`).

---

## 🛠️ Comment contribuer

### Ajouter une fonctionnalité
*   **Nouveau Grant** : Implémentez `OAuth20Grant` dans `com.github.scribejava.core.oauth2.grant`.
*   **Nouveau Provider** : Étendez `DefaultApi20` dans `scribejava-apis` et testez via `ReflectiveApiTest`.

### Standards & Qualité
*   **Java 8** : Compatibilité obligatoire.
*   **TDD** : Tout code doit être testé (JUnit 5 + AssertJ).
*   **Checkstyle & PMD** : Lancement systématique via `mvn checkstyle:check pmd:check`.
*   **Mutation Testing** : Utilisez PITest pour valider la force de vos tests.

### Conventions de Commit
Utilisez **Conventional Commits** : `feat:`, `fix:`, `refactor:`, `docs:`, `build:`.

---

## 🔒 Politique de Sécurité

*   **Signalement** : Ne créez pas de ticket public pour une faille. Contactez les mainteneurs par email.
*   **Secrets** : Ne jamais coder de secrets en dur. Utilisez `System.getenv()`.
*   **Stockage** : Utilisez des cookies sécurisés (HttpOnly) ou le stockage sécurisé de l'OS.
*   **PKCE** : Recommandé pour tous les flux afin de prévenir l'injection de code.

---

## 📥 Guide d'Extensibilité

### Extracteur de Token Personnalisé
Implémentez `TokenExtractor<OAuth2AccessToken>` et déclarez-le dans votre classe `Api`.

### Client HTTP Personnalisé
Implémentez `com.github.scribejava.core.httpclient.HttpClient` et passez-le au `ServiceBuilder`.

---

## 🌐 Dépannage (Troubleshooting)

### Erreurs SSL (Java 8)
*   **handshake_failure** : Mettez à jour votre JDK (>= 8u251) ou forcez TLS 1.2 via `-Dhttps.protocols=TLSv1.2`.
*   **PKIX path building failed** : Importez le certificat du serveur dans `cacerts` via `keytool`.

### Débogage
Activez le mode debug dans le builder : `.debug()`. Pour SLF4J, utilisez `.debugStream(new MyLoggingStream())`.

---

## 🚀 Commandes utiles

*   **Tests parallèles** : `mvn test -T 1C -Dmaven.javadoc.skip=true`
*   **Javadoc locale** : `mvn javadoc:aggregate -Dmaven.test.skip=true` (Disponible dans `target/site/apidocs/`).
