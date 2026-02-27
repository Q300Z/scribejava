# 🗺️ Guide des Flux Architecturaux (Enterprise Edition)

Ce document détaille les mécanismes internes de ScribeJava v9.2.x, illustrant comment le module `core` et le module `oidc` collaborent pour gérer le cycle de vie des identités.

---

## 1. Flux de Connexion OIDC (Login)
Ce flux intègre la **Découverte Dynamique**, le **PKCE** et la **Validation Native** de l'ID Token.

```mermaid
sequenceDiagram
    participant App as Application Client
    participant Disc as OidcDiscoveryService
    participant Service as OidcService
    participant IdP as Fournisseur d'Identité (Google/MS)

    Note over App, IdP: 1. PHASE DE DÉCOUVERTE (Discovery)
    App->>Disc: getProviderMetadata()
    Disc->>IdP: GET /.well-known/openid-configuration
    IdP-->>Disc: JSON Metadata (Endpoints, Keys URI)
    Disc-->>App: OidcProviderMetadata

    Note over App, IdP: 2. PHASE D'AUTORISATION (PKCE)
    App->>Service: createAuthorizationUrlBuilder().initPKCE()
    Service-->>App: PKCE (Verifier + Challenge)
    App->>IdP: Redirection Navigateur (URL avec code_challenge)
    IdP-->>App: Redirection avec ?code=XYZ

    Note over App, IdP: 3. ÉCHANGE ET VALIDATION
    App->>Service: getAccessToken(AuthorizationCodeGrant + Verifier)
    Service->>IdP: POST /token (code + code_verifier)
    IdP-->>Service: OAuth2AccessToken (incluant ID Token)
    Service->>App: token
    App->>Service: validateIdToken(token, nonce)
    Service->>Service: IdTokenValidator.validate()
    Service-->>App: IdToken (Claims décodés)
```

**Classes Clés :**
*   `OidcDiscoveryService` : Gère l'appel au point de terminaison de découverte.
*   `AuthorizationUrlBuilder` : Orchestre la construction de l'URL et l'initialisation du `PKCE`.
*   `AuthorizationCodeGrant` : Stratégie d'échange du code.
*   `IdTokenValidator` : Valide la signature cryptographique (RSA/EC) et les claims (`iss`, `aud`, `exp`).

---

## 2. Flux de Déconnexion (Logout)
ScribeJava supporte la déconnexion hybride : technique (révocation) et utilisateur (fin de session).

```mermaid
sequenceDiagram
    participant App as Application Client
    participant Service as OidcService
    participant IdP as Fournisseur d'Identité

    Note over App, IdP: MÉTHODE 1 : RÉVOCATION TECHNIQUE (Back-Channel)
    App->>Service: revokeToken(accessToken, TokenTypeHint.ACCESS_TOKEN)
    Service->>IdP: POST /revocation (token + client_auth)
    IdP-->>Service: 200 OK (Jeton invalidé)

    Note over App, IdP: MÉTHODE 2 : FIN DE SESSION (Front-Channel)
    App->>App: OidcSessionHelper.getLogoutUrl()
    App->>IdP: Redirection Navigateur (end_session_endpoint)
    IdP-->>App: Redirection vers post_logout_redirect_uri
```

**Points de terminaison impliqués :**
*   **Revocation Endpoint** : Défini par `metadata.getRevocationEndpoint()`.
*   **End Session Endpoint** : URL vers laquelle l'utilisateur est redirigé pour le Logout SSO.

---

## 3. Flux de Renouvellement (Refresh Token)
Gestion de la persistance sans intervention de l'utilisateur.

```mermaid
sequenceDiagram
    participant App as Application Client
    participant Service as OidcService
    participant Grant as RefreshTokenGrant
    participant IdP as Fournisseur d'Identité

    App->>Service: refreshAccessToken(refreshToken)
    Service->>Grant: createRequest(service)
    Grant->>IdP: POST /token (grant_type=refresh_token)
    IdP-->>Service: New OAuth2AccessToken
    Service-->>App: token
```

