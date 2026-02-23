# Processus de Release sur GitHub

Comme ScribeJava n'est pas publié sur Maven Central, les releases sont gérées directement via GitHub.

## 🚀 Comment créer une nouvelle version

1. **Mettre à jour les fichiers de version** :
   Assurez-vous que le `pom.xml` a la version finale (ex: `9.0.0` au lieu de `9.0.0-SNAPSHOT`).
   ```bash
   mvn versions:set -DnewVersion=9.0.0
   ```

2. **Mettre à jour le `CHANGELOG.md`** :
   Vérifiez que toutes les nouveautés sont bien listées sous la section de la version.

3. **Créer et pusher le Tag** :
   C'est cette étape qui déclenche l'automatisation.
   ```bash
   git tag -a v9.0.0 -m "Release version 9.0.0"
   git push origin v9.0.0
   ```

4. **Vérifier sur GitHub** :

* Allez dans l'onglet **Actions** pour voir le build de la release.
* Une fois terminé, allez dans **Releases**. Vous y trouverez les notes de version et tous les fichiers `.jar` (
  core, oidc, apis, etc.) attachés automatiquement.

## 🛠️ Utiliser cette version dans un autre projet

Comme la librairie n'est pas sur Maven Central, un utilisateur doit :

1. Télécharger les JARs depuis la page **Releases** de GitHub.
2. Les ajouter au classpath de son projet ou les installer manuellement dans son repo local via
   `mvn install:install-file`.
