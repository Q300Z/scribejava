# 🛡️ Sécurité Avancée : DPoP & PAR

ScribeJava v9 intègre les derniers standards de sécurité de l'IETF pour protéger vos échanges OAuth 2.0.

---

## 🔐 1. DPoP (Demonstrating Proof-of-Possession)

Le DPoP empêche l'utilisation d'un jeton volé en le liant mathématiquement à une paire de clés privée/publique détenue
par le client.

### Mise en œuvre DPoP standard (Clé RSA éphémère)

ScribeJava intègre nativement le support de DPoP et injecte automatiquement l'en-tête `DPoP` requis lors de l'obtention et de l'utilisation du jeton.

```java
// 1. Créez un générateur de preuve (génère une clé RSA éphémère par défaut)
DefaultDPoPProofCreator proofCreator = new DefaultDPoPProofCreator();

// 2. Configurez le service en passant le créateur au builder (.dpop)
OAuth20Service service = new ServiceBuilder(clientId)
    .apiSecret(secret)
    .dpop(proofCreator) // Active DPoP de manière native
    .build(GoogleApi20.instance());

// Les jetons obtenus seront de type 'DPoP' et les requêtes signées automatiquement.
```

### Utilisation avec une paire de clés existante (KeyPair)

Si vous possédez déjà une paire de clés persistante, vous pouvez la passer au constructeur détaillé de `DefaultDPoPProofCreator` :

```java
// Instanciation avec votre clé privée et clé publique existante
DefaultDPoPProofCreator proofCreator = new DefaultDPoPProofCreator(
    myKeyPair.getPrivate(),
    myKeyPair.getPublic(),
    new com.github.scribejava.oidc.model.JwtSigner.RsaSha256Signer()
);

OAuth20Service service = new ServiceBuilder(clientId)
    .apiSecret(secret)
    .dpop(proofCreator)
    .build(GoogleApi20.instance());
```

---

## 🛰️ 2. PAR (Pushed Authorization Requests)

Le PAR (RFC 9126) améliore la sécurité en envoyant les paramètres d'autorisation directement au serveur via une requête
POST sécurisée, plutôt que de les faire transiter par l'URL du navigateur.

### Mise en œuvre PAR

```java
// ScribeJava gère le PAR de manière transparente lors de l'appel au builder de l'URL
String authUrl = service.createAuthorizationUrlBuilder()
    .state(state)
    .usePushedAuthorizationRequests(true) // Active PAR : pousse les paramètres et génère l'URL avec request_uri
    .build();
```

---

## 💡 Recommandations

1. **Toujours utiliser PKCE** : Même pour les clients confidentiels (serveur).

2. **Rotation des clés** : Changez vos clés DPoP régulièrement.

3. **Scopes limités** : Ne demandez que le strict nécessaire.

---

## ✅ Checklist de Sécurité (Mise en Production)

Avant de déployer votre intégration, vérifiez ces 5 points :

1. [ ] **PKCE Activé** : Appelez-vous `initPKCE()` sur l' `AuthorizationUrlBuilder` pour générer le challenge PKCE ?

2. [ ] **HTTPS Uniquement** : Vos redirections (`callback`) et endpoints sont-ils tous en HTTPS ?

3. [ ] **Secret protégé** : Votre `apiSecret` est-il chargé via une variable d'environnement (et non en dur) ?

4. [ ] **Validation ID Token** : Si vous utilisez OIDC, validez-vous systématiquement le jeton (Signature + `iss` +
    `aud`) ?

5. [ ] **DPoP (Si sensible)** : Pour les APIs critiques (paiement, santé), avez-vous activé le DPoP pour lier le jeton
    à votre client ?

## 4. JWT Secured Authorization Request (JAR) - RFC 9101

Pour empêcher l'altération des paramètres de la requête d'autorisation (ex: injection de `redirect_uri` malveillant),
vous pouvez signer la requête.

### Activation

Utilisez `JarAuthorizationRequestConverter` (du module `scribejava-oidc`) dans le builder.

```java

// Clé privée standard JDK pour signer le JWT
PrivateKey privateKey = ...;

OAuth20Service service = new ServiceBuilder("client-id")
    .apiSecret("secret")
    .callback("https://callback.com")
    // Active JAR : Convertit les params en JWT signé avec JDK PrivateKey
    .authorizationRequestConverter(
        new JarAuthorizationRequestConverter(
            "client-id", // issuer
            "https://accounts.google.com", // audience (l'IDP)
            privateKey,
            "kid-1",
            new com.github.scribejava.oidc.model.JwtSigner.RsaSha256Signer()
        )
    )
    .build(OidcGoogleApi20.instance());

// L'URL générée contiendra ?client_id=...&request=eyJ...
String url = service.createAuthorizationUrlBuilder().build();

```

### Combinaison avec PAR (Pushed Authorization Requests)

Si vous activez à la fois JAR et PAR, ScribeJava enverra le JWT signé (`request`) directement au serveur via l'appel API
PAR (`POST /par`). C'est la configuration **la plus sécurisée** possible.

```java

// JAR + PAR = Sécurité Maximale
OAuth20Service service = new ServiceBuilder("client-id")
    .authorizationRequestConverter(new JarAuthorizationRequestConverter(...)) // JAR
    .build(GoogleApi20.instance());

// Génère l'URL d'autorisation : applique JAR pour signer et pousse via PAR
String authUrl = service.createAuthorizationUrlBuilder()
    .state(state)
    .usePushedAuthorizationRequests(true)
    .build();

```

---

## 🏗️ 5. Resource Indicators (RFC 8707)

Si votre serveur d'autorisation gère plusieurs ressources (APIs) avec des permissions distinctes, vous pouvez spécifier la ressource cible lors de la demande de jeton.

### Mise en œuvre

```java
// Pour spécifier des paramètres additionnels comme la ressource cible (RFC 8707),
// étendez ou créez une instance personnalisée de OAuth20Grant :
OAuth20Grant grant = new AuthorizationCodeGrant(code) {
    @Override
    public OAuthRequest createRequest(OAuth20Service service) {
        OAuthRequest request = super.createRequest(service);
        request.addParameter("resource", "https://api.monservice.com/v1");
        return request;
    }
};

OAuth2AccessToken token = service.getAccessToken(grant);
```

---
[⬅️ Retour à l'accueil](https://github.com/Q300Z/scribejava/blob/master/README.md)