**Classes Clés :**
*   `RefreshTokenGrant` : Encapsule les paramètres `refresh_token` et `client_id/secret`.
*   `OAuth2AccessToken` : Contient le nouveau jeton et sa date d'expiration calculée via `getExpiresAt()`.

---

## 4. Orchestration Enterprise (Integration Helpers)
Le module `integration-helpers` automatise la gestion du cycle de vie des jetons et la sécurité CSRF, évitant ainsi au développeur de manipuler manuellement les jetons.

### 4.1. Fin de Flux Orchestrée (Callback)
Le `OidcAuthFlowCoordinator` centralise toutes les validations de retour.

```mermaid
sequenceDiagram
    participant Browser as Navigateur
    participant App as Contrôleur Applicatif
    participant Coord as OidcAuthFlowCoordinator
    participant Service as OidcService
    participant Repo as TokenRepository
    participant IdP as Fournisseur d'Identité

    Browser->>App: GET /callback?code=XYZ&state=ABC
    App->>Coord: finishAuthorization(userId, code, state, context)
    
    rect rgb(240, 240, 240)
    Note over Coord: 1. Validation CSRF Automatique
    Coord->>Coord: validateState(received, expected)
    end

    Coord->>Service: getAccessToken(Grant + PKCE Verifier)
    Service->>IdP: POST /token
    IdP-->>Service: OAuth2AccessToken
    
    rect rgb(240, 240, 240)
    Note over Coord: 2. Validation OIDC Automatique
    Coord->>Service: validateIdToken(token, nonce)
    Service-->>Coord: IdToken
    end

    Coord->>Repo: save(userId, ExpiringTokenWrapper)
    Coord-->>App: OidcAuthResult (Token + Claims consolidés)
    App-->>Browser: Redirection vers l'espace membre
```

### 4.2. Appel API avec Auto-Renouvellement
Le `AuthorizedClientService` exécute des requêtes sans que le développeur ne se soucie de l'expiration du jeton.

```mermaid
sequenceDiagram
    participant Business as Logique Métier
    participant Client as AuthorizedClientService
    participant Renewer as TokenAutoRenewer
    participant Repo as TokenRepository
    participant Service as OidcService
    participant IdP as Fournisseur d'Identité

    Business->>Client: execute(userId, request)
    Client->>Renewer: getValidToken(userId)
    Renewer->>Repo: findByKey(userId)
    Repo-->>Renewer: ExpiringTokenWrapper

    alt Jeton Expiré (ou proche de l'expiration)
        Note over Renewer: Verrouillage Thread-Safe (Lock par userId)
        Renewer->>Service: refreshAccessToken(refreshToken)
        Service->>IdP: POST /token (grant_type=refresh_token)
        IdP-->>Service: New OAuth2AccessToken
        Renewer->>Repo: save(userId, newWrapper)
        Note over Renewer: Déverrouillage
    end

    Renewer-->>Client: OAuth2AccessToken valide
    Client->>Service: signRequest(token, request)
    Client->>Service: execute(request)
    Service-->>Client: Response
    Client-->>Business: Response
```

---

## 📊 Comparaison : Standard vs Orchestré

| Caractéristique | Flux Standard (`core`) | Flux Orchestré (`helpers`) |
| :--- | :--- | :--- |
| **Gestion du State** | Manuelle (Session) | Automatique via `AuthFlowCoordinator` |
| **Validation PKCE** | Manuelle (Stockage verifier) | Automatique via `AuthSessionContext` |
| **Refresh Token** | Manuel (Vérification `isExpired`) | Transparent via `TokenAutoRenewer` |
| **Concurrence** | Risque de Race Condition | Thread-safe avec verrouillage par clé |
| **Stockage** | Code spécifique à l'app | Abstraction via `TokenRepository` |

