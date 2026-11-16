#!/usr/bin/env bash
# Rebuild ipa10m from the ipa10l jar. Already-held columns are skipped, and the ring overlaps the view-distance square.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
SRC_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10l.jar"
OUT_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10m.jar"
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
  echo "ipa10l jar not found at $SRC_JAR" >&2
  exit 1
fi
if head -c 40 "$SRC_JAR" | grep -q "git-lfs"; then
  OID="$(awk '/^oid sha256:/{print substr($2, 8)}' "$SRC_JAR")"
  SRC_JAR="$REPO/.git/lfs/objects/${OID:0:2}/${OID:2:2}/$OID"
  if [[ ! -f "$SRC_JAR" ]]; then
    echo "ipa10l LFS object $OID is not in the local store" >&2
    exit 1
  fi
fi

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/patch" "$WORK/test" "$WORK/stub"

javac --release 21 -cp "$SRC_JAR:$FASTUTIL" -d "$WORK/classes" \
  "$ROOT/src/me/cortex/voxy/server/RingCursor.java" \
  "$ROOT/src/me/cortex/voxy/server/RingScan.java" \
  "$ROOT/src/me/cortex/voxy/server/TerrainAvailability.java" \
  "$ROOT/src/me/cortex/voxy/server/TerrainSentMemory.java" \
  "$ROOT/src/me/cortex/voxy/server/TerrainStreamPolicy.java" \
  "$ROOT/src/me/cortex/voxy/server/TerrainDispatch.java"
javac --release 21 -cp "$ASM:$ASM_COMMONS:$ASM_TREE" -d "$WORK/patch" \
  "$ROOT/src/patch/PatchIpa10m.java"

BUILT="$WORK/voxy-ipa10m.jar"
FRAME_CP="$WORK/patch:$ASM:$ASM_COMMONS:$ASM_TREE:$SRC_JAR"
if [[ -f "$MC_JAR" ]]; then FRAME_CP="$FRAME_CP:$MC_JAR"; fi
if [[ -f "$NF_JAR" ]]; then FRAME_CP="$FRAME_CP:$NF_JAR"; fi
mapfile -t NEW_CLASSES < <(find "$WORK/classes/me/cortex/voxy/server" -name '*.class' | sort)
java -cp "$FRAME_CP" PatchIpa10m "$SRC_JAR" "$BUILT" "${NEW_CLASSES[@]}"

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
    "me/cortex/voxy/server/VoxyServerChunkSync.class",
    "me/cortex/voxy/server/VoxyServerGeneratedChunkWalker.class",
    "me/cortex/voxy/server/ServerTerrainTransport.class",
    "me/cortex/voxy/server/RingCursor.class",
    "me/cortex/voxy/server/RingScan.class",
    "me/cortex/voxy/server/RingScan$Probe.class",
    "me/cortex/voxy/server/TerrainAvailability.class",
    "me/cortex/voxy/server/TerrainDispatch.class",
    "me/cortex/voxy/server/TerrainDispatch$Io.class",
    "me/cortex/voxy/server/TerrainStreamPolicy.class",
    "me/cortex/voxy/server/TerrainStreamPolicy$Motion.class",
    "me/cortex/voxy/common/config/VoxyConfig.class",
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
    "me/cortex/voxy/server/RingCursor.class",
    "me/cortex/voxy/server/RingScan.class",
    "me/cortex/voxy/server/TerrainAvailability.class",
    "me/cortex/voxy/server/TerrainSentMemory.class",
    "me/cortex/voxy/server/PendingOrder.class",
    "me/cortex/voxy/server/TerrainDispatch.class",
]
for name in need:
    if name not in new_names:
        raise SystemExit(f"missing {name}")
text = b.read("META-INF/neoforge.mods.toml").decode()
manifest = b.read("META-INF/MANIFEST.MF").decode()
if "ipa10m" not in text or "ipa10l" in text or "ipa10m" not in manifest or "ipa10l" in manifest:
    raise SystemExit("version string was not bumped to ipa10m")
if "0.2.17-alpha-sodium08-portal-lods-ipa10m" not in text:
    raise SystemExit("mods.toml version is not ipa10m")
config = b.read("me/cortex/voxy/common/config/VoxyConfig.class")
if b"serverTerrainSyncDebug" not in config:
    raise SystemExit("debug flag was not added")
json.loads(b.read("client.voxy.mixins.json"))
json.loads(b.read("common.voxy.mixins.json"))
print("parity ok; sha1", hashlib.sha1(open(new, "rb").read()).hexdigest())
PY

cp "$BUILT" "$OUT_JAR"
echo "built $OUT_JAR"
