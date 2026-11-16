#!/usr/bin/env bash
# Rebuild ipa10t from the ipa10s jar. acceptLevel still returns ClientLevel.
# VerifyJar and VerifyVoxyJar both run before the jar is installed.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
SRC_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10s.jar"
OUT_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10t.jar"
OLD_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10s.jar"
WORK="$ROOT/build"
DEPS="${VOXY_DEPS:-/tmp/voxy-mc}"
ASM="${ASM_JAR:-/tmp/libs/asm-9.7.1.jar}"
ASM_COMMONS="${ASM_COMMONS_JAR:-/tmp/libs/asm-commons-9.7.1.jar}"
ASM_TREE="${ASM_TREE_JAR:-/tmp/libs/asm-tree-9.7.1.jar}"
ASM_ANALYSIS="${ASM_ANALYSIS_JAR:-/tmp/libs/asm-analysis-9.7.1.jar}"
ASM_UTIL="${ASM_UTIL_JAR:-/tmp/libs/asm-util-9.7.1.jar}"
SLF4J_API="${SLF4J_API_JAR:-/tmp/libs/slf4j-api-2.0.16.jar}"
SLF4J_SIMPLE="${SLF4J_SIMPLE_JAR:-/tmp/libs/slf4j-simple-2.0.16.jar}"
FASTUTIL="${FASTUTIL_JAR:-/tmp/libs/fastutil-8.5.15.jar}"

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
fetch "$ASM_ANALYSIS" https://repo1.maven.org/maven2/org/ow2/asm/asm-analysis/9.7.1/asm-analysis-9.7.1.jar
fetch "$ASM_UTIL" https://repo1.maven.org/maven2/org/ow2/asm/asm-util/9.7.1/asm-util-9.7.1.jar
fetch "$SLF4J_API" https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar
fetch "$SLF4J_SIMPLE" https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.16/slf4j-simple-2.0.16.jar
fetch "$FASTUTIL" https://repo1.maven.org/maven2/it/unimi/dsi/fastutil/8.5.15/fastutil-8.5.15.jar

mkdir -p "$DEPS/libs" "$DEPS/nfrt"
fetch "$DEPS/client-1.21.1.jar" https://piston-data.mojang.com/v1/objects/30c73b1c5da787909b2f73340419fdf13b9def88/client.jar
fetch "$DEPS/client-1.21.1-mappings.txt" https://piston-data.mojang.com/v1/objects/2244b6f072256667bcd9a73df124d6c58de77992/client.txt
fetch "$DEPS/server-1.21.1-bundler.jar" https://piston-data.mojang.com/v1/objects/59353fb40c36d304f2035d51e7d6e6baa98dc05c/server.jar
fetch "$DEPS/server-1.21.1-mappings.txt" https://piston-data.mojang.com/v1/objects/03f8985492bda0afc0898465341eb0acef35f570/server.txt
fetch "$DEPS/art-2.0.3-all.jar" https://maven.neoforged.net/releases/net/neoforged/AutoRenamingTool/2.0.3/AutoRenamingTool-2.0.3-all.jar
fetch "$DEPS/neoforge-21.1.265-installer.jar" https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.265/neoforge-21.1.265-installer.jar
fetch "$DEPS/neoforge-21.1.265-universal.jar" https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.265/neoforge-21.1.265-universal.jar
fetch "$DEPS/sodium.jar" https://cdn.modrinth.com/data/AANobbMI/versions/uMOpc5uV/sodium-neoforge-0.8.13+mc1.21.1.jar
fetch "$DEPS/iris.jar" https://cdn.modrinth.com/data/YL57xq9U/versions/KduFYu4t/iris-neoforge-1.8.14-beta.1+mc1.21.1.jar
fetch "$DEPS/lwjgl.jar" https://repo1.maven.org/maven2/org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3.jar
fetch "$DEPS/commons-compress.jar" https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.26.2/commons-compress-1.26.2.jar
fetch "$DEPS/cglib.jar" https://repo1.maven.org/maven2/cglib/cglib/3.3.0/cglib-3.3.0.jar
fetch "$DEPS/resilience4j-circuitbreaker.jar" https://repo1.maven.org/maven2/io/github/resilience4j/resilience4j-circuitbreaker/2.2.0/resilience4j-circuitbreaker-2.2.0.jar
fetch "$DEPS/resilience4j-core.jar" https://repo1.maven.org/maven2/io/github/resilience4j/resilience4j-core/2.2.0/resilience4j-core-2.2.0.jar
fetch "$DEPS/resilience4j-retry.jar" https://repo1.maven.org/maven2/io/github/resilience4j/resilience4j-retry/2.2.0/resilience4j-retry-2.2.0.jar

