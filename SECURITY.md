# Politique de Sécurité - ScribeJava

Nous prenons la sécurité de ScribeJava très au sérieux. Si vous découvrez une vulnérabilité de sécurité, nous vous remercions de nous la signaler de manière responsable afin que nous puissions la corriger avant qu'elle ne soit rendue publique.

---

## 📞 Signaler une Vulnérabilité

**Ne créez pas de ticket (Issue) public pour signaler une vulnérabilité.**

Pour signaler une faille de sécurité :
1. Envoyez un e-mail détaillé à l'adresse de l'équipe de sécurité : **security@scribejava.org** (ou contactez directement l'un des mainteneurs principaux).
2. Pour les failles hautement critiques, vous pouvez chiffrer vos communications à l'aide de notre clé PGP publique (disponible sur les serveurs de clés publics avec l'ID d'empreinte `0xDEADBEEF...`).
3. Veuillez inclure dans votre e-mail :
   * Une description détaillée de la vulnérabilité.
   * Les étapes de reproduction ou un exploit d'exemple (Proof of Concept).
   * L'impact potentiel de la faille.

Nous accusons réception de votre signalement sous **48 heures** et nous efforçons de fournir un correctif dans un délai de **15 à 30 jours**.

---

## 🛡️ Versions Supportées

Seules les versions listées ci-dessous reçoivent activement des correctifs de sécurité :

| Version | Supportée | Niveau de correctifs |
| :--- | :---: | :--- |
| **9.x** | **✓ Oui** | Failles critiques, modérées et améliorations générales |
| **8.x** | **✓ Oui** | Failles de sécurité critiques uniquement (Fin de support fin 2026) |
| **< 8.0** | ✗ Non | Plus supportée (Mise à jour fortement recommandée) |

---

## 🔒 Nos Principes de Sécurité

ScribeJava est conçue dès le départ comme une bibliothèque hautement sécurisée :
1. **Zéro Dépendance Transitive** : Réduit à néant le risque d'attaques sur la chaîne d'approvisionnement (Supply Chain Attacks).
2. **Masquage Automatique des Logs** : Les implémentations de `OAuthLogger` (comme `DefaultOAuthLogger`) censurent automatiquement les secrets (`access_token`, `client_secret`, etc.) pour éviter les fuites involontaires dans les fichiers de journaux système.
3. **Regex Robustes** : Toutes les expressions régulières utilisées dans le parseur JSON natif et les utilitaires sont conçues de manière stricte pour éviter les crashs de déni de service par épuisement de ressources (Regex DoS).
4. **Validation stricte OIDC** : Le validateur `IdTokenValidator` impose des vérifications cryptographiques strictes et des validations de temps/nonce conformes aux spécifications OpenID Connect 1.0 Core.
