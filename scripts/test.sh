#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

JUNIT_VERSION="6.0.3"
LIB_DIRECTORY="$PROJECT_ROOT/lib"
JUNIT_JAR="$LIB_DIRECTORY/junit-platform-console-standalone-$JUNIT_VERSION.jar"
TEST_OUTPUT="$PROJECT_ROOT/out/tests"

mkdir -p "$LIB_DIRECTORY"
mkdir -p "$TEST_OUTPUT"

if [[ ! -f "$JUNIT_JAR" ]]; then
    echo "Downloading JUnit $JUNIT_VERSION..."

    curl -fsSL \
        "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/$JUNIT_VERSION/junit-platform-console-standalone-$JUNIT_VERSION.jar" \
        -o "$JUNIT_JAR"
fi

"$PROJECT_ROOT/scripts/build.sh"

javac \
    -cp "$PROJECT_ROOT/out:$JUNIT_JAR" \
    -d "$TEST_OUTPUT" \
    "$PROJECT_ROOT"/tests/texo/*.java

java -jar "$JUNIT_JAR" execute \
    --class-path "$PROJECT_ROOT/out:$TEST_OUTPUT" \
    --scan-class-path \
    --fail-if-no-tests