---
## 🛠️ Matrice des Méthodes Enterprise

| Opération | Classe | Méthode | Standard |
| :--- | :--- | :--- | :--- |
| **Discovery** | `OidcDiscoveryService` | `getProviderMetadata()` | RFC 8414 / OIDC Disc |
| **PKCE** | `AuthorizationUrlBuilder` | `initPKCE()` | RFC 7636 |
| **Signature** | `OAuth20RequestSigner` | `signRequest(token, req)` | RFC 6750 |
| **Révocation** | `OAuth20Service` | `revokeToken(token, hint)` | RFC 7009 |
| **DPoP** | `DPoPProofCreator` | `createDPoPProof(req, token)` | RFC 9449 |

---

## 5. Résilience Industrielle (Retry Policy)
Le moteur de ScribeJava intègre une boucle de résilience capable de gérer les erreurs transitoires (Rate Limit, Instabilité serveur).

```mermaid
graph TD
    Start[OAuthService.execute] --> Req[Exécution de la requête]
    Req --> Resp{Code de réponse ?}
    Resp -- "2xx / 4xx (hors 429)" --> Success[Retourne la réponse]
    Resp -- "429 / 5xx" --> Retry{RetryPolicy active ?}
    Retry -- Non --> Success
    Retry -- Oui --> Check{Max attempts atteint ?}
    Check -- Oui --> Success
    Check -- Non --> Wait[Attente delayMs x exponential]
    Wait --> Req
```

---

## 6. Observabilité et Diagnostic (Redaction)
La sécurité est maintenue même dans les logs grâce au mécanisme de masquage automatique des secrets.

```mermaid
sequenceDiagram
    participant App as Logique Métier
    participant Req as OAuthRequest
    participant Log as OAuthLogger / cURL
    
    App->>Req: signRequest(token)
    App->>Req: toCurlCommand() / toDebugString()
    Note over Req: Analyse des headers & params
    Req->>Req: Identifie 'Authorization', 'secret', 'token'
    Req->>Req: Remplace les valeurs par [REDACTED]
    Req-->>Log: Chaîne sécurisée pour les logs
```

---

## 7. Cryptographie Native OIDC (Validation JWT)
ScribeJava n'utilise pas de bibliothèque externe (Zéro-Dépendance) pour valider les signatures.

```mermaid
flowDiagram
    ID[ID Token String] --> Parse[Jwt.parse: Base64 Decoding]
    Parse --> Header[Header: kid, alg]
    Parse --> Payload[Claims: iss, sub, aud, exp]
    Header --> Lookup{Recherche kid dans JWKS}
    Lookup -- Trouvé --> Crypto[Signature.verify: java.security]
    Lookup -- Absent --> Refresh[Rotation: Rechargement JWKS]
    Refresh --> Crypto
    Crypto --> Claims[Validation temporelle: iat < now < exp]
    Claims --> Final[IdToken Validé]
```

---

## 8. Authentification Client (Pattern Strategy)
Flexible pour s'adapter à toutes les exigences des fournisseurs.

```mermaid
graph LR
    Service[OAuth20Service] --> Strategy{ClientAuthentication}
    Strategy -- Default --> Basic[HttpBasic: Header Authorization: Basic base64]
    Strategy -- Azure/Custom --> Body[RequestBody: params client_id & client_secret]
```

---

## 9. Pipeline d'Intercepteurs (Extensibilité)
Modification modulaire des requêtes avant l'envoi.

```mermaid
graph LR
    Start[Requête Brute] --> I1[Interceptor 1: Headers Custom]
    I1 --> I2[Interceptor 2: PKCE Injection]
    I2 --> I3[Interceptor 3: DPoP Proof]
    I3 --> Exec[Exécution Client HTTP]
```

---

## 10. Liaison Cryptographique DPoP (RFC 9449)
Lien indissociable entre le jeton et la clé privée du client.

