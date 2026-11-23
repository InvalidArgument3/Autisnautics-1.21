#!/usr/bin/env bash
# Yarn 1.20.1 sources -> Mojmap 1.20.1 with Mercury and tiny-remapper.
# Does not apply 1.20.1 -> 1.21.1 API drift. Do not commit Mojang's client jar or mapping txt.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REMAP="${REMAP_DIR:-/tmp/remap}"
LIB=/tmp/mercury
ASM_ASM="$(find /home/ubuntu/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm/9.9.1 -name 'asm-9.9.1.jar' | head -1)"
ASM_TREE="$(find /home/ubuntu/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm-tree/9.9.1 -name 'asm-tree-9.9.1.jar' | head -1)"
ASM_COMMONS="$(find /home/ubuntu/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm-commons/9.9.1 -name 'asm-commons-9.9.1.jar' | head -1)"
CP="$LIB/mercury-0.6.0.jar:$LIB/tiny-remapper-0.11.0.jar:$LIB/mapping-io-0.7.1.jar:$ASM_ASM:$ASM_TREE:$ASM_COMMONS"
mkdir -p "$HERE/build"
javac --release 17 -cp "$CP" -d "$HERE/build" "$HERE/RemapSources.java"
python3 "$HERE/prepare_tiny.py"
exec java -cp "$HERE/build:$CP" RemapSources \
  "$REMAP/yarn-to-mojmap.tiny" \
  "$REMAP/official-to-yarn.tiny" \
  "$REMAP/client-1.20.1.jar" \
  "${1:?yarn source root}" \
  "${2:?output directory}"
