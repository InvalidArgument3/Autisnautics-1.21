#!/usr/bin/env bash
# Rebuild the ipa10j Voxy jar from the ipa10i binary and the tint/sampler patch.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
SRC_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10i.jar"
OUT_JAR="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10j.jar"
WORK="$ROOT/build"
ASM="${ASM_JAR:-/tmp/libs/asm-9.7.1.jar}"
SLF4J_API="${SLF4J_API_JAR:-/tmp/libs/slf4j-api-2.0.16.jar}"
SLF4J_SIMPLE="${SLF4J_SIMPLE_JAR:-/tmp/libs/slf4j-simple-2.0.16.jar}"

if [[ ! -f "$ASM" ]]; then
  mkdir -p "$(dirname "$ASM")"
  curl -fsSL -o "$ASM" https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar
fi

rm -rf "$WORK"
mkdir -p "$WORK/stubs" "$WORK/classes" "$WORK/patch" "$WORK/test"

find "$ROOT/src/stub" -name '*.java' > "$WORK/stubs.list"
javac --release 21 -d "$WORK/stubs" @"$WORK/stubs.list"

javac --release 21 -cp "$WORK/stubs" -d "$WORK/classes" \
  "$ROOT/src/me/cortex/voxy/client/core/model/ColourTintSafety.java"
javac --release 21 -cp "$ASM" -d "$WORK/patch" "$ROOT/src/patch/PatchVoxy.java"

java -cp "$WORK/patch:$ASM" PatchVoxy "$SRC_JAR" \
  "$WORK/classes/me/cortex/voxy/client/core/model/ColourTintSafety.class" \
  "$OUT_JAR"

javac --release 21 -cp "$WORK/stubs:$OUT_JAR" -d "$WORK/test" \
  "$ROOT/src/test/me/cortex/voxy/client/core/model/TintSafetyHarness.java"
java -cp "$OUT_JAR:$WORK/stubs:$WORK/test:$SLF4J_API:$SLF4J_SIMPLE" \
  me.cortex.voxy.client.core.model.TintSafetyHarness

python3 - "$SRC_JAR" "$OUT_JAR" <<'PY'
import hashlib, sys, zipfile
old, new = sys.argv[1:]
changed = {
    "me/cortex/voxy/client/core/model/ModelFactory.class",
    "me/cortex/voxy/client/core/model/ModelFactory$1.class",
    "me/cortex/voxy/client/core/model/ModelFactory$2.class",
    "me/cortex/voxy/client/TimingStatistics.class",
    "me/cortex/voxy/client/TimingStatistics$TimeSampler.class",
    "me/cortex/voxy/client/core/VoxyRenderSystem.class",
    "META-INF/MANIFEST.MF",
    "META-INF/neoforge.mods.toml",
}
added = {"me/cortex/voxy/client/core/model/ColourTintSafety.class"}
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
if "ipa10j" not in text or "ipa10i" in text or "ipa10j" not in manifest or "ipa10i" in manifest:
    raise SystemExit("version string was not bumped to ipa10j")
import json
json.loads(b.read("client.voxy.mixins.json"))
json.loads(b.read("common.voxy.mixins.json"))
digest = hashlib.sha1(open(new, "rb").read()).hexdigest()
print(f"parity ok; sha1 {digest}")
PY

echo "built $OUT_JAR"
