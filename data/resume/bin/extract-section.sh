#!/bin/bash
# Usage: ./bin/extract-section.sh <docx-file> <heading>

JAR_PATH="$(dirname "$0")/../build/libs/templater-0.0.1.jar"

if [ $# -lt 2 ]; then
  echo "Usage: $0 <docx-file> <heading>"
  exit 1
fi

export LOGGING_LEVEL_COM_TEMPLATER=ERROR


java -jar "$JAR_PATH" extract-section "$1" "$2"