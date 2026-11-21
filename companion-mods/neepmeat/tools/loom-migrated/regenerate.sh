#!/usr/bin/env bash
# Rebuild the Mojmap 1.20.1 tree from upstream. Output is gitignored.
set -euo pipefail

PIN=0d36776e4e78fd4049cbe9f556a5204d5e98c4ff
ROOT="$(cd "$(dirname "$0")" && pwd)"
WORK="${NEP_LOOM_WORK:-/tmp/neep-loom-regen}"

rm -rf "$WORK"
git clone --filter=blob:none https://codeberg.org/MeatWheeze/NeepMeat.git "$WORK"
git -C "$WORK" checkout "$PIN"
git -C "$WORK" submodule update --init subprojects/meatlib

python3 - "$WORK/build.gradle" << 'PY'
import pathlib, sys
path = pathlib.Path(sys.argv[1])
text = path.read_text()
text = text.replace("id 'fabric-loom' version '1.9-SNAPSHOT'", "id 'fabric-loom' version '1.9.2'")
path.write_text(text)
PY

(cd "$WORK" && ./gradlew migrateMappings --mappings "net.minecraft:mappings:1.20.1")

rm -rf "$ROOT/neepmeat" "$ROOT/meatlib"
mkdir -p "$ROOT/neepmeat" "$ROOT/meatlib"
cp -a "$WORK/remappedSrc/." "$ROOT/neepmeat/"
cp -a "$WORK/subprojects/meatlib/remappedSrc/." "$ROOT/meatlib/"
echo "wrote $(find "$ROOT/neepmeat" "$ROOT/meatlib" -name '*.java' | wc -l) files under $ROOT"
