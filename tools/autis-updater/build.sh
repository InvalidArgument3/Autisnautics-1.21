#!/usr/bin/env bash
# Build autis-updater.jar (Java 21 bytecode) into the pack override.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SRC="$(cd "$(dirname "$0")" && pwd)"
JAVAC="${JAVAC:-javac}"
JAR_BIN="${JAR_BIN:-jar}"
rm -rf "$SRC/build"
mkdir -p "$SRC/build"
"$JAVAC" --release 21 -encoding UTF-8 -d "$SRC/build" "$SRC/Updater.java"
# The class constant, the manifest, and this resource are the embedded version.
ver="$(sed -n 's/.*UPDATER_VERSION = \([0-9][0-9]*\).*/\1/p' "$SRC/Updater.java")"
man="$(sed -n 's/^Updater-Version: *//p' "$SRC/MANIFEST.MF" | tr -d '\r')"
if [ -z "$ver" ] || [ "$ver" != "$man" ]; then
  echo "Updater-Version mismatch: source=$ver manifest=$man" >&2
  exit 1
fi
printf '%s\n' "$ver" > "$SRC/build/updater-version.txt"
DEST="$ROOT/.pakku/client-overrides/autis/updater.jar"
rm -f "$DEST"
"$JAR_BIN" cfm "$DEST" "$SRC/MANIFEST.MF" -C "$SRC/build" .
rm -rf "$SRC/build"
mkdir -p "$ROOT/.pakku/server-overrides/autis"
cp -f "$DEST" "$ROOT/.pakku/server-overrides/autis/updater.jar"
manifest="$(unzip -p "$DEST" META-INF/MANIFEST.MF)"
printf '%s\n' "$manifest" | grep -q 'Premain-Class:'
printf '%s\n' "$manifest" | grep -q "Updater-Version: $ver"
if [ -d /opt/cursor/artifacts ]; then
  cp -f "$DEST" /opt/cursor/artifacts/autis-updater.jar
fi
SANITY="$(mktemp -d)"
mkdir -p "$SANITY/mods"
java -jar "$DEST" --check --game-dir "$SANITY" >"$SANITY/out.txt"
grep -q 'no .autis/version.txt, skipped' "$SANITY/out.txt"
grep -q 'skipped-no-version' "$SANITY/autis/updater.log"
if [ -f "$SANITY/.autis/version.txt" ]; then
  echo "skip must not invent .autis/version.txt" >&2
  exit 1
fi
rm -rf "$SANITY"
echo "built $DEST (Updater-Version $ver)"
