#!/usr/bin/env bash
# Build autis-updater.jar (Java 21 bytecode) into the pack override.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SRC="$(cd "$(dirname "$0")" && pwd)"
JAVAC="${JAVAC:-javac}"
JAR="${JAR:-jar}"
rm -rf "$SRC/build"
mkdir -p "$SRC/build"
"$JAVAC" --release 21 -encoding UTF-8 -d "$SRC/build" "$SRC/Updater.java"
"$JAR" cfm "$ROOT/.pakku/client-overrides/autis/updater.jar" "$SRC/MANIFEST.MF" -C "$SRC/build" .
rm -rf "$SRC/build"
if [ -d /opt/cursor/artifacts ]; then
  cp -f "$ROOT/.pakku/client-overrides/autis/updater.jar" /opt/cursor/artifacts/autis-updater.jar
fi
echo "built $ROOT/.pakku/client-overrides/autis/updater.jar"