python3 - "$DEPS" <<'PY'
import hashlib, pathlib, sys, zipfile
dest = pathlib.Path(sys.argv[1])
expected = {
    "client-1.21.1.jar": "30c73b1c5da787909b2f73340419fdf13b9def88",
    "client-1.21.1-mappings.txt": "2244b6f072256667bcd9a73df124d6c58de77992",
    "server-1.21.1-bundler.jar": "59353fb40c36d304f2035d51e7d6e6baa98dc05c",
    "server-1.21.1-mappings.txt": "03f8985492bda0afc0898465341eb0acef35f570",
}
for name, sha in expected.items():
    digest = hashlib.sha1((dest / name).read_bytes()).hexdigest()
    if digest != sha:
        raise SystemExit(f"{name} sha1 {digest} != {sha}")
bundler = zipfile.ZipFile(dest / "server-1.21.1-bundler.jar")
server = dest / "server-1.21.1.jar"
if not server.exists():
    server.write_bytes(bundler.read("META-INF/versions/1.21.1/server-1.21.1.jar"))
libs = dest / "libs"
libs.mkdir(exist_ok=True)
for name in bundler.namelist():
    if name.startswith("META-INF/libraries/") and name.endswith(".jar"):
        target = libs / name.rsplit("/", 1)[-1]
        if not target.exists():
            target.write_bytes(bundler.read(name))
installer = zipfile.ZipFile(dest / "neoforge-21.1.265-installer.jar")
import json
version = json.loads(installer.read("version.json"))
runtime = dest / "nfrt"
runtime.mkdir(exist_ok=True)
for lib in version["libraries"]:
    art = lib["downloads"]["artifact"]
    url = art.get("url") or ""
    if not url:
        continue
    target = runtime / url.rsplit("/", 1)[-1]
    if target.exists() and target.stat().st_size == art["size"]:
        continue
    import urllib.request
    urllib.request.urlretrieve(url, target)
print("minecraft deps ready")
PY

remap() {
  local input="$1" output="$2" names="$3"
  if [[ -f "$output" ]]; then
    return
  fi
  java -jar "$DEPS/art-2.0.3-all.jar" \
    --input "$input" \
    --output "$output" \
    --names "$names" \
    --reverse \
    --threads 4 >/dev/null
  zip -d "$output" 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*.DSA' >/dev/null || true
}
remap "$DEPS/client-1.21.1.jar" "$DEPS/client-1.21.1-mojmap.jar" "$DEPS/client-1.21.1-mappings.txt"
remap "$DEPS/server-1.21.1.jar" "$DEPS/server-1.21.1-mojmap.jar" "$DEPS/server-1.21.1-mappings.txt"

MC_JAR="$DEPS/client-1.21.1-mojmap.jar"
MC_SERVER="$DEPS/server-1.21.1-mojmap.jar"
NF_JAR="$DEPS/neoforge-21.1.265-universal.jar"

if [[ ! -f "$SRC_JAR" ]]; then
  echo "ipa10s jar not found at $SRC_JAR" >&2
  exit 1
fi
if [[ ! -f "$SRC_JAR" ]]; then
  echo "ipa10p1 jar not found" >&2
  exit 1
fi
if head -c 40 "$SRC_JAR" | grep -q "git-lfs"; then
  OID="$(awk '/^oid sha256:/{print substr($2, 8)}' "$SRC_JAR")"
  SRC_JAR="$REPO/.git/lfs/objects/${OID:0:2}/${OID:2:2}/$OID"
  if [[ ! -f "$SRC_JAR" ]]; then
    echo "ipa10s LFS object $OID is not in the local store" >&2
    exit 1
  fi
