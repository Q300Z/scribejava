#!/bin/bash

# Dossier pour les logs
LOG_DIR="ci-logs"
mkdir -p "$LOG_DIR"

echo "🚀 Préparation : Installation locale des artefacts (JDK 17)..."
# Cette étape est cruciale pour que les modules puissent se voir entre eux dans le volume partagé
docker compose -f docker-compose.ci.yml run --rm test-jdk17 clean install -DskipTests -Dmaven.javadoc.skip=true > "$LOG_DIR/setup.log" 2>&1

echo "🚀 Lancement de la matrice CI (Lintage + Tests JDK 8, 11, 17, 21, 25) en parallèle..."
echo "📂 Les logs sont disponibles dans le dossier '$LOG_DIR'"

# Liste des services à exécuter
SERVICES=("lint" "test-jdk8" "test-jdk11" "test-jdk17" "test-jdk21" "test-jdk25")

# Lancement en parallèle et capture des logs
declare -A PIDS
for SERVICE in "${SERVICES[@]}"; do
    echo "  ➡️  Démarrage de $SERVICE..."
    docker compose -f docker-compose.ci.yml run --rm "$SERVICE" > "$LOG_DIR/$SERVICE.log" 2>&1 &
    PIDS[$SERVICE]=$!
done

echo "⏳ Attente de la fin des exécutions (cela peut prendre quelques minutes)..."

# Suivi des résultats
FAILED=0
for SERVICE in "${SERVICES[@]}"; do
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
