# Guide de Contribution ScribeJava

Merci de l'intérêt que vous portez à ScribeJava ! Ce document contient les instructions nécessaires pour contribuer au projet tout en respectant nos standards de qualité et d'architecture.

## 🏗️ Architecture du Projet

Le projet est divisé en plusieurs modules Maven, chacun ayant une responsabilité précise :

*   **`scribejava-core`** : Le moteur OAuth agnostique.
*   **`scribejava-oidc`** : Extension pour le support d'OpenID Connect.
*   **`scribejava-apis`** : Implémentations concrètes des fournisseurs (Google, GitHub, etc.).
*   **`scribejava-httpclient-*`** : Adaptateurs réseau.

### Principes de Design
Nous suivons strictement les principes **SOLID** :
*   **Strategy Pattern** pour les types de Grant (`OAuth20Grant`).
*   **Handler Pattern** pour les fonctionnalités complexes dans `OAuth20Service`.

## 🛠️ Comment ajouter une fonctionnalité

### 1. Ajouter un nouveau Grant Type
1. Créez une classe implémentant `OAuth20Grant` dans le package `com.github.scribejava.core.oauth2.grant`.
2. Implémentez `createRequest(OAuth20Service service)`.
3. Ajoutez un test unitaire.

### 2. Ajouter un nouveau fournisseur API
1. Créez une classe étendant `DefaultApi20` ou `DefaultApi10a` dans le module `scribejava-apis`.
2. Ajoutez un test dans `ReflectiveApiTest`.

## 📏 Conventions de Code

### Standards Techniques
*   **Compatibilité Java 8** : Requis pour la compilation et l'exécution.
*   **Typage fort** : Évitez `Object`. Utilisez des génériques et des Enums.

### Qualité du Code
*   **Checkstyle** : Respectez le fichier `checkstyle.xml`.
*   **PMD** : Évitez le code mort et les mauvaises pratiques.
*   **PITest** : Mutation Testing requis pour vérifier l'efficacité des tests.

## 📜 Conventions de Commit

Nous utilisons **Conventional Commits** :
*   `feat(...)`: Nouvelle fonctionnalité.
*   `fix(...)`: Correction de bug.
*   `test(...)`: Ajout ou modification de tests.
*   `refactor(...)`: Modification structurelle sans changement de comportement.

## 🚀 Lancer les tests

```bash
mvn test -T 1C -Dmaven.javadoc.skip=true
```

## 📚 Javadoc locale

```bash
mvn javadoc:aggregate -Dmaven.test.skip=true
```
Les fichiers seront dans `target/site/apidocs/index.html`.