fi

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/patch" "$WORK/test" "$WORK/stub"

NETTY="$DEPS/libs/netty-buffer-4.1.97.Final.jar:$DEPS/libs/netty-common-4.1.97.Final.jar:$DEPS/libs/netty-transport-4.1.97.Final.jar"
AUTHLIB="$(find "$DEPS/libs" -name 'authlib-*.jar' | head -1)"
STAGE_CP="$SRC_JAR:$FASTUTIL:$MC_JAR:$MC_SERVER:$NF_JAR:$NETTY"
if [[ -n "$AUTHLIB" ]]; then STAGE_CP="$STAGE_CP:$AUTHLIB"; fi
javac --release 21 -cp "$STAGE_CP" -d "$WORK/classes" \
  "$ROOT/src/me/cortex/voxy/server/TerrainDispatch.java" \
  "$ROOT/src/me/cortex/voxy/server/RingScan.java" \
  "$ROOT/src/me/cortex/voxy/server/ServerTerrainSyncDeferredQueue.java" \
  "$ROOT/src/me/cortex/voxy/server/TerrainWorkers.java" \
  "$ROOT/src/me/cortex/voxy/server/ColumnStages.java" \
  "$ROOT/src/me/cortex/voxy/server/ColumnProfile.java" \
  "$ROOT/src/me/cortex/voxy/server/TerrainStreamPolicy.java" \
  "$ROOT/src/me/cortex/voxy/common/network/SectionWire.java" \
  "$ROOT/src/me/cortex/voxy/client/ClientSectionApply.java" \
  "$ROOT/src/me/cortex/voxy/client/ClientColumnCoverage.java" \
  "$ROOT/src/me/cortex/voxy/client/TerrainClientStats.java"
javac --release 21 -cp "$ASM:$ASM_COMMONS:$ASM_TREE:$ASM_ANALYSIS:$ASM_UTIL" -d "$WORK/patch" \
  "$ROOT/src/patch/PatchIpa10t.java" \
  "$ROOT/src/patch/VerifyJar.java" \
  "$ROOT/src/patch/VerifyVoxyJar.java"

BUILT="$WORK/voxy-ipa10t.jar"
VERIFY_CP="$MC_JAR:$MC_SERVER:$NF_JAR:$DEPS/sodium.jar:$DEPS/iris.jar:$DEPS/lwjgl.jar:$DEPS/commons-compress.jar:$DEPS/cglib.jar:$DEPS/resilience4j-circuitbreaker.jar:$DEPS/resilience4j-core.jar:$DEPS/resilience4j-retry.jar:$FASTUTIL"
VERIFY_CP="$VERIFY_CP:$(find "$DEPS/nfrt" "$DEPS/libs" -name '*.jar' | sort | paste -sd: -)"
FRAME_CP="$WORK/patch:$ASM:$ASM_COMMONS:$ASM_TREE:$ASM_ANALYSIS:$ASM_UTIL:$SRC_JAR:$VERIFY_CP"
mapfile -t NEW_CLASSES < <(find "$WORK/classes/me/cortex/voxy" -name '*.class' | sort)
java -cp "$FRAME_CP" PatchIpa10t "$SRC_JAR" "$BUILT" "${NEW_CLASSES[@]}"

javac --release 21 -d "$WORK/stub" \
  "$ROOT/src/test/net/neoforged/fml/ModList.java" \
  "$ROOT/src/stub/net/minecraft/world/level/ChunkPos.java"
javac --release 21 -cp "$BUILT:$FASTUTIL:$WORK/stub:$MC_JAR" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/server/StreamHarness.java" \
  "$ROOT/src/test/me/cortex/voxy/server/RingHarness.java"
