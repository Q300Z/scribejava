# 🚀 Démarrage Rapide (Quick Start)

ScribeJava v9.2.x est conçu pour être opérationnel en quelques minutes. Cette page vous guide à travers les scénarios les plus courants avec des exemples complets prêts à l'emploi.

---

## 🏗️ Pré-requis
*   **JDK 8** ou supérieur.
*   Un identifiant client (`client_id`) obtenu auprès d'un fournisseur (Google, GitHub, Microsoft, etc.).

---

## 🗺️ Choisissez votre flux

### 1. OAuth 2.0 Standard (Interactif)
Idéal pour les applications web ou les outils CLI où l'utilisateur peut ouvrir un navigateur.
*   **Sécurité** : Inclus PKCE (RFC 7636).
*   **Exemple** : [`OAuth20QuickStart.java`](../scribejava-apis/src/test/java/com/github/scribejava/apis/examples/quickstart/OAuth20QuickStart.java)

### 2. OpenID Connect (OIDC) Natif
Pour l'authentification moderne avec validation automatique de l'identité.
*   **Fonctionnalité** : Découverte dynamique (Discovery) et validation native du jeton (ID Token).
*   **Exemple** : [`OidcQuickStart.java`](../scribejava-apis/src/test/java/com/github/scribejava/apis/examples/quickstart/OidcQuickStart.java)

### 3. Machine-to-Machine (Client Credentials)
Pour les serveurs, les démons ou les scripts automatisés sans intervention humaine.
*   **Fonctionnalité** : Authentification directe du client.
*   **Exemple** : [`ClientCredentialsQuickStart.java`](../scribejava-apis/src/test/java/com/github/scribejava/apis/examples/quickstart/ClientCredentialsQuickStart.java)

### 4. Appareils sans Navigateur (Device Flow)
Pour les terminaux SSH, les Smart TV ou les objets connectés (IoT).
*   **Fonctionnalité** : Validation déportée sur un autre appareil (Smartphone).
*   **Exemple** : [`DeviceFlowQuickStart.java`](../scribejava-apis/src/test/java/com/github/scribejava/apis/examples/quickstart/DeviceFlowQuickStart.java)

---

## 🛠️ Comment lancer les exemples ?

Tous les exemples sont situés dans le module `scribejava-apis`. Vous pouvez les copier dans votre projet ou les lancer via Maven :

```bash
# Exemple pour le flux standard
mvn test-compile exec:java -Dexec.mainClass="com.github.scribejava.apis.examples.quickstart.OAuth20QuickStart" -pl scribejava-apis
```

---

## 🔒 Bonnes Pratiques de Production

Pour passer de ces exemples à une application industrielle, consultez nos guides avancés :
1.  **[Auto-Refresh & Orchestration](./INTEGRATION_HELPERS_GUIDE.md)** : Ne gérez plus les jetons manuellement.
2.  **[Sécurité Enterprise (DPoP)](./ADVANCED_SECURITY.md)** : Liez vos jetons à votre matériel client.
3.  **[Dépannage](./TROUBLESHOOTING.md)** : En cas d'erreur `SSL` ou `iss mismatch`.

---
[⬅️ Retour au README principal](../README.md)
