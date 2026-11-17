#!/usr/bin/env bash
# Rebuild ipa10z from the ipa10y jar. Terrain-sync registration stays gated off.
# The four render classes ipa10w/ipa10x/ipa10y rewrote are copied back from the
# ipa10h bytes. VerifyJar, VerifyVoxyJar, and LinkVerifier run before install.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
SRC_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10y.jar"
OUT_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10z.jar"
OLD_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10y.jar"
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
fetch "$DEPS/lwjgl-opengl.jar" https://repo1.maven.org/maven2/org/lwjgl/lwjgl-opengl/3.3.3/lwjgl-opengl-3.3.3.jar
fetch "$DEPS/lwjgl-glfw.jar" https://repo1.maven.org/maven2/org/lwjgl/lwjgl-glfw/3.3.3/lwjgl-glfw-3.3.3.jar
fetch "$DEPS/lwjgl-stb.jar" https://repo1.maven.org/maven2/org/lwjgl/lwjgl-stb/3.3.3/lwjgl-stb-3.3.3.jar
fetch "$DEPS/lwjgl-lmdb.jar" https://repo1.maven.org/maven2/org/lwjgl/lwjgl-lmdb/3.3.3/lwjgl-lmdb-3.3.3.jar
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
  echo "ipa10y jar not found at $SRC_JAR" >&2
  exit 1
fi
if head -c 40 "$SRC_JAR" | grep -q "git-lfs"; then
  OID="$(awk '/^oid sha256:/{print substr($2, 8)}' "$SRC_JAR")"
  SRC_JAR="$REPO/.git/lfs/objects/${OID:0:2}/${OID:2:2}/$OID"
  if [[ ! -f "$SRC_JAR" ]]; then
    echo "ipa10y LFS object $OID is not in the local store" >&2
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
  "$ROOT/src/me/cortex/voxy/server/TerrainDelivery.java" \
  "$ROOT/src/me/cortex/voxy/server/TerrainSentMemory.java" \
  "$ROOT/src/me/cortex/voxy/server/TerrainAvailability.java" \
  "$ROOT/src/me/cortex/voxy/server/RingScan.java" \
  "$ROOT/src/me/cortex/voxy/server/ServerTerrainSyncDeferredQueue.java" \
  "$ROOT/src/me/cortex/voxy/server/TerrainWorkers.java" \
  "$ROOT/src/me/cortex/voxy/server/ColumnStages.java" \
  "$ROOT/src/me/cortex/voxy/server/ColumnProfile.java" \
  "$ROOT/src/me/cortex/voxy/server/TerrainStreamPolicy.java" \
  "$ROOT/src/me/cortex/voxy/server/RingCursor.java" \
  "$ROOT/src/me/cortex/voxy/common/network/SectionWire.java" \
  "$ROOT/src/me/cortex/voxy/client/ClientSectionApply.java" \
  "$ROOT/src/me/cortex/voxy/client/ClientIngress.java" \
  "$ROOT/src/me/cortex/voxy/client/ClientColumnCoverage.java" \
  "$ROOT/src/me/cortex/voxy/client/TerrainClientStats.java" \
  "$ROOT/src/me/cortex/voxy/client/TerrainRenderStats.java" \
  "$ROOT/src/me/cortex/voxy/client/TerrainVisibility.java"
javac --release 21 -cp "$ASM:$ASM_COMMONS:$ASM_TREE:$ASM_ANALYSIS:$ASM_UTIL" -d "$WORK/patch" \
  "$ROOT/src/patch/PatchIpa10z.java" \
  "$ROOT/src/patch/VerifyJar.java" \
  "$ROOT/src/patch/VerifyVoxyJar.java" \
  "$ROOT/src/patch/LinkVerifier.java"

BUILT="$WORK/voxy-ipa10z.jar"
VERIFY_CP="$MC_JAR:$MC_SERVER:$NF_JAR:$DEPS/sodium.jar:$DEPS/iris.jar:$DEPS/lwjgl.jar:$DEPS/commons-compress.jar:$DEPS/cglib.jar:$DEPS/resilience4j-circuitbreaker.jar:$DEPS/resilience4j-core.jar:$DEPS/resilience4j-retry.jar:$FASTUTIL"
VERIFY_CP="$VERIFY_CP:$(find "$DEPS/nfrt" "$DEPS/libs" -name '*.jar' | sort | paste -sd: -)"
FRAME_CP="$WORK/patch:$ASM:$ASM_COMMONS:$ASM_TREE:$ASM_ANALYSIS:$ASM_UTIL:$SRC_JAR:$VERIFY_CP"
mapfile -t NEW_CLASSES < <(find "$WORK/classes/me/cortex/voxy" -name '*.class' | sort)
java -cp "$FRAME_CP" PatchIpa10z "$SRC_JAR" "$BUILT" "$ROOT/baseline/ipa10h"