```mermaid
sequenceDiagram
    participant App as Application
    participant DPoP as DPoPProofCreator
    participant IdP as Fournisseur d'Identité

    Note over App, DPoP: Génération de Preuve (chaque appel)
    App->>DPoP: createDPoPProof(request, accessToken)
    DPoP->>DPoP: Hash(accessToken) -> 'ath' claim
    DPoP->>DPoP: Sign JWT(htm, htu, ath, jwk_public)
    DPoP-->>App: En-tête 'DPoP: signed_jwt'
    App->>IdP: Request + Jeton Bearer + En-tête DPoP
    IdP->>IdP: Vérifie la signature du JWT avec la clé publique fournie
    IdP->>IdP: Vérifie que 'ath' correspond au jeton envoyé
```

---

## 11. Pushed Authorization Requests (PAR - RFC 9126)
Le flux le plus sécurisé pour l'initiation : les paramètres ne passent plus par l'URL du navigateur.

```mermaid
sequenceDiagram
    participant App as Application
    participant Builder as AuthorizationUrlBuilder
    participant Handler as OAuth20PushedAuthHandler
    participant IdP as Fournisseur d'Identité

    App->>Builder: build() avec usePAR=true
    Builder->>Handler: pushAuthorizationRequestAsync(params)
    Handler->>IdP: POST /par (params + Client Auth)
    IdP-->>Handler: 201 Created (request_uri, expires_in)
    Handler-->>Builder: PushedAuthorizationResponse
    Builder->>Builder: Génère URL courte : ?request_uri=...
    Builder-->>App: URL d'autorisation sécurisée
```

---

## 12. Polling du Device Flow (RFC 8628)
Gestion intelligente de l'attente active pour les terminaux IoT/Console.

```mermaid
graph TD
    Start[Début du Polling] --> Req[POST /token grant_type=device_code]
    Req --> Resp{Réponse IdP ?}
    Resp -- "200 OK" --> Success[Retourne AccessToken]
    Resp -- "400 authorization_pending" --> Wait[Attente intervalSeconds]
    Resp -- "400 slow_down" --> SD[Augmentation interval +5s]
    Resp -- "400 access_denied / Autre" --> Fail[Lève OAuthResponseException]
    Wait --> Req
    SD --> Wait
```

---

## 13. Découverte du Client HTTP (ServiceLoader)
Comment ScribeJava maintient son autonomie "Zero-Dependency" au runtime.

```mermaid
graph LR
    Service[OAuthService Constructor] --> SL[java.util.ServiceLoader]
    SL --> Look[Cherche HttpClientProvider.class]
    Look -- "Trouvé (OkHttp/Armeria)" --> Load[Instancie l'implémentation externe]
    Look -- "Non trouvé" --> Default[Instancie JDKHttpClient natif]
    Load --> Ready[Service prêt avec HttpClient optimisé]
    Default --> Ready
```

---

## 14. JWT-Secured Authorization Request (JAR - RFC 9101)
Encapsulation cryptographique de la demande d'autorisation.

```mermaid
sequenceDiagram
    participant Builder as AuthorizationUrlBuilder
    participant Conv as JarAuthorizationRequestConverter
    participant Sign as RequestObjectService
    
    Builder->>Conv: convert(params_plats)
    Conv->>Sign: createRequestObject(params)
    Sign->>Sign: Signe JWT (Params -> Claims)
    Sign-->>Conv: signed_jwt
    Conv-->>Builder: Map(request = signed_jwt, client_id = ...)
    Builder->>Builder: Génère URL : ?request=eyJhbG...
```

---

## 15. Enregistrement Dynamique (RFC 7591)
Auto-provisioning pour les environnements de confiance ou de test.

