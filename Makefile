# Makefile pour ScribeJava v9.1+ "Enterprise Edition"
# Automatisation, Observabilité et Certification multi-JDK

.PHONY: help build test test-parallel lint pitest install doc release clean format certify ci snapshot

# --- Variables ---
VERSION_CURRENT := $(shell grep -m 1 "<version>" pom.xml | sed 's/[^0-9.]//g' | sed 's/-SNAPSHOT//')
NEXT_PATCH := $(shell echo $(VERSION_CURRENT) | awk -F. '{print $$1"."$$2"."$$3+1}')

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
	mvn javadoc:aggregate -Dmaven.test.skip=true

# --- Release (Basée sur les Tags) ---

release: ## Crée une release par Tag (ex: make release VER=9.2.0)
ifndef VER
	@echo "\033[33mInfo: VER non spécifié, utilisation de la prochaine version suggérée : $(NEXT_PATCH)\033[0m"
	$(eval VER=$(NEXT_PATCH))
endif
	@echo "\033[32m🚀 Préparation de la release v$(VER)...\033[0m"
	mvn versions:set -DnewVersion=$(VER) -DgenerateBackupPoms=false
	mvn versions:commit
	git add .
	git commit -m "chore: release v$(VER)"
	git tag -a v$(VER) -m "Release version $(VER)"
	@echo "\033[32m✅ Tag v$(VER) créé localement.\033[0m"
	@echo "Pour publier : git push origin master v$(VER)"

snapshot: ## Repasse en version de développement (ex: make snapshot NEXT=9.2.1-SNAPSHOT)
ifndef NEXT
	$(eval NEXT=$(shell echo $(VER) | awk -F. '{print $$1"."$$2"."$$3+1"-SNAPSHOT"}'))
endif
	mvn versions:set -DnewVersion=$(NEXT) -DgenerateBackupPoms=false
	mvn versions:commit
	git add .
	git commit -m "chore: prepare for next development iteration ($(NEXT))"
	@echo "\033[32m✅ Version de travail passée à $(NEXT)\033[0m"

clean: ## Nettoie les dossiers target et les logs de CI
	mvn clean
	rm -rf target/ ci-logs/
