# Guide de Durcissement de Sécurité en Production (Security Hardening)

Ce guide fournit les meilleures pratiques indispensables pour configurer et déployer **ScribeJava** de manière hautement sécurisée en environnement de production.

En tant que bibliothèque d'authentification et d'autorisation, une mauvaise configuration de son intégration peut exposer votre application à des failles critiques (attaques de l'homme du milieu, vols de jetons, attaques par rejeu).

---

## 1. Sécurisation du Transport (SSL / TLS)

### 🔴 Ne JAMAIS désactiver la vérification SSL

Durant le développement, il est tentant de désactiver la validation des certificats SSL pour travailler avec des serveurs locaux ou auto-signés. **Cette pratique est strictement proscrite en production**, sous peine de rendre votre application vulnérable aux attaques de l'homme du milieu (MitM).

JDKHttpClientConfig config = new JDKHttpClientConfig() {
    @Override
    public javax.net.ssl.SSLSocketFactory getSslSocketFactory() {
        return myTrustingSSLSocketFactory;
    }
};
config.withHostnameVerifier(myTrustingHostnameVerifier);
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

## 3. Validation stricte du State et du Nonce via OidcSessionStateStore

Les paramètres `state` (OAuth 2.0) et `nonce` (OpenID Connect) sont vos barrières majeures contre les attaques de type **Cross-Site Request Forgery (CSRF)** et **attaques par rejeu (Replay)**.

### 🔴 L'erreur classique : Le stockage local en mémoire mono-instance

Si votre application tourne sur plusieurs conteneurs ou instances derrière un Load Balancer (architecture Kubernetes, multi-instances Cloud), stocker les valeurs `state` et `nonce` (et le PKCE `code_verifier`) dans la session HTTP en mémoire locale (In-Memory) provoquera des échecs d'authentification aléatoires si l'utilisateur est redirigé vers une autre instance, ou vous contraindra à activer des sessions persistantes (Sticky Sessions), ce qui nuit à la scalabilité et à la tolérance aux pannes.

### 🟢 La solution ScribeJava : L'interface OidcSessionStateStore

ScribeJava automatise l'ensemble du cycle de vie via l'interface `OidcSessionStateStore`. Celle-ci stocke l'objet unifié `OidcSessionState` (qui regroupe le `state`, le `nonce` et le PKCE `code_verifier`).

Pour sécuriser un environnement distribué, implémentez un magasin partagé éphémère (ex: basé sur Redis avec TTL) :

```java
import com.github.scribejava.oidc.OidcSessionState;
import com.github.scribejava.oidc.OidcSessionStateStore;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisOidcSessionStateStore implements OidcSessionStateStore {
    private final JedisPool jedisPool;
    private static final int TTL_SECONDS = 300; // 5 minutes max

    public RedisOidcSessionStateStore(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public void save(OidcSessionState state) {
        try (Jedis jedis = jedisPool.getResource()) {
            // Sérialisation de l'état (par ex. en JSON ou binaire)
            String serialized = serialize(state);
            jedis.setex("oidc:session:" + state.getState(), TTL_SECONDS, serialized);
        }
    }

    @Override
    public OidcSessionState load(String stateVal) {
        try (Jedis jedis = jedisPool.getResource()) {
            String val = jedis.get("oidc:session:" + stateVal);
            return val != null ? deserialize(val) : null;
        }
    }

    @Override
    public void remove(String stateVal) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("oidc:session:" + stateVal);
        }
    }
}
```

#### Utilisation dans le flux

Associez simplement votre implémentation au service. ScribeJava s'occupe de persister les états générés, de vérifier les nonces au retour et de purger les jetons du cache sur validation :

```java
// Configuration
service.setSessionStateStore(new RedisOidcSessionStateStore(jedisPool));

// 1. Initialisation (génère et persiste l'état)
OidcSessionState sessionState = service.initSessionState();
String authUrl = service.getAuthorizationUrl(sessionState);

// 2. Échange et validation (vérifie le PKCE, corrèle le nonce de l'ID Token, supprime l'état du cache)
OAuth2AccessToken token = service.getAccessToken(authCode, sessionState.getState());
```

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

---

## 6. Guide de Configuration des Mécanismes Avancés de Sécurité (OIDC)

Pour sécuriser vos déploiements de production, ScribeJava OIDC intègre des interfaces extensibles et des protections contre le déni de service.

### A. Cache de Clés partagé (`OidcKeyCache`)

Pour éviter des rechargements intensifs et redondants de clés JWKS par vos différentes instances d'application, implémentez un cache partagé (ex. Redis) et passez-le au constructeur de `IdTokenValidator` :

```java
// Exemple d'utilisation d'un cache partagé avec IdTokenValidator
OidcKeyCache customCache = new RedisOidcKeyCache(jedisPool);
IdTokenValidator validator = new IdTokenValidator(
    issuer,
    clientId,
    "RS256",
    customCache,
    discoveryService,
    jwksUri
);
```

### B. Validation Multi-Tenant Dynamique (`IssuerValidator`)

Pour les environnements entreprise multi-tenant (ex. Microsoft Azure Active Directory / Entra ID, Okta), configurez `DefaultIssuerValidator` qui prend en charge les templates d'issuer `{tenantid}` / `{tenant}` et résout dynamiquement le claim `tid` associé :

```java
// Utilisation du validateur dynamique par défaut
validator.setIssuerValidator(new DefaultIssuerValidator());
```

Vous pouvez également injecter votre propre logique de validation pour restreindre les locataires acceptés :

```java
validator.setIssuerValidator((configured, claimIssuer, claims) -> {
    // Validation personnalisée des émetteurs autorisés
    return claimIssuer.startsWith("https://login.microsoftonline.com/") 
        && claims.containsKey("tid") 
        && isAuthorizedTenant((String) claims.get("tid"));
});
```

### C. Signature & Fournisseurs JCA (`SignatureVerifier`)

Si vous utilisez des modules matériels (HSM) ou des bibliothèques externes comme Bouncy Castle pour la validation cryptographique des signatures, vous pouvez configurer le fournisseur JCA et enregistrer des algorithmes propriétaires :

```java
DefaultSignatureVerifier verifier = new DefaultSignatureVerifier();
// Configuration du provider JCA
verifier.setProvider(Security.getProvider("BC")); // ex: Bouncy Castle
verifier.registerAlgorithm("CUSTOM-RS256", "SHA256withRSA");

validator.setSignatureVerifier(verifier);
```

### D. Résilience Réseau (Timeouts & Retries)

Pour vous prémunir contre les coupures réseau temporaires lors de l'appel d'auto-découverte (OIDC Discovery) ou la récupération des clés JWKS, configurez les timeouts de connexion/lecture et le système de retries asynchrones avec backoff exponentiel sur `OidcDiscoveryService` :

```java
OidcDiscoveryService discoveryService = new OidcDiscoveryService(issuerUri, httpClient, userAgent);

// Configuration des timeouts HTTP (en ms)
discoveryService.setConnectTimeout(3000);
discoveryService.setReadTimeout(3000);

// Configuration du backoff exponentiel non-bloquant
discoveryService.setMaxAttempts(4);        // Jusqu'à 4 tentatives
discoveryService.setInitialDelayMs(500L);   // 500ms avant le 1er retry
discoveryService.setBackoffMultiplier(2.0); // Multiplier par 2 à chaque échec (500ms, 1000ms, 2000ms...)
```

### E. Protection Anti-DoS contre les clés (`kid`) Inconnues

Si un attaquant tente d'inonder votre application avec des ID Tokens falsifiés contenant des identifiants de clés (`kid`) inexistants, ScribeJava pourrait saturer le serveur JWKS en tentant de recharger continuellement les clés. Pour éviter cela, ScribeJava applique :

* Un **cooldown de 5 minutes** sur l'appel réseau de rechargement des clés (`reloadKeys`).
* Un **cooldown de 5 minutes par `kid` inconnu** : tout échec de validation d'un `kid` donné empêche d'autres tentatives de récupération de ce même `kid` sur le réseau pendant cette durée.