```mermaid
sequenceDiagram
    participant App as Logique Métier
    participant Reg as OidcRegistrationService
    participant IdP as Fournisseur d'Identité

    App->>Reg: registerClientAsync(metadata_client)
    Reg->>Reg: JsonBuilder.build()
    Reg->>IdP: POST /registration (Content-Type: application/json)
    IdP->>IdP: Valide et crée le client
    IdP-->>Reg: 201 Created (client_id, client_secret, ...)
    Reg-->>App: Map des identifiants générés
```

---

## 16. Orchestration Multi-Tenant (OAuthServiceRegistry)
Centralisation de la gestion de multiples fournisseurs et clients au sein d'une même instance applicative.

```mermaid
graph LR
    User[UserId] --> Reg[OAuthServiceRegistry]
    Reg --> Map{Lookup by providerId}
    Map -- "google" --> ACS1[AuthorizedClientService G]
    Map -- "github" --> ACS2[AuthorizedClientService GH]
    Map -- "ms-entra" --> ACS3[AuthorizedClientService MS]
    ACS1 --> S1[OAuth20Service]
    ACS2 --> S2[OAuth20Service]
    ACS3 --> S3[OAuth20Service]
```

---

## 17. Moteur JSON Natif (Zéro-Dépendance)
Analyse récursive via expressions régulières pour garantir l'autonomie totale du runtime.

```mermaid
graph TD
    JSON[JSON String] --> Loop[JsonUtils.parse loop]
    Loop --> Match{Regex Pattern Match}
    Match -- "Pair Key:Value" --> Type{Type de Valeur ?}
    Type -- "String/Number/Bool" --> Map[Ajout à la Map courante]
    Type -- "Object { ... }" --> Rec[Appel Récursif (max depth 32)]
    Type -- "Array [ ... ]" --> Arr[Analyse de la liste]
    Rec --> Loop
    Arr --> Loop
    Map --> Loop
    Loop -- "Fin de chaîne" --> Result[Map finale d'objets Java]
```

---

## 18. Système d'Audit et d'Événements (Hooks)
Points d'ancrage pour le monitoring et la traçabilité métier.

```mermaid
sequenceDiagram
    participant App as Logique Métier
    participant Service as OAuth20Service
    participant Hook as OAuthEventListener
    participant Net as Client HTTP

    App->>Service: execute(request)
    Service->>Hook: onTokenRequested(request)
    Service->>Net: Envoi réseau
    Net-->>Service: Réponse
    Service->>Hook: onTokenReceived(token)
    Service-->>App: Response
    
    Note over Service, Hook: En cas d'échec
    Service->>Hook: onError(exception)
```

---

## 19. Pont des Adaptateurs HTTP (Bridge Pattern)
Découplage entre les modèles ScribeJava et les bibliothèques tierces.

```mermaid
graph LR
    SR[Scribe Request] --> Bridge[HttpClient Implementation]
    Bridge --> Native[Native Model]
    
    subgraph OkHttp
    Native1[okhttp3.Request]
    end
    
    subgraph Armeria
    Native2[com.linecorp.armeria.HttpRequest]
    end

    Bridge -- "okhttp-adaptor" --> Native1
    Bridge -- "armeria-adaptor" --> Native2
    Native1 -- "Execute" --> Resp1[okhttp3.Response]
    Resp1 --> SResp[Scribe Response]
```

---

## 20. Stratégie de Cache de Découverte OIDC
Optimisation des performances et réduction de la charge réseau IdP.

```mermaid
graph TD
    Start[OidcDiscoveryCache.getMetadata] --> Check{Présent en cache ?}
    Check -- Oui --> Return[Retourne les métadonnées]
    Check -- Non --> Lock[Verrouillage par issuerUri]
    Lock --> Check2{Toujours absent ?}
    Check2 -- Non --> Return
    Check2 -- Oui --> Fetch[Appel Réseau /.well-known]
    Fetch --> Store[Mise en cache thread-safe]
    Store --> Return
```

---
[⬅️ Retour au README principal](../README.md)
