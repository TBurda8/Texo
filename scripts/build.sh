#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_DIRECTORY="$PROJECT_ROOT/out"

mkdir -p "$OUTPUT_DIRECTORY"

javac \
    -d "$OUTPUT_DIRECTORY" \
    "$PROJECT_ROOT"/src/texo/*.java

echo "Texo compiled successfully."