javac --release 21 -d "$WORK/stub" \
  "$ROOT/src/test/net/neoforged/fml/ModList.java" \
  "$ROOT/src/stub/net/minecraft/world/level/ChunkPos.java"
javac --release 21 -cp "$BUILT:$FASTUTIL:$WORK/stub:$MC_JAR" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/server/StreamHarness.java" \
  "$ROOT/src/test/me/cortex/voxy/server/RingHarness.java" \
  "$ROOT/src/test/me/cortex/voxy/server/SessionHarness.java"
HARNESS_CP="$BUILT:$WORK/test:$WORK/stub:$SLF4J_API:$SLF4J_SIMPLE:$FASTUTIL:$MC_JAR"
# Streaming harnesses assert the network path. ipa10z keeps that path gated off.
echo "streaming harnesses skipped; terrain-sync registration returns immediately"
javac --release 21 -cp "$BUILT:$FASTUTIL" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/client/RenderPathHarness.java"
echo "render-path harness skipped; it drives the streaming apply hook"
javac --release 21 -cp "$BUILT" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/common/network/SectionWireHarness.java"
echo "section-wire harness skipped with the streaming protocol"
javac --release 21 -cp "$BUILT" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/common/network/DeliveryHarness.java"
echo "delivery harness skipped with the streaming queue"
APPLY_CP="$BUILT:$WORK/test:$MC_JAR:$MC_SERVER:$NF_JAR:$FASTUTIL:$NETTY"
APPLY_CP="$APPLY_CP:$(find "$DEPS/nfrt" "$DEPS/libs" -name '*.jar' | sort | paste -sd: -)"
javac --release 21 -cp "$APPLY_CP" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/client/ApplyHarness.java"
echo "apply harness skipped; handleSectionSyncBatch is gated off"

find "$ROOT/src/stub" -name '*.java' > "$WORK/stubs.list"
javac --release 21 -d "$WORK/stub" @"$WORK/stubs.list"
javac --release 21 -cp "$WORK/stub:$BUILT" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/client/core/model/TintSafetyHarness.java"
java -cp "$BUILT:$WORK/stub:$WORK/test:$SLF4J_API:$SLF4J_SIMPLE" \
  me.cortex.voxy.client.core.model.TintSafetyHarness

