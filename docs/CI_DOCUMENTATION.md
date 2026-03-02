# 🏗️ Documentation CI/CD ScribeJava

Ce document détaille l'infrastructure de Continuous Integration (CI) et Continuous Delivery (CD) de ScribeJava v9.2.4+. Notre pipeline est conçu pour garantir la règle **Zéro-Dépendance** et la stabilité sur une matrice de **5 versions de JDK**.

---

## 1. Vue d'ensemble de l'Architecture "Zero-Touch"

Le pipeline sépare strictement la préparation humaine (Local) de la certification et distribution industrielle (Cloud).

```mermaid
graph TD
    subgraph Local ["💻 Poste Développeur (LOCAL)"]
        Trigger[make release] --> CertPre[ci-local.sh: Multi-JDK Docker]
        CertPre --> Bump[release-it: Bump SemVer & Tag Git]
        Bump --> Push[git push master + tags]
    end

    subgraph Cloud ["☁️ GitHub Actions (CLOUD)"]
        Push --> MavenCI[Workflow: maven.yml]
        subgraph "🧪 Matrice de Certification"
            MavenCI --> JDK8[JDK 8]
            MavenCI --> JDK11[JDK 11]
            MavenCI --> JDK17[JDK 17]
            MavenCI --> JDK21[JDK 21]
            MavenCI --> JDK25[JDK 25]
        end
        
        JDK8 & JDK11 & JDK17 & JDK21 & JDK25 --> Success{Tests OK ?}
        Success -- Oui --> Publish[release-it: GitHub Release + JARs]
        Success -- Non --> Abort[❌ Release bloquée]
    end

    style Local fill:#f9f,stroke:#333,stroke-width:2px
    style Cloud fill:#bbf,stroke:#333,stroke-width:2px
```

---

## 2. Standardisation et Maintenance

L'intégrité du projet est maintenue par des processus automatisés de surveillance :

*   **Dependabot (`dependabot.yml`)** : Surveille les dépendances Maven (hebdomadaire) et les GitHub Actions (mensuel). Toute mise à jour génère une PR testée par le CI.
*   **Templates de Contribution** :
    *   `bug_report.md` : Force la fourniture du JDK et du client HTTP pour un diagnostic immédiat.
    *   `PULL_REQUEST_TEMPLATE.md` : Checklist imposant le respect des principes **SOLID**, du lintage et du Mutation Testing.

---

## 3. Cycle de Release Industrielle (`release-it`)

Nous utilisons **`release-it`** comme moteur unique pour garantir un versionnage sémantique (SemVer) sans erreur humaine.

### Le Triple Verrou de Sécurité :
1.  **Verrou Local** : `make release` lance `./ci-local.sh`. La release s'arrête si un test casse sur un des 5 JDKs.
2.  **Verrou Cloud** : Le workflow `maven.yml` ré-exécute l'intégralité de la matrice sur les serveurs GitHub pour audit.
3.  **Verrou de Publication** : La commande `release-it --github.release` n'est invoquée que si le point 2 est un succès total.

---

## 4. Documentation et Artefacts "Premium DX"

ScribeJava v9.2.4+ automatise la diffusion de la connaissance technique :

*   **Javadoc Automatisée (`deploy-docs.yml`)** : À chaque push sur `master`, la Javadoc agrégée est recompilée et publiée sur GitHub Pages.
*   **JARs Unifiés** : Le hook `before:github:release` de `release-it` produit des JARs contenant les `.class` et les sources de documentation pour une aide contextuelle immédiate dans les IDE.

---

## 5. Matrice de Certification multi-JDK

Le script `ci-local.sh` est le cœur battant de la robustesse du projet :

```mermaid
sequenceDiagram
    participant M as Makefile
    participant S as ci-local.sh
    participant D as Docker Engine
    participant J as JDK Containers (8-25)

    M->>S: Lancer certification
    S->>D: Build images (Isolées)
    par JDK 8 to 25
        S->>D: run container
        D->>J: mvn test
        J-->>S: Exit Code 0
    end
    S-->>M: Résultat Global (Certifié)
```

---
*Dernière mise à jour : Février 2026 - Certifié Enterprise Edition v9.2.4* ✅
