# 🚀 Processus de Release ScribeJava

Ce document décrit comment publier une nouvelle version de la bibliothèque en utilisant la chaîne CI/CD automatisée.

## 1. Pré-requis

- Les tests locaux doivent être au vert : `./ci-local.sh`.
- Vous devez avoir les droits d'écriture sur le dépôt GitHub.

## 2. Déclenchement d'une Release

La release est pilotée par l'outil `release-it`. Il y a deux manières de procéder :

### Méthode A : Automatisée (Recommandée)

1. Allez dans l'onglet **Actions** de GitHub.
2. Sélectionnez le workflow **Prepare Release**.
3. Cliquez sur **Run workflow**.
   - Ce workflow va :
     - Monter la version dans les `pom.xml`.
     - Créer un commit de release.
     - Créer un tag Git (ex: `v9.3.0`).
     - Pousser le tout sur `master`.

### Méthode B : Manuelle (Expert)

Depuis votre machine locale :

```bash
cd scribejava
pnpm install
pnpm exec release-it
```

## 3. Déroulement de la CI après le Tag

Une fois le tag poussé (via la Méthode A ou B), le workflow **Java CI with Maven** se déclenche automatiquement :

1. **Validation Multi-JDK** : Tests sur JDK 8, 11, 17, 21, 25.
2. **Build des Artefacts** : Les JARs sont compilés sous JDK 8.
3. **Publication GitHub** :
   - Une Release GitHub est créée.
   - Les JARs sont attachés en tant qu'assets.
   - Le Changelog est généré automatiquement.
4. **Documentation** : La Javadoc est mise à jour sur GitHub Pages.

## 4. Vérification

Une fois la CI terminée, vérifiez :

- La section [Releases](https://github.com/Q300Z/scribejava/releases).
- La documentation en ligne.
