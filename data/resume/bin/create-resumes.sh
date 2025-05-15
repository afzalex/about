#!/bin/bash
# Usage: ./bin/create-resumes.sh [loglevel]

JAR_PATH="$(dirname "$0")/../build/libs/templater-0.0.1.jar"
LOG_LEVEL="${1:-error}"

# Hard-coded file paths
BASE_TEMPLATE="./docs/base.template.docx"
OUTPUT_DIR="./docs"

# Get all template files except base template
TEMPLATE_FILES=(./docs/*.template.docx)
CONCRETE_TEMPLATES=()
for template in "${TEMPLATE_FILES[@]}"; do
    if [[ "$template" != "$BASE_TEMPLATE" ]]; then
        CONCRETE_TEMPLATES+=("$template")
    fi
done

# Check if JAR exists
if [[ ! -f "$JAR_PATH" ]]; then
  echo "❌ Error: JAR not found at $JAR_PATH"
  exit 1
fi

# Check if any concrete templates exist
if [ ${#CONCRETE_TEMPLATES[@]} -eq 0 ]; then
    echo "❌ No template files found in ./docs/ (excluding base template)"
    exit 1
fi

# Print found templates
echo "Found templates:"
for template in "${CONCRETE_TEMPLATES[@]}"; do
    echo "  - $template"
done

mkdir -p "$OUTPUT_DIR"

for CONCRETE_TEMPLATE in "${CONCRETE_TEMPLATES[@]}"; do
  BASENAME=$(basename "$CONCRETE_TEMPLATE" .template.docx)
  OUTPUT_FILE="$OUTPUT_DIR/resume-${BASENAME}.docx"
  echo "Merging $BASE_TEMPLATE with $CONCRETE_TEMPLATE -> $OUTPUT_FILE"
  java -jar "$JAR_PATH" merge "$BASE_TEMPLATE" "$CONCRETE_TEMPLATE" "$OUTPUT_FILE"
  if [ $? -ne 0 ]; then
    echo "Failed to merge $CONCRETE_TEMPLATE"
    exit 1
  fi
  echo "✅ Successfully created $OUTPUT_FILE"
done 