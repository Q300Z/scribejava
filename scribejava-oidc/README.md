# 🔐 Support OpenID Connect (OIDC)

Ce module fournit une implémentation complète et sécurisée du protocole OpenID Connect 1.0.

---

## 🌟 Fonctionnalités Clés

* **Auto-découverte (Discovery)** : Récupération dynamique des endpoints via `/.well-known/openid-configuration`.
* **Validation d'ID Token** : Vérification rigoureuse de la signature (RS256, etc.), de l'émetteur (`iss`), de
  l'audience (`aud`) et de l'expiration (`exp`).
* **Gestion des JWKS** : Support de la rotation des clés publiques du fournisseur.
* **UserInfo** : Récupération et parsing des claims utilisateur (email, profile, etc.).

---

## 🚀 Exemple de Flux Complet

Voici comment implémenter un flux OIDC standard (Discovery + Auth + Validation) en production :

```java
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.oidc.DefaultOidcApi20;
import com.github.scribejava.oidc.DefaultOidcKeyCache;
import com.github.scribejava.oidc.IdToken;
import com.github.scribejava.oidc.IdTokenValidator;
import com.github.scribejava.oidc.OidcDiscoveryService;
import com.github.scribejava.oidc.OidcProviderMetadata;
import com.github.scribejava.oidc.OidcService;
import com.github.scribejava.oidc.OidcServiceBuilder;
import com.github.scribejava.oidc.OidcSessionState;

// 1. Initialisation avec Auto-découverte (Google par exemple)
OidcDiscoveryService discovery = new OidcDiscoveryService(
    "https://accounts.google.com", 
    new JDKHttpClient(), 
    "ScribeJava"
);
OidcProviderMetadata metadata = discovery.getProviderMetadata();

OidcService service = new OidcServiceBuilder(clientId)
    .apiSecret(clientSecret)
    .callback(callbackUrl)
    .defaultScope("openid profile email")
    .build(new DefaultOidcApi20(metadata));

// 2. Gestion sécurisée de l'état de session (génère et persiste state, nonce et PKCE verifier)
OidcSessionState sessionState = service.initSessionState();
String authUrl = service.getAuthorizationUrl(sessionState);

// 3. Échange du code d'autorisation contre des jetons (avec PKCE et validation automatique)
// Le nonce est validé automatiquement par rapport à l'état stocké lors du succès
OAuth2AccessToken token = service.getAccessToken(code, sessionState.getState());

// 4. Extraction de l'ID Token validé
IdToken idToken = service.validateIdToken(token, sessionState.getNonce());
System.out.println("Utilisateur authentifié : " + idToken.getStandardClaims().getSub().orElse(""));
```

## 🛡️ Sécurité & OIDC

L'utilisation d'OpenID Connect nécessite une sécurité renforcée :

* Consultez le guide de **[Durcissement de Sécurité en Production (Security Hardening)](../docs/how-to/security-hardening.md)** pour configurer au mieux vos environnements distribués.
* Utilisez systématiquement le **PKCE** et le flux de session natif de ScribeJava.

---

## ⚙️ Configuration & Mécanismes de Durcissement

### 1️⃣ Cache de Clés Personnalisé (`OidcKeyCache`)

Par défaut, ScribeJava conserve les clés JWKS du fournisseur en mémoire vive via `DefaultOidcKeyCache`. En production multi-instance, vous pouvez brancher un cache partagé (ex. Redis) :

```java
public class RedisOidcKeyCache implements OidcKeyCache {
    private final JedisPool jedisPool;

    public RedisOidcKeyCache(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public OidcKey get(String kid) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get("jwks:key:" + kid);
            return json != null ? deserializeKey(json) : null;
        }
    }

    @Override
    public void putAll(Map<String, OidcKey> keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            for (Map.Entry<String, OidcKey> entry : keys.entrySet()) {
                jedis.setex("jwks:key:" + entry.getKey(), 86400, serializeKey(entry.getValue()));
            }
        }
    }

    @Override
    public void clear() {
        // Vider le cache si nécessaire
    }
}
```

Puis passez-le au constructeur de votre validateur :

```java
IdTokenValidator validator = new IdTokenValidator(
    metadata.getIssuer(),
    clientId,
    "RS256",
    new RedisOidcKeyCache(jedisPool),
    discovery,
    metadata.getJwksUri()
);
service.setIdTokenValidator(validator);
```