python3 - "$SRC_JAR" "$BUILT" <<'PY'
import hashlib, json, pathlib, sys, zipfile
old, new = sys.argv[1:]
changed = {
    "me/cortex/voxy/client/VoxyClientInstance.class",
    "me/cortex/voxy/client/core/rendering/building/RenderGenerationService.class",
    "me/cortex/voxy/client/core/rendering/hierachical/AsyncNodeManager.class",
    "me/cortex/voxy/client/core/rendering/hierachical/NodeManager.class",
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
if "ipa10z" not in text or "ipa10y" in text or "ipa10z" not in manifest or "ipa10y" in manifest:
    raise SystemExit("version string was not bumped to ipa10z")
if "0.2.17-alpha-sodium08-portal-lods-ipa10z" not in text:
    raise SystemExit("mods.toml version is not ipa10z")
baseline = pathlib.Path(old).resolve().parents[3] / "companion-mods/voxy-portal-lods/baseline/ipa10h"
for name in (
    "me/cortex/voxy/client/core/rendering/hierachical/AsyncNodeManager.class",
    "me/cortex/voxy/client/core/rendering/hierachical/NodeManager.class",
    "me/cortex/voxy/client/core/rendering/building/RenderGenerationService.class",
    "me/cortex/voxy/client/VoxyClientInstance.class",
):
    if b.read(name) != (baseline / name).read_bytes():
        raise SystemExit(f"{name} is not the ipa10h baseline")
untouched = (
    "me/cortex/voxy/client/core/rendering/SectionUpdateRouter.class",
    "me/cortex/voxy/client/core/rendering/RenderDistanceTracker.class",
    "me/cortex/voxy/client/core/VoxyRenderSystem.class",
    "me/cortex/voxy/client/core/model/ColourTintSafety.class",
)
for name in untouched:
    if a.read(name) != b.read(name):
        raise SystemExit(f"{name} changed")
for name in (
    "me/cortex/voxy/client/TerrainReceive.class",
    "me/cortex/voxy/client/TerrainClientStats.class",
):
    if name not in new_names:
        raise SystemExit(f"missing {name}")
handler = b.read("me/cortex/voxy/common/network/TerrainSyncClientHandler.class")
client_level = b"(Ljava/lang/Object;)Lnet/minecraft/client/multiplayer/ClientLevel;"
bare_level = b"(Ljava/lang/Object;)Lnet/minecraft/world/level/Level;"
if b"ClientIngress" not in handler or b"ClientSectionApply" not in handler:
    raise SystemExit("client handler lost the ingress guard or the off-thread apply")
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

mkdir -p "$WORK/nested"
python3 - "$WORK/nested" "$DEPS/sodium.jar" "$DEPS/iris.jar" "$NF_JAR" "$REPO/.pakku/overrides/mods/ip_aeronautics-2.8.17.jar" <<'PY'
import pathlib, sys, zipfile
out = pathlib.Path(sys.argv[1])
for raw in sys.argv[2:]:
    path = pathlib.Path(raw)
    if not path.is_file() or path.read_bytes()[:2] != b"PK":
        continue
    with zipfile.ZipFile(path) as zip:
        for name in zip.namelist():
            if name.startswith("META-INF/jarjar/") and name.endswith(".jar"):
                dest = out / pathlib.Path(name).name
                if not dest.exists():
                    dest.write_bytes(zip.read(name))
                    print("nested", dest.name)
PY
PACK_CP="$(python3 - "$REPO/.pakku/overrides/mods" "$REPO/.pakku/client-overrides/mods" <<'PY'
import pathlib, sys
found = []
for root in sys.argv[1:]:
    base = pathlib.Path(root)
    if not base.is_dir():
        continue
    for path in sorted(base.rglob("*.jar")):
        if "voxy-0.2.17" in path.name:
            continue
        try:
            with path.open("rb") as handle:
                if handle.read(2) == b"PK":
                    found.append(str(path))
        except OSError:
            pass
print(":".join(found))
PY
)"
VERIFY_CP="$MC_JAR:$MC_SERVER:$NF_JAR:$DEPS/sodium.jar:$DEPS/iris.jar:$DEPS/lwjgl.jar:$DEPS/lwjgl-opengl.jar:$DEPS/lwjgl-glfw.jar:$DEPS/lwjgl-stb.jar:$DEPS/lwjgl-lmdb.jar:$DEPS/commons-compress.jar:$DEPS/cglib.jar:$DEPS/resilience4j-circuitbreaker.jar:$DEPS/resilience4j-core.jar:$DEPS/resilience4j-retry.jar:$FASTUTIL"
VERIFY_CP="$VERIFY_CP:$(find "$DEPS/nfrt" "$DEPS/libs" "$WORK/nested" -name '*.jar' | sort | paste -sd: -)"
if [[ -n "$PACK_CP" ]]; then
  VERIFY_CP="$VERIFY_CP:$PACK_CP"
fi
java -cp "$WORK/patch:$ASM:$ASM_COMMONS:$ASM_TREE:$ASM_ANALYSIS:$ASM_UTIL" VerifyJar "$BUILT" "$VERIFY_CP"
JDK25="${JDK25:-/tmp/jdk25/bin/java}"
echo "LinkVerifier on ipa10y (must fail AsyncNodeManager clinit)"
set +e
"$JDK25" -Xverify:all -cp "$SRC_JAR:$WORK/patch:$VERIFY_CP" LinkVerifier --expect-verify-error "$SRC_JAR"
EXPECT_STATUS=$?
set -e
if [[ "$EXPECT_STATUS" -ne 0 ]]; then
  echo "ipa10y jar did not reproduce the VerifyError" >&2
  exit 1
fi
echo "LinkVerifier on ipa10z"
"$JDK25" -Xverify:all -cp "$BUILT:$WORK/patch:$VERIFY_CP" LinkVerifier "$BUILT"
"$JDK25" -Xverify:all -cp "$BUILT:$WORK/patch:$VERIFY_CP" VerifyVoxyJar "$BUILT"

cp "$BUILT" "$OUT_JAR"
echo "built $OUT_JAR"
