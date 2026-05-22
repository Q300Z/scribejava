# 🚀 Démarrage Rapide (Quick Start)

ScribeJava v9.2.x est conçu pour être opérationnel en quelques minutes. Cette page vous guide à travers les scénarios les plus courants avec des exemples complets prêts à l'emploi.

---

## 🏗️ Pré-requis

* **JDK 8** ou supérieur.

* Un identifiant client (`client_id`) obtenu auprès d'un fournisseur (Google, GitHub, Microsoft, etc.).

---

## 🗺️ Choisissez votre flux

### 1. OAuth 2.0 Standard & Fournisseurs Spécifiques

Idéal pour les applications web ou les outils CLI.

* **Sécurité** : Inclus PKCE (RFC 7636).
* **Exemples standard** :
  * [`OAuth20QuickStart.java`](https://github.com/Q300Z/scribejava/blob/master/scribejava-apis/src/test/java/com/github/scribejava/apis/examples/quickstart/OAuth20QuickStart.java) (Exemple générique)
  * [`KeycloakQuickStart.java`](https://github.com/Q300Z/scribejava/blob/master/scribejava-apis/src/test/java/com/github/scribejava/apis/examples/quickstart/KeycloakQuickStart.java) (Connexion et UserInfo avec **Keycloak** en local ou à distance)

### 2. OpenID Connect (OIDC) Natif & Cloud Identity

Pour l'authentification moderne avec validation automatique de l'identité et décodage de l'ID Token.

* **Fonctionnalité** : Découverte dynamique (Discovery) et validation native du jeton (ID Token).
* **Exemples OIDC** :
  * **Google** : [`OidcQuickStart.java`](https://github.com/Q300Z/scribejava/blob/master/scribejava-apis/src/test/java/com/github/scribejava/apis/examples/quickstart/OidcQuickStart.java) (Découverte et validation OIDC Google native)
  * **Microsoft Entra ID (Azure AD)** : [`MicrosoftAdQuickStart.java`](https://github.com/Q300Z/scribejava/blob/master/scribejava-apis/src/test/java/com/github/scribejava/apis/examples/quickstart/MicrosoftAdQuickStart.java) (Intégration OIDC multi-tenant Microsoft et appels Microsoft Graph API)

### 3. Machine-to-Machine (Client Credentials)

Pour les serveurs, les démons ou les scripts automatisés sans intervention humaine.

* **Fonctionnalité** : Authentification directe du client.
* **Exemple** : [`ClientCredentialsQuickStart.java`](https://github.com/Q300Z/scribejava/blob/master/scribejava-apis/src/test/java/com/github/scribejava/apis/examples/quickstart/ClientCredentialsQuickStart.java)

### 4. Appareils sans Navigateur (Device Flow)

Pour les terminaux SSH, les Smart TV ou les objets connectés (IoT).

* **Fonctionnalité** : Validation déportée sur un autre appareil (Smartphone).
* **Exemple** : [`DeviceFlowQuickStart.java`](https://github.com/Q300Z/scribejava/blob/master/scribejava-apis/src/test/java/com/github/scribejava/apis/examples/quickstart/DeviceFlowQuickStart.java)

---

## 🛠️ Comment lancer les exemples ?

Tous les exemples sont situés dans le module `scribejava-apis`. Vous pouvez les copier dans votre projet ou les lancer directement via Maven :

```bash
# Lancer l'exemple générique OAuth2
mvn test-compile exec:java -Dexec.mainClass="com.github.scribejava.apis.examples.quickstart.OAuth20QuickStart" -pl scribejava-apis

# Lancer l'exemple OIDC Google
mvn test-compile exec:java -Dexec.mainClass="com.github.scribejava.apis.examples.quickstart.OidcQuickStart" -pl scribejava-apis

# Lancer l'exemple Keycloak
mvn test-compile exec:java -Dexec.mainClass="com.github.scribejava.apis.examples.quickstart.KeycloakQuickStart" -pl scribejava-apis

# Lancer l'exemple Microsoft Azure AD
mvn test-compile exec:java -Dexec.mainClass="com.github.scribejava.apis.examples.quickstart.MicrosoftAdQuickStart" -pl scribejava-apis
```

---

## 🔒 Bonnes Pratiques de Production

Pour passer de ces exemples à une application industrielle, consultez nos guides avancés :

1. **[Auto-Refresh & Orchestration](../how-to/auto-refresh-tokens.md)** : Ne gérez plus les jetons manuellement.

2. **[Sécurité Enterprise (DPoP)](../how-to/secure-with-dpop-pkce.md)** : Liez vos jetons à votre matériel client.

3. **[Dépannage](../how-to/troubleshoot-ssl.md)** : En cas d'erreur `SSL` ou `iss mismatch`.

---
[⬅️ Retour au README principal](https://github.com/Q300Z/scribejava/blob/master/README.md)
