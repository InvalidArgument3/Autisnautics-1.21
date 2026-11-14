#!/usr/bin/env bash
# Rebuild ipa10k from the ipa10j jar by patching server LOD streaming.
# ipa10j itself is the tint/sampler patch of ipa10i; that jar is the parent commit.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
SRC_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10j.jar"
OUT_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10k.jar"
WORK="$ROOT/build"
ASM="${ASM_JAR:-/tmp/libs/asm-9.7.1.jar}"
ASM_COMMONS="${ASM_COMMONS_JAR:-/tmp/libs/asm-commons-9.7.1.jar}"
ASM_TREE="${ASM_TREE_JAR:-/tmp/libs/asm-tree-9.7.1.jar}"
SLF4J_API="${SLF4J_API_JAR:-/tmp/libs/slf4j-api-2.0.16.jar}"
SLF4J_SIMPLE="${SLF4J_SIMPLE_JAR:-/tmp/libs/slf4j-simple-2.0.16.jar}"
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

if [[ ! -f "$SRC_JAR" ]]; then
  echo "ipa10j jar not found at $SRC_JAR (restore it from git history before this commit)" >&2
  exit 1
fi

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/patch" "$WORK/test" "$WORK/stub"

javac --release 21 -cp "$SRC_JAR" -d "$WORK/classes" \
  "$ROOT/src/me/cortex/voxy/server/TerrainStreamPolicy.java"
javac --release 21 -cp "$ASM:$ASM_COMMONS:$ASM_TREE" -d "$WORK/patch" \
  "$ROOT/src/patch/PatchStreaming.java"

FRAME_CP="$WORK/patch:$ASM:$ASM_COMMONS:$ASM_TREE:$SRC_JAR"
if [[ -f "$MC_JAR" ]]; then FRAME_CP="$FRAME_CP:$MC_JAR"; fi
if [[ -f "$NF_JAR" ]]; then FRAME_CP="$FRAME_CP:$NF_JAR"; fi
mapfile -t POLICY_CLASSES < <(find "$WORK/classes/me/cortex/voxy/server" -name '*.class' | sort)
java -cp "$FRAME_CP" PatchStreaming "$SRC_JAR" "$OUT_JAR" "${POLICY_CLASSES[@]}"

javac --release 21 -cp "$OUT_JAR" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/server/StreamHarness.java"
javac --release 21 -d "$WORK/stub" "$ROOT/src/test/net/neoforged/fml/ModList.java"
HARNESS_CP="$OUT_JAR:$WORK/test:$WORK/stub:$SLF4J_API:$SLF4J_SIMPLE"
if [[ -f "$MC_JAR" ]]; then HARNESS_CP="$HARNESS_CP:$MC_JAR"; fi
java -cp "$HARNESS_CP" me.cortex.voxy.server.StreamHarness

# The tint harness still runs against the patched jar. Its stubs are unchanged.
find "$ROOT/src/stub" -name '*.java' > "$WORK/stubs.list"
javac --release 21 -d "$WORK/stub" @"$WORK/stubs.list"
javac --release 21 -cp "$WORK/stub:$OUT_JAR" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/client/core/model/TintSafetyHarness.java"
java -cp "$OUT_JAR:$WORK/stub:$WORK/test:$SLF4J_API:$SLF4J_SIMPLE" \
  me.cortex.voxy.client.core.model.TintSafetyHarness

python3 - "$SRC_JAR" "$OUT_JAR" <<'PY'
import hashlib, json, sys, zipfile
old, new = sys.argv[1:]
changed = {
    "me/cortex/voxy/common/config/VoxyConfig.class",
    "me/cortex/voxy/server/VoxyServerChunkSync.class",
    "me/cortex/voxy/server/ServerTerrainTransport.class",
    "me/cortex/voxy/server/ServerTerrainSyncBudget.class",
    "me/cortex/voxy/server/VoxyServerGeneratedChunkWalker.class",
    "me/cortex/voxy/common/network/ReliableTerrainQueue.class",
    "META-INF/MANIFEST.MF",
    "META-INF/neoforge.mods.toml",
}
added = {
    "me/cortex/voxy/server/TerrainStreamPolicy.class",
    "me/cortex/voxy/server/TerrainStreamPolicy$Motion.class",
}
a, b = zipfile.ZipFile(old), zipfile.ZipFile(new)
old_names, new_names = set(a.namelist()), set(b.namelist())
missing = old_names - new_names
extra = new_names - old_names - added
if missing or extra:
    raise SystemExit(f"entry mismatch missing={sorted(missing)} extra={sorted(extra)}")
unexpected = []
for name in sorted(old_names):
    if a.read(name) != b.read(name) and name not in changed:
        unexpected.append(name)
if unexpected:
    raise SystemExit("unexpected byte changes:\n" + "\n".join(unexpected))
for name in changed:
    if a.read(name) == b.read(name):
        raise SystemExit(f"expected {name} to change")
text = b.read("META-INF/neoforge.mods.toml").decode()
manifest = b.read("META-INF/MANIFEST.MF").decode()
if "ipa10k" not in text or "ipa10j" in text or "ipa10k" not in manifest or "ipa10j" in manifest:
    raise SystemExit("version string was not bumped to ipa10k")
json.loads(b.read("client.voxy.mixins.json"))
json.loads(b.read("common.voxy.mixins.json"))
print("parity ok; sha1", hashlib.sha1(open(new, "rb").read()).hexdigest())
PY

echo "built $OUT_JAR"
