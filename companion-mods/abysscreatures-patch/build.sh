#!/usr/bin/env bash
# Rebuild abysscreatures 0.2.3 from the 0.2.2 jar.
# The 0.2.2 jar is the parent of this change. After it is replaced, the script
# reads that blob back out of git (cc244d46) through the LFS smudge filter.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
OUT_JAR="$REPO/.pakku/overrides/mods/abysscreatures-0.2.3-neoforge-1.21.1.jar"
OLD_NAME="abysscreatures-0.2.2-neoforge-1.21.1.jar"
OLD_JAR="$REPO/.pakku/overrides/mods/$OLD_NAME"
SRC_REV="${SRC_REV:-cc244d46a96515f5f641b6023326d562e9fa531c}"
ASM="${ASM_JAR:-/tmp/libs/asm-9.7.1.jar}"
ASM_TREE="${ASM_TREE_JAR:-/tmp/libs/asm-tree-9.7.1.jar}"
WORK="$ROOT/build"

fetch() {
  local dest="$1" url="$2"
  if [[ ! -f "$dest" ]]; then
    mkdir -p "$(dirname "$dest")"
    curl -fsSL -o "$dest" "$url"
  fi
}
fetch "$ASM" https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar
fetch "$ASM_TREE" https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/patch" "$WORK/test" "$WORK/src"

javac --release 21 -d "$WORK/classes" "$ROOT/src/abysscreatures/ai/Course.java"
javac --release 21 -cp "$WORK/classes" -d "$WORK/test" \
  "$ROOT/src/test/abysscreatures/ai/HeadingHarness.java"
java -cp "$WORK/classes:$WORK/test" abysscreatures.ai.HeadingHarness

if [[ -f "$OLD_JAR" ]] && ! grep -q 'git-lfs.github.com' "$OLD_JAR"; then
  cp "$OLD_JAR" "$WORK/src/$OLD_NAME"
else
  git -C "$REPO" cat-file --filters "$SRC_REV:.pakku/overrides/mods/$OLD_NAME" > "$WORK/src/$OLD_NAME"
fi
if ! unzip -t "$WORK/src/$OLD_NAME" >/dev/null; then
  echo "0.2.2 jar is not a zip (LFS pointer?). Fetch the LFS object and re-run." >&2
  exit 1
fi

javac --release 21 -cp "$ASM:$ASM_TREE" -d "$WORK/patch" "$ROOT/src/patch/PatchHeading.java"
java -cp "$WORK/patch:$ASM:$ASM_TREE" PatchHeading \
  "$WORK/src/$OLD_NAME" "$OUT_JAR" "$WORK/classes/abysscreatures/ai/Course.class"

python3 - "$WORK/src/$OLD_NAME" "$OUT_JAR" <<'PY'
import hashlib, sys, zipfile
old, new = sys.argv[1:]
changed = {
    "abysscreatures/ai/BoidMoveControl.class",
    "abysscreatures/ai/SwimMoveControl.class",
    "abysscreatures/ai/Boid.class",
    "META-INF/neoforge.mods.toml",
}
added = {"abysscreatures/ai/Course.class"}
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
for name in changed | added:
    if name in old_names and a.read(name) == b.read(name):
        raise SystemExit(f"expected {name} to change")
    if name not in new_names:
        raise SystemExit(f"missing {name}")
text = b.read("META-INF/neoforge.mods.toml").decode()
if 'version="0.2.3"' not in text or 'version="0.2.2"' in text:
    raise SystemExit("version string was not bumped to 0.2.3")
for name in (
    "abysscreatures/ai/BoidMoveControl.class",
    "abysscreatures/ai/SwimMoveControl.class",
    "abysscreatures/ai/Boid.class",
):
    blob = b.read(name)
    if b"abysscreatures/ai/Course" not in blob:
        raise SystemExit(f"{name} does not reference Course")
if b"separationGain" not in b.read("abysscreatures/ai/Boid.class"):
    raise SystemExit("Boid no longer calls separationGain")
# The head is the look control's. Neither move control may stamp yHeadRot.
for name in ("abysscreatures/ai/BoidMoveControl.class", "abysscreatures/ai/SwimMoveControl.class"):
    blob = b.read(name)
    if b"yHeadRot" in blob:
        raise SystemExit(f"{name} still mentions yHeadRot")
print("parity ok; sha1", hashlib.sha1(open(new, "rb").read()).hexdigest())
PY

if [[ -f "$OLD_JAR" ]]; then
  rm -f "$OLD_JAR"
fi

echo "built $OUT_JAR"
