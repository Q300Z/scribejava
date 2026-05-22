# Guide de Durcissement de Sécurité en Production (Security Hardening)

Ce guide fournit les meilleures pratiques indispensables pour configurer et déployer **ScribeJava** de manière hautement sécurisée en environnement de production. 

En tant que bibliothèque d'authentification et d'autorisation, une mauvaise configuration de son intégration peut exposer votre application à des failles critiques (attaques de l'homme du milieu, vols de jetons, attaques par rejeu).

---

## 1. Sécurisation du Transport (SSL / TLS)

### 🔴 Ne JAMAIS désactiver la vérification SSL
Durant le développement, il est tentant de désactiver la validation des certificats SSL pour travailler avec des serveurs locaux ou auto-signés. **Cette pratique est strictement proscrite en production**, sous peine de rendre votre application vulnérable aux attaques de l'homme du milieu (MitM).

```java
// ⚠️ DANGER CRITIQUE - NE JAMAIS FAIRE CELA EN PRODUCTION :
HttpClientConfig config = new HttpClientConfig();
config.setNoSSLVerification(true); // À PROSCRIRE !
```

### 🟢 Comment gérer les Autorités de Certification (CA) privées ?
Si votre entreprise utilise un serveur d'identité interne sécurisé par un certificat privé, n'utilisez pas l'option ci-dessus. Importez plutôt le certificat de l'autorité de certification (CA) racine dans le magasin de confiance de votre JVM (`cacerts`) :

```bash
keytool -importcert -trustcacerts -file mon-ca-interne.crt -alias monCA -keystore $JAVA_HOME/lib/security/cacerts
```

---

## 2. Prévention des fuites de secrets dans les Logs

Les jetons d'accès (`access_token`), d'actualisation (`refresh_token`) et les secrets clients (`client_secret`) sont des données ultra-sensibles équivalentes à des mots de passe.

### 🟢 Utilisez toujours le masquage automatique
Si vous activez le mode débogage ou utilisez des loggers réseau, assurez-vous d'utiliser le système d'observabilité sécurisé de ScribeJava :

* **`DefaultOAuthLogger`** effectue un filtrage automatique très performant sur les corps de requêtes et réponses pour remplacer les jetons et secrets par la chaîne de censure `[MASKED]`.
* Si vous écrivez une implémentation personnalisée de `OAuthLogger` (ex. pour SLF4J), déléguez le nettoyage du corps à `DefaultOAuthLogger` ou implémentez un filtre regex équivalent.

```java
// Exemple d'implémentation personnalisée sécurisée :
public class SecureSlf4jLogger implements OAuthLogger {
    private static final Logger LOG = LoggerFactory.getLogger(SecureSlf4jLogger.class);
    private final DefaultOAuthLogger sanitizer = new DefaultOAuthLogger();

    @Override
    public void logRequest(OAuthRequest request) {
        LOG.debug("Requête HTTP signée : {}", request.toDebugString());
    }

    @Override
    public void logResponse(Response response) {
        try {
            // Nettoyage automatique avant écriture dans SLF4J
            String safeBody = sanitizer.sanitizeResponseBody(response.getBody());
            LOG.debug("Réponse HTTP (Code {}), Corps : {}", response.getCode(), safeBody);
        } catch (IOException e) {
            LOG.error("Erreur de lecture du corps", e);
        }
    }
}
```

---

## 3. Validation stricte du State et du Nonce en environnement distribué

Les paramètres `state` (OAuth 2.0) et `nonce` (OpenID Connect) sont vos barrières majeures contre les attaques de type **Cross-Site Request Forgery (CSRF)** et **attaques par rejeu (Replay)**.

### 🔴 L'erreur classique : Le stockage local en mémoire mono-instance
Si votre application tourne sur plusieurs conteneurs ou instances derrière un Load Balancer (architecture Kubernetes, multi-instances Cloud), stocker les valeurs `state` et `nonce` dans la session HTTP en mémoire locale (In-Memory) provoquera des échecs d'authentification aléatoires si l'utilisateur est redirigé vers une autre instance, ou pire, vous obligera à activer des sessions persistantes (Sticky Sessions), ce qui nuit à la scalabilité.

### 🟢 La solution : Cache partagé éphémère (ex: Redis)
Stockez le `state` et le `nonce` dans un magasin de données partagé (comme un serveur Redis ou Memcached) avec une **durée de vie très courte (Time-To-Live - TTL)** de **5 à 10 minutes maximum** :

1. L'utilisateur clique sur "Connexion" : vous générez un `state` unique et cryptographiquement sûr, et vous le stockez dans Redis associé à l'ID de session de l'utilisateur : `SET EX oauth2:state:<session_id> <state_value> 300` (expire dans 5 minutes).
2. Au retour du callback : vous comparez le `state` fourni par l'URL avec celui de Redis, puis vous le **supprimez immédiatement** pour empêcher qu'il soit réutilisé (`DEL oauth2:state:<session_id>`).

---

## 4. Durcissement d'OpenID Connect (ID Token)

La validation des ID Tokens ne doit souffrir d'aucune concession :

* **Toujours valider les signatures de manière stricte** : L'ID Token doit être validé cryptographiquement à l'aide de la clé publique correspondante au paramètre `kid` de l'en-tête, récupérée dynamiquement via l'URI `jwks_uri` du fournisseur.
* **Ne pas ignorer les exceptions** : Capturez et traitez toutes les sous-classes d' `OAuthException` jetées par `IdTokenValidator`. Si une validation échoue (ex. `Token has expired`), refusez catégoriquement l'accès à l'utilisateur.
* **Forcer des délais de décalage temporel (Clock Skew) très courts** : Bien qu'un léger décalage soit toléré pour compenser la désynchronisation des horloges de serveurs, n'acceptez jamais des jetons expirés depuis plus de 60 secondes.

---

## 5. Protection des clés privées DPoP (Proof of Possession)

Le protocole DPoP (RFC 9449) lie l' `Access Token` à une clé privée détenue par le client ScribeJava.

* **Cycle de vie des paires de clés** : Ne réutilisez pas la même paire de clés cryptographiques indéfiniment. Générez une nouvelle paire de clés pour chaque session utilisateur (ou effectuez une rotation fréquente de la paire de clés globale).
* **Isolation et stockage** : Ne stockez jamais de clés privées DPoP en clair dans des bases de données ou des fichiers système. Si elles doivent être persistées temporairement, utilisez un magasin de clés sécurisé certifié (Java KeyStore - JKS sécurisé par mot de passe robuste, ou un gestionnaire de secrets d'entreprise comme HashiCorp Vault / AWS Secrets Manager).
