# 🏗️ Documentation CI/CD ScribeJava

Ce document détaille l'infrastructure de Continuous Integration (CI) et Continuous Delivery (CD) de ScribeJava v9.2.4+. Notre pipeline est conçu pour garantir la règle **Zéro-Dépendance** et la stabilité sur une matrice de **5 versions de JDK**.

---

## 1. Vue d'ensemble de l'Architecture "Zero-Touch"

Le pipeline sépare strictement la préparation humaine (Local) de la certification et distribution industrielle (Cloud).

```mermaid
graph TD
    subgraph Local ["💻 Poste Développeur (LOCAL)"]
        Trigger[make release] --> CertPre[ci-local.sh: Multi-JDK Docker]
        CertPre --> DocLint[Audit Markdown & Liens]
        DocLint --> Bump[release-it: Bump SemVer & Tag Git]
        Bump --> Push[git push master + tags]
    end

    subgraph Cloud ["☁️ GitHub Actions (CLOUD)"]
        Push --> MavenCI[Workflow: maven.yml]
        MavenCI --> GHDocLint[Audit Markdown & Liens]
        subgraph "🧪 Matrice de Certification"
            MavenCI --> JDK8[JDK 8]
            MavenCI --> JDK11[JDK 11]
            MavenCI --> JDK17[JDK 17]
            MavenCI --> JDK21[JDK 21]
            MavenCI --> JDK25[JDK 25]
        end
        
        JDK8 & JDK11 & JDK17 & JDK21 & JDK25 & GHDocLint --> Success{Tests OK ?}
        Success -- Oui --> Publish[release-it: GitHub Release + JARs]
        Success -- Non --> Abort[❌ Release bloquée]
    end

    style Local fill:#f9f,stroke:#333,stroke-width:2px
    style Cloud fill:#bbf,stroke:#333,stroke-width:2px
```

---

## 2. Standardisation et Maintenance

L'intégrité du projet est maintenue par des processus automatisés :

*   **Zero-Dependency Enforcement** : Le plugin `maven-enforcer` bloque le build si une dépendance interdite (Jackson, Nimbus, org.json) est détectée au runtime.
*   **Pure Java Certification** : Depuis la v9.2.4, le module `integration-helpers` est certifié sans aucune dépendance de logging (retrait de SLF4J), garantissant une portabilité totale sans conflit de classpath.
*   **Dependabot** : Surveillance hebdomadaire des vulnérabilités.

---

## 3. Cycle de Release Industrielle (`release-it`)

Nous utilisons **`release-it`** comme moteur unique pour garantir un versionnage sémantique (SemVer) sans erreur humaine.

### Le Triple Verrou de Sécurité :
1.  **Verrou Local** : `make release` lance `./ci-local.sh`. La release s'arrête si un test casse sur un des 5 JDKs.
2.  **Verrou Cloud** : Le workflow `maven.yml` ré-exécute l'intégralité de la matrice sur les serveurs GitHub pour audit.
3.  **Verrou de Publication** : La commande `release-it --github.release` n'est invoquée que si les tests cloud sont un succès total.

---

## 4. Documentation et Artefacts "Premium DX"

*   **Javadoc Automatisée (`deploy-docs.yml`)** : Publication continue sur GitHub Pages.
*   **JARs Unifiés** : Les JARs de distribution incluent les `.class`, les sources et la documentation agrégée.
*   **Typed Builders** : Les builders OIDC retournent désormais nativement le type `OidcService`, éliminant les `ClassCastException` et améliorant l'expérience développeur.

---

## 5. Matrice de Certification multi-JDK

Le script `ci-local.sh` est le garant de la robustesse :

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
*Dernière mise à jour : Mars 2026 - Certifié Enterprise Edition v9.2.4* ✅
