# ADR 002 : Maintien de la Compatibilité JDK 8 pour le Core

*   **Statut** : Accepté
*   **Date** : 2026-05-22
*   **Auteurs** : Équipe ScribeJava

---

## 1. Contexte et Problème

Avec la sortie des versions modernes de Java (JDK 11, 17, 21, et 25), de nombreuses bibliothèques abandonnent le support des anciennes versions de Java. Cependant, ScribeJava est particulièrement populaire dans deux écosystèmes :
1.  **L'écosystème Android** : De nombreuses applications Android ciblent encore des niveaux d'API nécessitant la compatibilité avec le bytecode Java 8 ou refusant les API Java plus récentes sans desméthodage lourd.
2.  **Les applications héritées (Legacy)** : De nombreuses infrastructures d'entreprise tournent toujours sur des runtimes JDK 8.

ScribeJava doit équilibrer l'adoption de standards de développement modernes et le maintien d'une compatibilité maximale.

---

## 2. Décision d'Architecture

Nous décidons de maintenir la compatibilité du code source et du bytecode avec **JDK 8** pour le module principal `scribejava-core` et le module `scribejava-oidc`.

*   **Pas de syntaxes modernes exclusives au Core** : Le code du Core n'utilisera pas de fonctionnalités introduites après Java 8 (telles que les classes `Record`, les `var` en variables locales, ou les blocs de texte multilignes).
*   **Pipeline de CI multi-JDK** : La chaîne de tests continue validera à chaque commit la compilation et l'exécution des tests sur une matrice complète de JDK, allant de JDK 8 jusqu'à la dernière version en cours (JDK 25).
*   **Modules d'extension modernes** : Si des fonctionnalités requièrent des versions supérieures de Java (comme certains clients HTTP récents ou des cryptographies avancées), elles seront développées dans des modules optionnels à part, laissant le Core accessible en Java 8.

---

## 3. Conséquences

### Avantages (+)
*   **Portabilité maximale** : La bibliothèque peut être intégrée sans aucune friction sur n'importe quel projet Java moderne ou ancien, ainsi que sur toutes les versions d'Android.
*   **Stabilité** : Restreindre le langage aux structures classiques de Java 8 garantit un code simple, lisible et extrêmement stable dans le temps.

### Inconvénients (-)
*   **Privation des améliorations du langage** : Les développeurs de la bibliothèque ne peuvent pas utiliser les nouveautés syntaxiques facilitant l'écriture de code (comme le pattern matching, les records, ou la nouvelle API de switch).
*   **Code parfois plus verbeux** : Certaines structures de données et manipulations de chaînes de caractères restent plus verbeuses qu'elles ne le seraient en Java moderne.
