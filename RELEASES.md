# 📦 Historique des Releases

## v9.1.0 - OIDC Enterprise Edition (Février 2026)
**Nouveautés majeures :**
*   **Module `integration-helpers`** : Orchestration automatisée pour la production.
*   **OIDC Enterprise** : Coordinateur avec validation Nonce, JWT et Fallback UserInfo.
*   **Auto-Rafraîchissement** : `AuthorizedClientService` gère les jetons expirés de manière thread-safe.
*   **Observabilité** : Interface `AuthEventListener` pour l'audit et le logging.
*   **Matrix CI** : Validation certifiée sur tous les JDK de 8 à 25.

---

## v9.0.0 - SOLID Refactoring (Février 2026)
**Changements majeurs :**
*   **Strategy Pattern** : Introduction des `Grants` pour l'obtention des jetons.
*   **Sécurité** : Support complet PKCE (RFC 7636) et DPoP (RFC 9449).
*   **Discovery Service** : Découverte dynamique des endpoints OIDC (RFC 8414).
*   **Zéro Dépendance** : Core certifié sans dépendances externes.

---

## 🚀 Processus de Release

1.  **Tagging** : `git tag -a vX.Y.Z -m "Message"`
2.  **Pushing** : `git push origin vX.Y.Z`
3.  **Automation** : GitHub Actions compile les JARs et crée la release.

[⬅️ Retour à l'accueil](./README.md)
