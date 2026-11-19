#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"$PROJECT_ROOT/scripts/build.sh"
>/dev/null

exec java \
  -cp "$PROJECT_ROOT/out" \
  texo.Main \
  "$@"