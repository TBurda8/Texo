#!/usr/bin/env bash

set -euo pipefaul

PROJECT_ROOT="$(cd "$(dirname"${BASH_SOURCE[0]}")/.." && pwd)"

"$PROJECT_ROOT/scripts/build.sh"

exec java \
  -cp "$PROJECT_ROOT/out" \
  texo.Main \
  "$@"