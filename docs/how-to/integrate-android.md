# 📱 Guide d'intégration sur Android

Grâce à sa compatibilité stricte avec le **JDK 8** (voir [ADR 002](../adr/002-jdk8-compatibility.md)) et son architecture **Zero-Dependency** au runtime, ScribeJava est extrêmement légère, performante et parfaitement adaptée au développement d'applications mobiles Android.

Ce guide explique comment configurer et intégrer ScribeJava dans une application Android, en respectant les bonnes pratiques de sécurité modernes (PKCE, Custom Tabs, et exécution asynchrone).

---

## 🎯 1. Pourquoi utiliser ScribeJava sur Android ?

1. **Légèreté absolue** : Les bibliothèques d'authentification d'entreprise (comme Spring Security, Pac4j ou AppAuth) pèsent souvent plusieurs dizaines de mégaoctets et importent des centaines de dépendances. Le Core de ScribeJava pèse moins de **300 Ko**, ce qui limite l'impact sur la taille de votre APK.
2. **Compatibilité Totale** : Fonctionne sur toutes les versions d'Android (de l'API 19 à la plus récente) sans nécessiter de mécanisme lourd de désucrage (desugaring) du bytecode.
3. **Transport Flexible** : Vous pouvez utiliser le client HTTP natif du JDK ou injecter le client standard d'Android : **OkHttp 4**, assurant un excellent partage des pools de connexion et des performances réseaux optimales.

---

## 🛠️ 2. Configuration et Dépendances

Dans le fichier `build.gradle` (module `app`) de votre projet Android, ajoutez les dépendances suivantes. Il est fortement recommandé d'utiliser le module de transport **OkHttp** de ScribeJava sur Android :

```groovy
dependencies {
    // ScribeJava Core
    implementation 'com.github.scribejava:scribejava-core:9.4.0'
    // Transport OkHttp recommandé pour Android
    implementation 'com.github.scribejava:scribejava-httpclient-okhttp:9.4.0'
    
    // Pour l'ouverture sécurisée du navigateur (Android Custom Tabs)
    implementation 'androidx.browser:browser:1.8.0'
}
```

---

## 🔑 3. Configuration du Redirection Link (Deep Linking)

Pour recevoir le code d'autorisation après la connexion de l'utilisateur, vous devez déclarer un filtre d'intention (Intent Filter) dans votre **`AndroidManifest.xml`** lié à l'activité qui gérera le callback :

```xml
<activity android:name=".AuthCallbackActivity"
          android:exported="true">
    
    <!-- Filtre d'intention pour capturer le Deep Link de redirection -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        
        <!-- Définit votre redirect URI : myapp://oauth-callback -->
        <data android:scheme="myapp" android:host="oauth-callback" />
    </intent-filter>
</activity>
```

---

## 🚀 4. Exemple d'Intégration Complet

Sur Android, **toutes les requêtes réseau sont interdites sur le Thread Principal (UI Thread)** sous peine de lever une exception `NetworkOnMainThreadException`. Nous devons exécuter les appels ScribeJava de manière asynchrone (via Executors, Coroutines Kotlin ou RxJava).

Voici l'implémentation complète d'une activité initiant le flux avec **GitHub** et le sécurisant avec **PKCE** (fortement recommandé sur mobile) :

### Étape A : L'activité de connexion (`LoginActivity.java`)