HARNESS_CP="$BUILT:$WORK/test:$WORK/stub:$SLF4J_API:$SLF4J_SIMPLE:$FASTUTIL:$MC_JAR"
java -cp "$HARNESS_CP" me.cortex.voxy.server.StreamHarness
java -cp "$HARNESS_CP" me.cortex.voxy.server.RingHarness
javac --release 21 -cp "$BUILT" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/common/network/SectionWireHarness.java"
java -cp "$BUILT:$WORK/test" me.cortex.voxy.common.network.SectionWireHarness
javac --release 21 -cp "$BUILT" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/common/network/DeliveryHarness.java"
java -cp "$BUILT:$WORK/test" me.cortex.voxy.common.network.DeliveryHarness
APPLY_CP="$BUILT:$WORK/test:$MC_JAR:$MC_SERVER:$NF_JAR:$FASTUTIL:$NETTY"
APPLY_CP="$APPLY_CP:$(find "$DEPS/nfrt" "$DEPS/libs" -name '*.jar' | sort | paste -sd: -)"
javac --release 21 -cp "$APPLY_CP" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/client/ApplyHarness.java"
java --add-opens java.base/jdk.internal.misc=ALL-UNNAMED -Dvoxy.applySeconds=60 -cp "$APPLY_CP" me.cortex.voxy.client.ApplyHarness

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
    "me/cortex/voxy/client/ClientColumnCoverage.class",
    "me/cortex/voxy/client/ClientSectionApply.class",
    "me/cortex/voxy/server/TerrainStreamPolicy$Motion.class",
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
if "ipa10t" not in text or "ipa10s" in text or "ipa10t" not in manifest or "ipa10s" in manifest:
    raise SystemExit("version string was not bumped to ipa10t")
if "0.2.17-alpha-sodium08-portal-lods-ipa10t" not in text:
    raise SystemExit("mods.toml version is not ipa10t")
for name in (
    "me/cortex/voxy/client/TerrainReceive.class",
    "me/cortex/voxy/client/TerrainClientStats.class",
):
    if name not in new_names:
        raise SystemExit(f"missing {name}")
handler = b.read("me/cortex/voxy/common/network/TerrainSyncClientHandler.class")
client_level = b"(Ljava/lang/Object;)Lnet/minecraft/client/multiplayer/ClientLevel;"
bare_level = b"(Ljava/lang/Object;)Lnet/minecraft/world/level/Level;"
if b"SectionWire" not in handler or b"ClientSectionApply" not in handler:
    raise SystemExit("client handler lost frame expand or the off-thread apply")
receive = b.read("me/cortex/voxy/client/TerrainReceive.class")
if client_level not in receive or bare_level in receive:
    raise SystemExit("acceptLevel no longer returns ClientLevel")
if b"serverTerrainSyncPlayerBytesPerTick" not in b.read("me/cortex/voxy/common/config/VoxyConfig.class"):
    raise SystemExit("player byte budget field missing")
if b"column_coverage" not in b.read("me/cortex/voxy/common/network/ColumnCoveragePayload.class"):
    raise SystemExit("coverage payload is missing its channel id")
config = b.read("me/cortex/voxy/common/config/VoxyConfig.class")
if b"serverTerrainSyncDebug" not in config:
    raise SystemExit("debug flag was not added")
json.loads(b.read("client.voxy.mixins.json"))
json.loads(b.read("common.voxy.mixins.json"))
print("parity ok; sha1", hashlib.sha1(open(new, "rb").read()).hexdigest())
PY

VERIFY_CP="$MC_JAR:$MC_SERVER:$NF_JAR:$DEPS/sodium.jar:$DEPS/iris.jar:$DEPS/lwjgl.jar:$DEPS/commons-compress.jar:$DEPS/cglib.jar:$DEPS/resilience4j-circuitbreaker.jar:$DEPS/resilience4j-core.jar:$DEPS/resilience4j-retry.jar:$FASTUTIL"
VERIFY_CP="$VERIFY_CP:$(find "$DEPS/nfrt" "$DEPS/libs" -name '*.jar' | sort | paste -sd: -)"
java -cp "$WORK/patch:$ASM:$ASM_COMMONS:$ASM_TREE:$ASM_ANALYSIS:$ASM_UTIL" VerifyJar "$BUILT" "$VERIFY_CP"
JDK25="${JDK25:-/tmp/jdk25/bin/java}"
"$JDK25" -Xverify:all -cp "$BUILT:$WORK/patch:$VERIFY_CP" VerifyVoxyJar "$BUILT"

cp "$BUILT" "$OUT_JAR"
echo "built $OUT_JAR"
