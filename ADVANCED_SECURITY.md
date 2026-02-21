# 🛡️ Sécurité Avancée : DPoP & PAR

ScribeJava v9 intègre les derniers standards de sécurité de l'IETF pour protéger vos échanges OAuth 2.0.

---

## 🔐 1. DPoP (Demonstrating Proof-of-Possession)
Le DPoP empêche l'utilisation d'un jeton volé en le liant mathématiquement à une paire de clés privée/publique détenue par le client.

### Mise en œuvre
```java
// 1. Créez un générateur de preuve (fourni dans le module OIDC)
DefaultDPoPProofCreator proofCreator = new DefaultDPoPProofCreator(myKeyPair);

// 2. Configurez le service
OAuth20Service service = new ServiceBuilder(clientId)
    .apiSecret(secret)
    .dpopProofCreator(proofCreator) // Active DPoP pour tous les échanges
    .build(GoogleApi20.instance());

// Les jetons obtenus seront de type 'DPoP' et les requêtes signées automatiquement.
```

---

## 🛰️ 2. PAR (Pushed Authorization Requests)
Le PAR (RFC 9126) améliore la sécurité en envoyant les paramètres d'autorisation directement au serveur via une requête POST sécurisée, plutôt que de les faire transiter par l'URL du navigateur.

### Mise en œuvre
```java
// ScribeJava gère le PAR automatiquement si l'API le supporte
PushedAuthorizationResponse parResponse = service.pushAuthorizationRequest(new AuthorizationCodeGrant(code));

// Redirigez l'utilisateur vers l'URI fournie par le serveur
String authUrl = parResponse.getRequestUri();
```

---

## 💡 Recommandations
1.  **Toujours utiliser PKCE** : Même pour les clients confidentiels (serveur).
2.  **Rotation des clés** : Changez vos clés DPoP régulièrement.
3.  **Scopes limités** : Ne demandez que le strict nécessaire.

---

## ✅ Checklist de Sécurité (Mise en Production)

Avant de déployer votre intégration, vérifiez ces 5 points :

1.  [ ] **PKCE Activé** : Utilisez-vous `.pkce(true)` dans votre `ServiceBuilder` ?
2.  [ ] **HTTPS Uniquement** : Vos redirections (`callback`) et endpoints sont-ils tous en HTTPS ?
3.  [ ] **Secret protégé** : Votre `apiSecret` est-il chargé via une variable d'environnement (et non en dur) ?
4.  [ ] **Validation ID Token** : Si vous utilisez OIDC, validez-vous systématiquement le jeton (Signature + `iss` + `aud`) ?
5.  [ ] **DPoP (Si sensible)** : Pour les APIs critiques (paiement, santé), avez-vous activé le DPoP pour lier le jeton à votre client ?

---
[⬅️ Retour à l'accueil](./README.md)
