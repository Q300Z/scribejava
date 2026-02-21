# 🔌 Catalogue des APIs OAuth & OIDC

ScribeJava inclut des configurations prêtes à l'emploi pour plus de **50 fournisseurs**. Ce module contient les définitions d'endpoints et les extracteurs de jetons spécifiques.

---

## 🏆 Top APIs (v9.0.0)

| Fournisseur | Protocole | Classe ScribeJava | Exemple de Portées (Scopes) |
| :--- | :--- | :--- | :--- |
| **Google** | OAuth 2.0 / OIDC | `GoogleApi20` / `OidcGoogleApi20` | `profile`, `email` |
| **GitHub** | OAuth 2.0 / OIDC | `GitHubApi` / `OidcGitHubApi20` | `user`, `repo` |
| **Microsoft** | OAuth 2.0 / OIDC | `MicrosoftAzureActiveDirectory20Api` / `OidcMicrosoftAzureActiveDirectory20Api` | `User.Read`, `openid`, `profile` |
| **LinkedIn** | OAuth 2.0 | `LinkedInApi20` | `r_liteprofile`, `r_emailaddress` |
| **Twitter** | OAuth 1.0a (**Déprécié**) / 2.0 | `TwitterApi` | N/A (1.0a) |
| **Facebook** | OAuth 2.0 | `FacebookApi` | `public_profile`, `email` |

---

## 🏷️ Navigation par Catégorie

### 👥 Social & Identité
*   **Facebook** : [Exemple](./src/test/java/com/github/scribejava/apis/examples/FacebookExample.java)
*   **LinkedIn** : [Exemple](./src/test/java/com/github/scribejava/apis/examples/LinkedIn20Example.java)
*   **Slack** : [Exemple](./src/test/java/com/github/scribejava/apis/examples/SlackExample.java)

### 💻 DevOps & Cloud
*   **GitHub** : [Exemple](./src/test/java/com/github/scribejava/apis/examples/GitHubExample.java)
*   **GitLab** : [Exemple](./src/test/java/com/github/scribejava/apis/examples/GitLabApi20Example.java)
*   **Dropbox** : [Exemple](./src/test/java/com/github/scribejava/apis/examples/DropboxExample.java)

### 🏢 Enterprise (OIDC Native)
*   **Google OIDC** : Utilisez `OidcGoogleApi20` pour bénéficier de l'auto-découverte (Émetteur : `https://accounts.google.com`).
*   **GitHub OIDC** : Utilisez `OidcGitHubApi20` pour les intégrations GitHub Actions (Émetteur : `https://token.actions.githubusercontent.com`).
*   **Microsoft OIDC** : Utilisez `OidcMicrosoftAzureActiveDirectory20Api` pour Microsoft Entra ID (v2.0).
*   **Okta / Auth0** : Via le module générique `scribejava-oidc`.

---

## ➕ Ajouter un nouveau fournisseur (Checklist)

Nous encourageons les contributions ! Pour ajouter une API (ex: *DiscordApi*), suivez ces étapes :

1.  **Créer la classe API** : Étendre `DefaultApi20` dans `src/main/java/.../apis/`.
2.  **Définir les Endpoints** : Implémenter les URLs d'autorisation et de jeton.
3.  **Ajouter un Exemple** : Créer une classe dans `src/test/java/.../examples/`.
4.  **Vérifier le Style** : Lancer `make format` avant de soumettre votre PR.

---
[🏠 Accueil](../README.md) | [🔌 APIs](../scribejava-apis/README.md) | [🔐 OIDC](../scribejava-oidc/README.md) | [🛡️ Sécurité](../ADVANCED_SECURITY.md)
