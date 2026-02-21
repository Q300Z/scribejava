# Guide de Contribution ScribeJava

Merci de l'intérêt que vous portez à ScribeJava ! Ce document contient les instructions nécessaires pour contribuer au projet tout en respectant nos standards de qualité et d'architecture.

## 🏗️ Architecture du Projet

Le projet est divisé en plusieurs modules Maven, chacun ayant une responsabilité précise :

*   **`scribejava-core`** : Le moteur OAuth agnostique. Il gère le protocole, la signature des requêtes et l'abstraction HTTP.
*   **`scribejava-oidc`** : Extension pour le support complet d'OpenID Connect (Discovery, Dynamic Registration, ID Token validation).
*   **`scribejava-apis`** : Implémentations concrètes des fournisseurs (Google, GitHub, Facebook...).
*   **`scribejava-httpclient-*`** : Adaptateurs pour différentes librairies réseau (OkHttp, Armeria, etc.).

### Patterns de Conception
Nous suivons strictement les principes **SOLID** :
*   **Strategy Pattern** pour les types de Grant (`OAuth20Grant`).
*   **Handler Pattern** pour les fonctionnalités complexes dans `OAuth20Service` (Revocation, Device Flow, PAR).
*   **Inversion de Dépendance** : Toujours dépendre d'interfaces (`HttpClient`, `TokenExtractor`).

## 🛠️ Comment ajouter une fonctionnalité

### 1. Ajouter un nouveau Grant Type
Si vous souhaitez ajouter un nouveau flux OAuth (ex: SAML, CIBA) :
1. Créez une classe implémentant `OAuth20Grant` dans le package `com.github.scribejava.core.oauth2.grant`.
2. Implémentez la méthode `createRequest(OAuth20Service service)`.
3. Testez-la avec un test unitaire dédié.

### 2. Ajouter un nouveau fournisseur API
1. Créez une classe étendant `DefaultApi20` ou `DefaultApi10a` dans le module `scribejava-apis`.
2. Définissez les endpoints requis (`getAccessTokenEndpoint`, `getAuthorizationBaseUrl`).
3. Ajoutez un test de non-régression dans `ReflectiveApiTest`.

### 3. Ajouter un nouveau Handler
Si une responsabilité commence à polluer `OAuth20Service` :
1. Créez un Handler dédié (ex: `OAuth20MyFeatureHandler`).
2. Injectez-le dans le constructeur de `OAuth20Service`.
3. Déléguez les appels publics du service vers ce handler.

## 📏 Conventions de Code

### Standards Techniques
*   **Compatibilité Java 8** : Le code doit compiler et fonctionner sous Java 8.
*   **Typage fort** : Évitez `Any` ou `Object`. Utilisez des génériques et des Enums.
*   **Immuabilité** : Privilégiez les champs `final` et les objets immuables.

### Qualité du Code
Nous utilisons plusieurs outils automatisés :
*   **Checkstyle** : Respectez le fichier `checkstyle.xml` (Longueur de ligne max : 256).
*   **PMD** : Évitez le code mort et les mauvaises pratiques détectées par `pmd.xml`.
*   **PITest** : Nous utilisons le Mutation Testing pour vérifier la force des tests.

### Tests (TDD Obligatoire)
Toute nouvelle fonctionnalité ou correction de bug **doit** être accompagnée de tests :
*   **JUnit 5** (Jupiter).
*   **AssertJ** pour des assertions lisibles.
*   **MockWebServer** (OkHttp) pour simuler les réponses serveurs.
*   **Couverture cible** : > 80%.

## 📜 Conventions de Commit

Nous suivons la convention **Conventional Commits** :
*   `feat(...)`: Nouvelle fonctionnalité.
*   `fix(...)`: Correction de bug.
*   `test(...)`: Ajout ou modification de tests.
*   `refactor(...)`: Modification du code sans changement de comportement.
*   `build(...)`: Changement du système de build (Maven).

## 🚀 Lancer les tests

Pour lancer les tests en parallèle (recommandé) :
```bash
mvn test -T 1C -Dmaven.javadoc.skip=true
```

Pour vérifier la conformité Checkstyle et PMD :
```bash
mvn checkstyle:check pmd:check
```
