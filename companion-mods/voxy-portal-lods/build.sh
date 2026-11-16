#!/usr/bin/env bash
# Rebuild ipa10o from the ipa10n jar. A missed disk read no longer parks the ring.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
SRC_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10n.jar"
OUT_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10o.jar"
WORK="$ROOT/build"
ASM="${ASM_JAR:-/tmp/libs/asm-9.7.1.jar}"
ASM_COMMONS="${ASM_COMMONS_JAR:-/tmp/libs/asm-commons-9.7.1.jar}"
ASM_TREE="${ASM_TREE_JAR:-/tmp/libs/asm-tree-9.7.1.jar}"
SLF4J_API="${SLF4J_API_JAR:-/tmp/libs/slf4j-api-2.0.16.jar}"
SLF4J_SIMPLE="${SLF4J_SIMPLE_JAR:-/tmp/libs/slf4j-simple-2.0.16.jar}"
FASTUTIL="${FASTUTIL_JAR:-/tmp/libs/fastutil-8.5.15.jar}"
MC_JAR="${MC_JAR:-/tmp/voxy-instance/libraries/net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar}"
NF_JAR="${NF_JAR:-/tmp/voxy-instance/libraries/net/neoforged/neoforge/21.1.265/neoforge-21.1.265-universal.jar}"

fetch() {
  local dest="$1" url="$2"
  if [[ ! -f "$dest" ]]; then
    mkdir -p "$(dirname "$dest")"
    curl -fsSL -o "$dest" "$url"
  fi
}
fetch "$ASM" https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar
fetch "$ASM_COMMONS" https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar
fetch "$ASM_TREE" https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar
fetch "$SLF4J_API" https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar
fetch "$SLF4J_SIMPLE" https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.16/slf4j-simple-2.0.16.jar
fetch "$FASTUTIL" https://repo1.maven.org/maven2/it/unimi/dsi/fastutil/8.5.15/fastutil-8.5.15.jar

if [[ ! -f "$SRC_JAR" ]]; then
  echo "ipa10n jar not found at $SRC_JAR" >&2
  exit 1
fi
if head -c 40 "$SRC_JAR" | grep -q "git-lfs"; then
  OID="$(awk '/^oid sha256:/{print substr($2, 8)}' "$SRC_JAR")"
  SRC_JAR="$REPO/.git/lfs/objects/${OID:0:2}/${OID:2:2}/$OID"
  if [[ ! -f "$SRC_JAR" ]]; then
    echo "ipa10n LFS object $OID is not in the local store" >&2
    exit 1
  fi
fi

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/patch" "$WORK/test" "$WORK/stub"

MC_SERVER="${MC_SERVER_JAR:-/tmp/autis-server/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar}"
NF_UNIVERSAL="${NF_UNIVERSAL_JAR:-/tmp/autis-server/libraries/net/neoforged/neoforge/21.1.265/neoforge-21.1.265-universal.jar}"
NETTY="${NETTY_JAR:-/tmp/autis-server/libraries/io/netty/netty-buffer/4.1.97.Final/netty-buffer-4.1.97.Final.jar:/tmp/autis-server/libraries/io/netty/netty-common/4.1.97.Final/netty-common-4.1.97.Final.jar}"
javac --release 21 -cp "$SRC_JAR:$FASTUTIL" -d "$WORK/classes" \
  "$ROOT/src/me/cortex/voxy/server/RingCursor.java" \
  "$ROOT/src/me/cortex/voxy/server/TerrainDispatch.java"
javac --release 21 -cp "$ASM:$ASM_COMMONS:$ASM_TREE" -d "$WORK/patch" \
  "$ROOT/src/patch/PatchIpa10o.java"

BUILT="$WORK/voxy-ipa10o.jar"
FRAME_CP="$WORK/patch:$ASM:$ASM_COMMONS:$ASM_TREE:$SRC_JAR"
if [[ -f "$MC_JAR" ]]; then FRAME_CP="$FRAME_CP:$MC_JAR"; fi
if [[ -f "$NF_JAR" ]]; then FRAME_CP="$FRAME_CP:$NF_JAR"; fi
mapfile -t NEW_CLASSES < <(find "$WORK/classes/me/cortex/voxy" -name '*.class' | sort)
java -cp "$FRAME_CP" PatchIpa10o "$SRC_JAR" "$BUILT" "${NEW_CLASSES[@]}"

