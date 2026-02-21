# Makefile pour ScribeJava v9
# Automatisation du développement et des releases

.PHONY: help build test test-parallel lint pitest install doc release clean format snapshot

# --- Variables ---
VERSION_CURRENT := $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
NEXT_SNAPSHOT := $(shell echo $(VERSION_CURRENT) | awk -F. '{print $$1"."$$2"."$$3+1"-SNAPSHOT"}')

help: ## Affiche cette aide
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# --- Développement ---

build: ## Compile tous les modules
	mvn clean compile -Dmaven.javadoc.skip=true

test: ## Lance les tests unitaires
	mvn test -Dmaven.javadoc.skip=true

test-parallel: ## Lance les tests en parallèle (optimisé pour CI)
	mvn test -T 1C -Dmaven.javadoc.skip=true

lint: ## Vérifie le style (Checkstyle, PMD, Spotless, License)
	mvn checkstyle:check pmd:check spotless:check license:check

format: ## Formate automatiquement le code et ajoute les headers de licence
	mvn spotless:apply license:format

pitest: ## Lance le Mutation Testing sur les modules critiques
	mvn pitest:mutationCoverage -pl scribejava-core,scribejava-oidc

install: ## Installe les JARs dans le repo local .m2 sans lancer les tests
	mvn clean install -DskipTests -Dmaven.javadoc.skip=true

doc: ## Génère la Javadoc complète (agrégée)
	mvn javadoc:aggregate -Dmaven.test.skip=true

# --- Release ---

release: ## Prépare et crée une release GitHub (ex: make release VERSION=9.0.0)
ifndef VERSION
	@echo "\033[31mErreur: La variable VERSION est obligatoire (ex: make release VERSION=9.0.0)\033[0m"
	@exit 1
endif
	@echo "\033[32m🚀 Préparation de la release v$(VERSION)...\033[0m"
	mvn versions:set -DnewVersion=$(VERSION) -DgenerateBackupPoms=false
	git add .
	git commit -m "build: release v$(VERSION)"
	git tag -a v$(VERSION) -m "Release version $(VERSION)"
	@echo "\033[33m⚠️  Le tag v$(VERSION) est créé localement.\033[0m"
	@echo "Lancez 'git push origin master --tags' pour déclencher la CI de release."

snapshot: ## Repasse en version de développement (ex: make snapshot NEXT=9.0.1-SNAPSHOT)
ifndef NEXT
	@echo "\033[33mInfo: Passage automatique à la version $(NEXT_SNAPSHOT)\033[0m"
	$(eval NEXT=$(NEXT_SNAPSHOT))
endif
	mvn versions:set -DnewVersion=$(NEXT) -DgenerateBackupPoms=false
	git add .
	git commit -m "build: prepare for next development iteration ($(NEXT))"
	@echo "\033[32m✅ Version de travail passée à $(NEXT)\033[0m"

clean: ## Nettoie les fichiers de build
	mvn clean
	rm -rf target/
