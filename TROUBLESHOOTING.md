# Guide de Dépannage

Solutions aux problèmes courants.

## 🌐 Erreurs SSL/TLS (Java 8)

### `javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure`
Ceci arrive généralement quand le serveur exige TLS 1.2 ou 1.3 et que votre environnement Java 8 a un fournisseur de sécurité obsolète.
*   **Solution** : Mettez à jour votre JDK vers la dernière version mineure (8u251 ou plus).
*   **Alternative** : Ajoutez `-Dhttps.protocols=TLSv1.2,TLSv1.3` aux arguments de votre JVM.

### `sun.security.validator.ValidatorException: PKIX path building failed`
Le serveur utilise un certificat d'une autorité non reconnue par votre JDK.
*   **Solution** : Importez le certificat du serveur dans le magasin `cacerts` de votre JDK via l'outil `keytool`.

## ✍️ Problèmes de Signature

### `OAuthSignatureException: Error while generating signature`
Pour OAuth 1.0a, vérifiez que votre `apiSecret` est correct. Si vous utilisez RSA-SHA1, vérifiez que votre clé privée est au format PKCS#8.

## 🐛 Débogage

Pour voir les requêtes et réponses HTTP brutes, activez le mode debug :
```java
OAuthService service = new ServiceBuilder(apiKey)
    .debug() // Log dans System.out
    .build(api);
```
Pour rediriger les logs vers **SLF4J**, fournissez un `OutputStream` personnalisé :
```java
.debugStream(new LoggingOutputStream(logger))
```
