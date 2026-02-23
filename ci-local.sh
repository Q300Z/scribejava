#!/bin/bash

# Dossier pour les logs
LOG_DIR="ci-logs"

echo "🧹 Nettoyage des dossiers de sortie..."
# On utilise une image légère pour supprimer les fichiers créés par root dans les containers
docker run --rm -v "$(pwd):/app" eclipse-temurin:17-jdk-jammy rm -rf /app/docs-output/* /app/ci-logs/*
docker volume rm scribejava_docs-data > /dev/null 2>&1 || true

mkdir -p "$LOG_DIR"
mkdir -p "docs-output"

echo "🚀 Préparation : Installation locale des artefacts (JDK 17)..."
# Cette étape est cruciale pour que les modules puissent se voir entre eux dans le volume partagé
docker compose -f docker-compose.ci.yml run --rm test-jdk17 clean install -DskipTests -Dmaven.javadoc.skip=true > "$LOG_DIR/setup.log" 2>&1

echo "🚀 Lancement de la matrice CI..."
echo "📂 Les logs sont disponibles dans le dossier '$LOG_DIR'"

echo "🧐 Étape 1 : Lintage et Analyse statique (JDK 17)..."
docker compose -f docker-compose.ci.yml run --rm lint > "$LOG_DIR/lint.log" 2>&1
echo "  ✅ lint : SUCCESS"

echo "📚 Étape 2 : Génération de la documentation (JDK 17)..."
docker compose -f docker-compose.ci.yml run --rm docs > "$LOG_DIR/docs.log" 2>&1
echo "  ✅ docs : SUCCESS"
echo "  📂 Extraction de la documentation..."
docker run --rm -v scribejava_docs-data:/from -v "$(pwd)/docs-output:/to" eclipse-temurin:17-jdk-jammy cp -r /from/. /to/

echo "🧪 Étape 3 : Tests multi-JDK en parallèle..."
# Liste des services de test à exécuter
TEST_SERVICES=("test-jdk8" "test-jdk11" "test-jdk17" "test-jdk21" "test-jdk25")

declare -A PIDS
for SERVICE in "${TEST_SERVICES[@]}"; do
    echo "  ➡️  Démarrage de $SERVICE..."
    docker compose -f docker-compose.ci.yml run --rm "$SERVICE" > "$LOG_DIR/$SERVICE.log" 2>&1 &
    PIDS[$SERVICE]=$!
done

echo "⏳ Attente de la fin des tests..."

# Suivi des résultats
FAILED=0
for SERVICE in "${TEST_SERVICES[@]}"; do
    wait "${PIDS[$SERVICE]}"
    STATUS=$?
    if [ $STATUS -eq 0 ]; then
        echo "  ✅ $SERVICE : SUCCESS"
    else
        echo "  ❌ $SERVICE : FAILED (voir $LOG_DIR/$SERVICE.log)"
        FAILED=1
    fi
done

if [ $FAILED -eq 0 ]; then
    echo -e "\n🎉 Félicitations ! Toute la matrice CI est passée avec succès."
    exit 0
else
    echo -e "\n⚠️  Certains jobs ont échoué. Vérifiez les logs."
    exit 1
fi
