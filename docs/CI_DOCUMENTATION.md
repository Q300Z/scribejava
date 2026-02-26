# 🏗️ Documentation CI/CD ScribeJava

Ce document détaille l'infrastructure de Continuous Integration (CI) et Continuous Delivery (CD) de ScribeJava v9.1+. Notre pipeline est conçu pour garantir la règle **Zéro-Dépendance** et la stabilité sur une matrice de **5 versions de JDK**.

---

## 1. Vue d'ensemble de l'Architecture

Le pipeline est divisé en trois zones : le développement local (Docker), la validation GitHub (Actions) et la publication automatisée.

```mermaid
graph TD
    subgraph "💻 Zone Développeur"
        Dev[Code & Tests] --> Format[make format]
        Format --> CertLocal[make certify]
        CertLocal -- "Docker multi-JDK" --> Push[git push master]
    end

    subgraph "☁️ GitHub CI (maven.yml)"
        Push --> Lint[Job: Lintage JDK 17]
        Lint --> Build[Job: Build JDK 8]
        Build --> TestMatrix{Test Matrix}
        TestMatrix --> JDK8[JDK 8]
        TestMatrix --> JDK11[JDK 11]
        TestMatrix --> JDK17[JDK 17]
        TestMatrix --> JDK21[JDK 21]
        TestMatrix --> JDK25[JDK 25]
    end

    subgraph "🚀 Zone Release (direct-release.yml)"
        Trigger[make release] --> GH_Workflow[Direct Release Workflow]
        GH_Workflow --> Calc[Calcul Version & Changelog]
        Calc --> Tag[Création Tag vX.Y.Z]
        Tag --> FinalRelease[GitHub Release + JARs + Javadoc]
    end
```

---

## 2. Pipeline de Certification Locale (`ci-local.sh`)

Avant chaque push, le développeur doit lancer `make certify`. Ce script utilise Docker pour isoler l'exécution et garantir que les changements sont compatibles avec le passé (Java 8) et le futur (Java 25).

### Étapes du cycle local :
1.  **Formatage** : Spotless applique le Google Java Style.
2.  **Licences** : Vérification des headers MIT.
3.  **Conteneurisation** : Lancement de 5 conteneurs Docker en parallèle.
4.  **Isolation** : Chaque JDK exécute la suite complète de tests JUnit 5.

```mermaid
sequenceDiagram
    participant M as Makefile
    participant S as ci-local.sh
    participant D as Docker Engine
    participant J as JDK Containers (8-25)

    M->>S: Lancer certification
    S->>D: Build images (si nécessaire)
    par JDK 8 to 25
        S->>D: run container
        D->>J: mvn test
        J-->>S: Exit Code (0/1)
    end
    S-->>M: Résultat Global
```

---

## 3. Matrice de Validation GitHub Actions

Le workflow `.github/workflows/maven.yml` est le gardien du repository. Il refuse tout merge qui ne respecte pas les critères de qualité.

### Les "Quality Gates" :
*   **Checkstyle** : Rigueur du code (nommage, structure).
*   **PMD** : Détection des mauvaises pratiques et bugs potentiels.
*   **Maven Enforcer** : **CRITIQUE** - Interdit toute dépendance externe (Jackson, Nimbus, etc.) au runtime.
*   **PITest** : Mutation Testing pour vérifier la pertinence des tests unitaires.

---

## 4. Flux de Release Semi-Automatique

Nous utilisons les **Conventional Commits** (`feat:`, `fix:`) pour piloter la version.

```mermaid
stateDiagram-v2
    [*] --> DevCommit: git commit -m 'feat: ...'
    DevCommit --> PushMaster: push origin master
    PushMaster --> ReleaseTrigger: make release (gh dispatch)
    
    state ReleaseTrigger {
        AnalyseCommits --> BumpVersion: Patch/Minor/Major
        BumpVersion --> UpdatePoms: mvn versions:set
        UpdatePoms --> GenChangelog: conventional-changelog
        GenChangelog --> CreateTag: git tag vX.Y.Z
    }
    
    CreateTag --> Publish: Create GitHub Release
    Publish --> [*]: JARs + Javadoc dispos
```

### Pourquoi "Semi-Automatique" ?
Le calcul de la version et la génération du changelog sont gérés par l'IA du workflow, mais le déclenchement est **manuel** (`workflow_dispatch`). Cela permet de grouper plusieurs fonctionnalités dans une seule release.

---

## 5. Artefacts Unifiés

Contrairement aux builds standards, ScribeJava v9.1+ produit un **JAR unique par module** :
*   **Structure** : Les `.class` et la documentation HTML (`/apidocs`) sont dans le même fichier.
*   **Avantage** : Une portabilité totale et une aide contextuelle immédiate dans les IDE sans téléchargement supplémentaire.

---
*Dernière mise à jour : Février 2026 - Certifié Enterprise Ready* ✅