### 2️⃣ Validation Multi-Tenant Dynamique (`IssuerValidator`)

Pour gérer les IDPs multi-tenant (ex. Microsoft Entra ID ou Okta) sans correspondances en dur :

```java
IdTokenValidator validator = new IdTokenValidator(
    "https://login.microsoftonline.com/{tenantid}/v2.0",
    clientId,
    "RS256",
    new DefaultOidcKeyCache(),
    discovery,
    metadata.getJwksUri()
);

// DefaultIssuerValidator résout automatiquement {tenantid} via le claim `tid`
validator.setIssuerValidator(new DefaultIssuerValidator());
```

Vous pouvez aussi implémenter un validateur d'émetteurs d'entreprise personnalisé :

```java
validator.setIssuerValidator((configured, claimIssuer, claims) -> {
    return claimIssuer.startsWith("https://identity.mycompany.com/");
});
```

### 3️⃣ Magasin d'État de Session (`OidcSessionStateStore`)

Pour corréler le `state`, le `nonce` et le `code_verifier` (PKCE) en cluster distribué, implémentez un magasin partagé :

```java
public class RedisOidcSessionStateStore implements OidcSessionStateStore {
    private final JedisPool jedisPool;

    @Override
    public void save(OidcSessionState state) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex("oidc:session:" + state.getState(), 300, serialize(state));
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

Associez-le à votre service OIDC :

```java
service.setSessionStateStore(new RedisOidcSessionStateStore(jedisPool));
```

### 4️⃣ Fournisseurs & Algorithmes JCA (`SignatureVerifier`)

Pour valider les signatures des ID Tokens via des modules de sécurité matériels (HSM) ou des fournisseurs cryptographiques spécifiques :

```java
DefaultSignatureVerifier verifier = new DefaultSignatureVerifier();
// Enregistrement d'un fournisseur JCA (ex. BouncyCastle)
verifier.setProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
// Enregistrement d'un algorithme personnalisé
verifier.registerAlgorithm("CUSTOM-RS256", "SHA256withRSA");

validator.setSignatureVerifier(verifier);
```

### 5️⃣ Résilience Réseau & Retries

Protégez les appels d'auto-découverte (Discovery) et d'interrogation de clés JWKS contre les pannes temporaires :

```java
OidcDiscoveryService discovery = new OidcDiscoveryService(issuer, httpClient, userAgent);

// Timeouts
discovery.setConnectTimeout(5000); // 5 secondes
discovery.setReadTimeout(5000);

// Tentatives de retry automatiques non-bloquantes avec Exponential Backoff
discovery.setMaxAttempts(4);
discovery.setInitialDelayMs(500L);
discovery.setBackoffMultiplier(2.0); // 500ms, 1000ms, 2000ms...
```

### 6️⃣ Limitation de Débit Anti-DoS (`kid` Inconnus)

Pour éviter de saturer le serveur JWKS lors d'injections de `kid` inexistants (attaques DoS) :

* ScribeJava applique un **cooldown de 5 minutes** après chaque échec de résolution d'un `kid` donné.
* Un **cooldown global de 5 minutes** limite la fréquence maximale de rechargement réseau du JWKS (`reloadKeys`).

---

## 🚀 Utilisation Avancée

### Validation manuelle d'un ID Token

```java
IdToken idToken = new IdToken(rawIdToken);
IdTokenValidator validator = new IdTokenValidator(
    expectedIssuer,
    clientId,
    "RS256",
    new DefaultOidcKeyCache()
);
// Validation avec nonce et maxAuthAgeSeconds (0 pour ignorer)
validator.validate(idToken, new OidcNonce(expectedNonce), 3600);
```

### Gestion des Claims (UserInfo)

```java
DefaultOidcApi20 api = (DefaultOidcApi20) service.getApi();
OAuthRequest request = new OAuthRequest(Verb.GET, api.getMetadata().getUserinfoEndpoint());
service.signRequest(token, request);

try (Response response = service.execute(request)) {
    UserInfoJsonExtractor extractor = UserInfoJsonExtractor.instance();
    Map<String, Object> claims = extractor.extract(response.getBody());
    System.out.println("Email : " + claims.get("email"));
}
```

---
[🏠 Accueil](../README.md) | [🔌 APIs](../scribejava-apis/README.md) | [🔐 OIDC](../scribejava-oidc/README.md) | [🛡️ Sécurité](../docs/how-to/security-hardening.md)