```java
package com.mycompany.myapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.httpclient.okhttp.OkHttpHttpClientConfig;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    // Service OAuth20 et Executor pour les tâches en arrière-plan
    private OAuth20Service oauthService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Configuration de ScribeJava avec le client OkHttp
        oauthService = new ServiceBuilder("votre-client-id")
                .callback("myapp://oauth-callback")
                .httpClientConfig(OkHttpHttpClientConfig.defaultConfig())
                .build(GitHubApi.instance());

        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(v -> initiateOAuthFlow());
    }

    private void initiateOAuthFlow() {
        // 2. Génération de l'URL d'autorisation en arrière-plan avec PKCE
        executor.execute(() -> {
            com.github.scribejava.core.oauth.AuthorizationUrlBuilder builder = oauthService.createAuthorizationUrlBuilder()
                    .initPKCE(); // ✅ Active et génère automatiquement le challenge PKCE
            
            // Stockez builder.getPkce().getCodeVerifier() de manière persistante (ex. SharedPreferences)
            // pour l'utiliser lors de la phase de callback.
            saveCodeVerifier(builder.getPkce().getCodeVerifier());

            final String authUrl = builder.build();

            // 3. Retour sur le thread principal pour lancer Custom Tabs
            runOnUiThread(() -> {
                CustomTabsIntent.Builder tabsBuilder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = tabsBuilder.build();
                // Ouvre le navigateur sécurisé natif
                customTabsIntent.launchUrl(LoginActivity.this, Uri.parse(authUrl));
            });
        });
    }

    private void saveCodeVerifier(String verifier) {
        // Exemple : stocker dans les SharedPreferences ou en mémoire de l'application
    }
}
```

---

### Étape B : L'activité de capture du callback (`AuthCallbackActivity.java`)

Une fois la connexion réussie, Android capture la redirection et lance l'activité associée avec les paramètres `code` et `state` dans l'Intent.

```java
package com.mycompany.myapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.AuthorizationCodeGrant;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthCallbackActivity extends AppCompatActivity {

    private OAuth20Service oauthService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callback);
        tvStatus = findViewById(R.id.tv_status);

        oauthService = new ServiceBuilder("votre-client-id")
                .callback("myapp://oauth-callback")
                .build(GitHubApi.instance());

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith("myapp://oauth-callback")) {
            // Extraction du code d'autorisation retourné par le serveur
            final String code = uri.getQueryParameter("code");

            if (code != null) {
                exchangeCodeForToken(code);
            } else {
                Toast.makeText(this, "Erreur : Code non trouvé dans la redirection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exchangeCodeForToken(String code) {
        tvStatus.setText("Échange du code d'autorisation contre le jeton...");

        // Exécution en arrière-plan obligatoire
        executor.execute(() -> {
            try {
                // 1. Échange du code d'autorisation avec PKCE
                final AuthorizationCodeGrant grant = new AuthorizationCodeGrant(code);
                // Récupérez le code verifier stocké lors de l'initiation de l'authentification
                final String codeVerifier = getStoredCodeVerifier();
                grant.setPkceCodeVerifier(codeVerifier);

                final OAuth2AccessToken accessToken = oauthService.getAccessToken(grant);
                
                // 2. Appel immédiat d'une ressource signée pour test
                final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");
                oauthService.signRequest(accessToken, request);
 
                try (Response response = oauthService.execute(request)) {
                    final String userBody = response.getBody();
                    
                    // Mise à jour de l'interface graphique sur le thread principal
                    runOnUiThread(() -> tvStatus.setText("Profil récupéré :\n" + userBody));
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> tvStatus.setText("Erreur d'authentification : " + e.getMessage()));
            }
        });
    }

    private String getStoredCodeVerifier() {
        // Exemple : récupérer depuis les SharedPreferences
        return "verifier-sauvegarde";
    }
}
```

---

## 🔒 5. Bonnes Pratiques de Sécurité sur Mobile

1. **Utilisez obligatoirement PKCE (via `initPKCE()`)** : Les applications mobiles sont des clients dits "publics" qui ne peuvent pas stocker de `client_secret` de manière sécurisée (toute clé dans l'APK peut être extraite par ingénierie inverse). L'utilisation de PKCE (en appelant `.initPKCE()` sur l' `AuthorizationUrlBuilder` et en passant le code verifier à l' `AuthorizationCodeGrant`) protège votre application contre l'interception de code d'autorisation par des applications malveillantes sur le même appareil.
2. **Bannissez les WebViews intégrées** : N'affichez jamais la page de connexion dans une `WebView` classique intégrée à votre application. Les serveurs d'autorisation (comme Google ou Microsoft) les bloquent souvent pour prévenir le phishing. Privilégiez toujours les **Android Custom Tabs** (`androidx.browser:browser`), qui partagent les cookies de session du navigateur système, évitant ainsi à l'utilisateur de ressaisir ses identifiants s'il est déjà connecté.