javac --release 21 -d "$WORK/stub" \
  "$ROOT/src/test/net/neoforged/fml/ModList.java" \
  "$ROOT/src/stub/net/minecraft/world/level/ChunkPos.java"
javac --release 21 -cp "$BUILT:$FASTUTIL:$WORK/stub" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/server/StreamHarness.java" \
  "$ROOT/src/test/me/cortex/voxy/server/RingHarness.java"
HARNESS_CP="$BUILT:$WORK/test:$WORK/stub:$SLF4J_API:$SLF4J_SIMPLE:$FASTUTIL"
if [[ -f "$MC_JAR" ]]; then HARNESS_CP="$HARNESS_CP:$MC_JAR"; fi
java -cp "$HARNESS_CP" me.cortex.voxy.server.StreamHarness
java -cp "$HARNESS_CP" me.cortex.voxy.server.RingHarness

find "$ROOT/src/stub" -name '*.java' > "$WORK/stubs.list"
javac --release 21 -d "$WORK/stub" @"$WORK/stubs.list"
javac --release 21 -cp "$WORK/stub:$BUILT" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/client/core/model/TintSafetyHarness.java"
java -cp "$BUILT:$WORK/stub:$WORK/test:$SLF4J_API:$SLF4J_SIMPLE" \
  me.cortex.voxy.client.core.model.TintSafetyHarness

python3 - "$SRC_JAR" "$BUILT" <<'PY'
import hashlib, json, sys, zipfile
old, new = sys.argv[1:]
changed = {
    "me/cortex/voxy/server/RingCursor.class",
    "me/cortex/voxy/server/TerrainDispatch.class",
    "me/cortex/voxy/server/TerrainDispatch$Io.class",
    "me/cortex/voxy/server/TerrainDispatch$Counters.class",
    "META-INF/MANIFEST.MF",
    "META-INF/neoforge.mods.toml",
}
a, b = zipfile.ZipFile(old), zipfile.ZipFile(new)
old_names, new_names = set(a.namelist()), set(b.namelist())
missing = old_names - new_names
if missing:
    raise SystemExit(f"missing entries {sorted(missing)}")
unexpected = []
for name in sorted(old_names):
    if a.read(name) != b.read(name) and name not in changed:
        unexpected.append(name)
if unexpected:
    raise SystemExit("unexpected byte changes:\n" + "\n".join(unexpected))
for name in changed:
    if a.read(name) == b.read(name):
        raise SystemExit(f"expected {name} to change")
need = [
    "me/cortex/voxy/server/ColumnCoverageIndex.class",
    "me/cortex/voxy/common/network/ColumnCoveragePayload.class",
    "me/cortex/voxy/client/ClientColumnCoverage.class",
    "me/cortex/voxy/server/TerrainSentMemory.class",
]
for name in need:
    if name not in new_names:
        raise SystemExit(f"missing {name}")
text = b.read("META-INF/neoforge.mods.toml").decode()
manifest = b.read("META-INF/MANIFEST.MF").decode()
if "ipa10o" not in text or "ipa10n" in text or "ipa10o" not in manifest or "ipa10n" in manifest:
    raise SystemExit("version string was not bumped to ipa10o")
if "0.2.17-alpha-sodium08-portal-lods-ipa10o" not in text:
    raise SystemExit("mods.toml version is not ipa10o")
if b"column_coverage" not in b.read("me/cortex/voxy/common/network/ColumnCoveragePayload.class"):
    raise SystemExit("coverage payload is missing its channel id")
config = b.read("me/cortex/voxy/common/config/VoxyConfig.class")
if b"serverTerrainSyncDebug" not in config:
    raise SystemExit("debug flag was not added")
json.loads(b.read("client.voxy.mixins.json"))
json.loads(b.read("common.voxy.mixins.json"))
print("parity ok; sha1", hashlib.sha1(open(new, "rb").read()).hexdigest())
PY

cp "$BUILT" "$OUT_JAR"
echo "built $OUT_JAR"
