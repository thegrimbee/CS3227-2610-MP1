#!/usr/bin/env bash

set -u

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ACTUAL_FILE="$SCRIPT_DIR/ACTUAL.TXT"
TEST_DATA_DIR="$SCRIPT_DIR/data"

cleanup() {
    rm -f -- "$ACTUAL_FILE"
    rm -rf -- "$TEST_DATA_DIR"
}

trap cleanup EXIT
cleanup
cd "$SCRIPT_DIR" || exit 1

if ! ../gradlew -p .. clean build; then
    echo "FAIL: Gradle build failed."
    exit 1
fi

if ! java -jar ../build/libs/manhwadexlite.jar < input.txt > "$ACTUAL_FILE"; then
    echo "FAIL: CLI execution failed."
    exit 1
fi

if diff -u EXPECTED.TXT "$ACTUAL_FILE"; then
    echo "PASS"
    exit 0
fi

echo "FAIL: Output differs from EXPECTED.TXT."
exit 1
