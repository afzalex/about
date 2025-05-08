#!/bin/bash
# Usage: ./bin/create-resumes.sh [loglevel]

JAR_PATH="$(dirname "$0")/../build/libs/templater-0.0.1.jar"
LOG_LEVEL="${1:-error}"

# Hard-coded file paths
BASE_TEMPLATE="./docs/base.template.docx"
OUTPUT_DIR="./docs"
CONCRETE_TEMPLATES=(
  "./docs/sde.template.docx"
)

mkdir -p "$OUTPUT_DIR"

for CONCRETE_TEMPLATE in "${CONCRETE_TEMPLATES[@]}"; do
  BASENAME=$(basename "$CONCRETE_TEMPLATE" .template.docx)
  OUTPUT_FILE="$OUTPUT_DIR/resume-${BASENAME}.docx"
  echo "Merging $BASE_TEMPLATE with $CONCRETE_TEMPLATE -> $OUTPUT_FILE"
  java -jar "$JAR_PATH" merge "$BASE_TEMPLATE" "$CONCRETE_TEMPLATE" "$OUTPUT_FILE"
  if [ $? -ne 0 ]; then
    echo "Failed to merge $CONCRETE_TEMPLATE"
  fi
  echo
  # Optionally, add a delay or more logging here
  # Remove echo if you want less output
done 