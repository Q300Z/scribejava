# ADR 001 : Zéro Dépendance Externe Directe (Zero-Dependency)

*   **Statut** : Accepté
*   **Date** : 2026-05-22
*   **Auteurs** : Équipe ScribeJava

---

## 1. Contexte et Problème

Dans le monde Java, de nombreuses bibliothèques d'intégration de protocoles (telles que Nimbus, Spring Security ou Pac4j) importent un grand nombre de dépendances transitives (Jackson, slf4j, OkHttp, etc.).
Cela pose plusieurs problèmes majeurs pour les utilisateurs :
1.  **Conflits de versions (Jar Hell)** : L'importation d'une bibliothèque tierce peut forcer une version d'un parseur JSON ou d'un client HTTP incompatible avec le reste du projet de l'utilisateur.
2.  **Poids et performances** : Pour des projets légers, des microservices serverless, ou des applications Android, embarquer des dizaines de mégaoctets de dépendances d'infrastructure est sous-optimal.
3.  **Failles de sécurité** : Chaque dépendance transitive augmente la surface d'attaque globale du projet.

ScribeJava doit proposer un mécanisme d'intégration OAuth et OIDC ultra-léger et thread-safe.

---

## 2. Décision d'Architecture

Nous décidons de maintenir le module principal (`scribejava-core`) et le module OpenID Connect (`scribejava-oidc`) avec **zéro dépendance externe**.

*   **Parseur JSON natif** : ScribeJava implémente son propre parseur JSON minimaliste et robuste écrit entièrement à la main dans le Core.
*   **Couche réseau abstraite** : ScribeJava propose une couche de transport réseau abstraite qui utilise le client HTTP natif du JDK (`HttpURLConnection`) par défaut.
*   **Dépendances optionnelles** : Les intégrations de clients tiers performants (OkHttp, Armeria) ou de bibliothèques tierces spécifiques sont déportées dans des modules d'extension séparés (ex: `scribejava-httpclient-okhttp`). L'utilisateur n'importe ces dépendances que s'il choisit explicitement de les utiliser.

---

## 3. Conséquences

### Avantages (+)
*   **Légèreté absolue** : Le module `scribejava-core` pèse moins de 1 Mo et n'introduit aucun risque de conflit de dépendance pour l'utilisateur.
*   **Facilité d'intégration Android** : Zéro dépendance simplifie l'intégration dans Android Studio sans configuration complexe de ProGuard ou de résolution de conflits Gradle.
*   **Sécurité maximale** : Aucune dépendance transitive signifie aucune vulnérabilité introduite indirectement.

### Inconvénients (-)
*   **Coût de maintenance interne** : Toute modification ou correction du parseur JSON doit être faite et testée à la main au sein du projet plutôt que de s'appuyer sur des mises à jour de Jackson ou Gson.
*   **Limitation des fonctionnalités** : Le parseur JSON interne est conçu pour extraire des jetons et des structures d'identités simples. Il n'a pas vocation à devenir un parseur générique complexe.
