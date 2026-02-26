# 🔐 ScribeJava OIDC (Enterprise)

Implémentation native du protocole OpenID Connect 1.0. 100% autonome (aucune dépendance Nimbus/Jackson).

## 🌟 Fonctionnalités Industrielles
- **Configuration Magique** : Fournissez uniquement l'URL de l'émetteur (ex: `https://accounts.google.com`), le builder s'occupe de tout.
- **Résilience** : Cache interne des métadonnées OIDC et des clés publiques.
- **Sécurité** : Support de l'auto-rotation des clés JWK et validation native RSA/EC.
- **Fluent Claims** : Accès typé aux claims standard (email, given_name, etc.).

## 🚀 Quick Start
```java
// Plus besoin de configurer les endpoints manuellement !
OidcServiceBuilder builder = new OidcServiceBuilder(clientId)
    .baseOnDiscovery("https://accounts.google.com", httpClient, userAgent);

OAuth20Service service = builder.build(new DefaultOidcApi20());

// Utilisation typée des données
IdToken idToken = service.extractIdToken(token);
String email = idToken.getStandardClaims().getEmail().orElse("inconnu");
```
