/*
 * The MIT License
 *
 * Copyright (c) 2010 Pablo Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.scribejava.apis.examples.quickstart;

import static com.github.scribejava.apis.examples.quickstart.QuickStartUtils.config;
import static com.github.scribejava.apis.examples.quickstart.QuickStartUtils.readInput;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.integration.AuthorizedClientService;
import com.github.scribejava.core.integration.ExpiringTokenWrapper;
import com.github.scribejava.core.integration.TokenAutoRenewer;
import com.github.scribejava.core.integration.TokenRepository;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * [QUICKSTART] Orchestration avec les Helpers.
 *
 * <p>Cet exemple montre le niveau de service ultime : 1. Utilisation d'un Repository pour stocker
 * les jetons. 2. Rafraîchissement AUTOMATIQUE et transparent (AutoRenewer). 3. Exécution d'appels
 * API sans manipuler les jetons manuellement.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class HelpersQuickStart {

  private static final String CLIENT_ID = config("SCRIBE_CLIENT_ID", "votre_client_id");
  private static final String CLIENT_SECRET = config("SCRIBE_CLIENT_SECRET", "votre_client_secret");

  private HelpersQuickStart() {}

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {

    final OAuth20Service service =
        new ServiceBuilder(CLIENT_ID)
            .apiSecret(CLIENT_SECRET)
            .defaultScope("read:user")
            .build(GitHubApi.instance());

    // 1. On définit un dépôt de jetons (ici en mémoire pour l'exemple)
    final TokenRepository<String, ExpiringTokenWrapper> repository = new InMemoryRepo();

    // 2. On initialise le Renouveleur Automatique
    final TokenAutoRenewer<String> renewer =
        new TokenAutoRenewer<>(
            repository,
            oldToken -> {
              try {
                System.out.println("[INFO] >> Rafraîchissement automatique du jeton...");
                return service.refreshAccessToken(oldToken.getRefreshToken());
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });

    // 3. On crée le Client Autorisé (La couche d'abstraction finale)
    final AuthorizedClientService<String> client = new AuthorizedClientService<>(service, renewer);

    System.out.println("=== QuickStart : Orchestration Industrielle ===");

    // --- PHASE D'INITIALISATION (Une seule fois) ---
    final String userId = "user_123";
    final String authUrl = service.getAuthorizationUrl();
    System.out.println("Connectez-vous : " + authUrl);
    final String code = readInput("Code de redirection");

    final OAuth2AccessToken initialToken = service.getAccessToken(code);
    repository.save(userId, new ExpiringTokenWrapper(initialToken));

    // --- PHASE D'EXÉCUTION (Le développeur ne voit plus le jeton) ---
    System.out.println("\nExécution d'un appel API orchestré...");

    final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");

    // Magie : execute() va chercher le jeton, vérifier s'il est expiré,
    // le rafraîchir si besoin, signer la requête et l'envoyer.
    try (Response response = client.execute(userId, request)) {
      System.out.println("Réponse : " + response.getCode());
      System.out.println(response.getBody());
    }
  }

  /** Implémentation simple en mémoire pour l'exemple. */
  private static class InMemoryRepo implements TokenRepository<String, ExpiringTokenWrapper> {
    private final Map<String, ExpiringTokenWrapper> store = new HashMap<>();

    @Override
    public Optional<ExpiringTokenWrapper> findByKey(String key) {
      return Optional.ofNullable(store.get(key));
    }

    @Override
    public void save(String key, ExpiringTokenWrapper token) {
      store.put(key, token);
    }

    @Override
    public void deleteByKey(String key) {
      store.remove(key);
    }
  }
}
