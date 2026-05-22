# Makefile pour ScribeJava v9.1+ "Enterprise Edition"
# Automatisation, Observabilité et Certification multi-JDK

.PHONY: help build test test-parallel lint pitest install doc release release-local clean format certify ci sync

# --- Variables ---
VERSION_CURRENT := $(shell grep -m 1 "<version>" pom.xml | sed 's/[^0-9.]//g' | sed 's/-SNAPSHOT//')
MKDOCS := $(shell which mkdocs 2>/dev/null || echo "./venv/bin/mkdocs")

help: ## Affiche cette aide
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# --- Développement ---

build: ## Compile tous les modules (Zero-Dependency)
	mvn clean compile -Dmaven.javadoc.skip=true

test: ## Lance les tests unitaires (JUnit 5 + AssertJ)
	mvn test -Dmaven.javadoc.skip=true

test-parallel: ## Lance les tests en parallèle (1 thread par coeur)
	mvn test -T 1C -Dmaven.javadoc.skip=true

lint: ## Analyse statique (Checkstyle, PMD, Spotless, License)
	mvn checkstyle:check pmd:check spotless:check license:check

lint-docs: ## Analyse la qualité de la documentation (Markdown & Liens)
	@echo "\033[36m🔍 Lintage Markdown...\033[0m"
	@docker compose -f docker-compose.ci.yml run --rm markdownlint
	@echo "\033[36m🔍 Vérification des liens...\033[0m"
	@docker compose -f docker-compose.ci.yml run --rm lychee

format: ## Formate le code (Google Style) et répare les headers de licence
	mvn spotless:apply license:format

certify: format ## Formate et lance la certification multi-JDK locale (Nécessite Docker)
	@echo "\033[32m🧪 Lancement de la certification ScribeJava...\033[0m"
	./ci-local.sh

ci: ## Alias pour certify
	@$(MAKE) certify

pitest: ## Mutation Testing sur les modules critiques (Core & OIDC)
	mvn pitest:mutationCoverage -pl scribejava-core,scribejava-oidc

install: ## Installe les JARs dans le repo local .m2
	mvn clean install -DskipTests -Dmaven.javadoc.skip=true

doc: ## Génère la Javadoc agrégée (Premium DX)
	mvn install -DskipTests -Dmaven.javadoc.skip=true -Dspotless.check.skip=true -Dcheckstyle.skip=true -Dpmd.skip=true -Dlicense.skip=true
	mvn javadoc:aggregate -DskipTests -Dmaven.javadoc.skip=false -Dspotless.check.skip=true -Dcheckstyle.skip=true -Dpmd.skip=true -Dlicense.skip=true

doc-site: doc ## Génère le site de documentation MkDocs complet avec Javadoc intégrée
	@echo "\033[36m🏗️ Génération du site MkDocs...\033[0m"
	$(MKDOCS) build
	@echo "\033[36m🔌 Intégration de la Javadoc...\033[0m"
	cp -r target/classes/apidocs site/
	@echo "\033[32m✅ Site généré avec succès dans le dossier './site' !\033[0m"

deploy-site: doc-site ## Déploie manuellement le site de documentation sur la branche gh-pages
	@echo "\033[32m🚀 Déploiement du site de documentation sur gh-pages...\033[0m"
	cd site && git init && git add . && git commit -m "docs: manual deploy of documentation hub [skip ci]" && git remote add origin git@github.com:Q300Z/scribejava.git && git push origin master:gh-pages --force


# --- Release (Automatisation du cycle de vie via release-it) ---

release: ## Lance le cycle de release (Bump, Changelog, Tag, Push) - Déclenche ensuite le CI de publication
	@echo "\033[32m🚀 Lancement du processus de release avec release-it (pnpm)...\033[0m"
	@pnpm dlx release-it


sync: ## Récupère les derniers changements de release depuis GitHub
	git pull --rebase origin master
	git fetch --tags

clean: ## Nettoie les dossiers target et les logs de CI
	mvn clean
	rm -rf target/ ci-logs/
