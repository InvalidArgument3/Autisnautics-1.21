#!/usr/bin/env bash
# Rebuild abysscreatures 0.2.4 from the 0.2.3 jar plus the AI sources in this directory.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
OUT_JAR="$REPO/.pakku/overrides/mods/abysscreatures-0.2.4-neoforge-1.21.1.jar"
BASE_NAME="abysscreatures-0.2.3-neoforge-1.21.1.jar"
BASE_JAR="$REPO/.pakku/overrides/mods/$BASE_NAME"
WORK="$ROOT/build/pack"

mkdir -p "$ROOT/build/heading" "$ROOT/build/heading-test"
javac --release 21 -d "$ROOT/build/heading" "$ROOT/src/abysscreatures/ai/Course.java"
javac --release 21 -cp "$ROOT/build/heading" -d "$ROOT/build/heading-test" \
  "$ROOT/src/test/abysscreatures/ai/HeadingHarness.java"
java -cp "$ROOT/build/heading:$ROOT/build/heading-test" abysscreatures.ai.HeadingHarness

(cd "$ROOT" && ./gradlew --offline compileJava) || (cd "$ROOT" && ./gradlew compileJava)

mkdir -p "$ROOT/build"
if [[ -f "$BASE_JAR" ]] && ! grep -q 'git-lfs.github.com' "$BASE_JAR"; then
  BASE="$BASE_JAR"
  cp -f "$BASE_JAR" "$ROOT/build/$BASE_NAME"
elif [[ -f "$ROOT/build/$BASE_NAME" ]]; then
  BASE="$ROOT/build/$BASE_NAME"
else
  mkdir -p "$WORK"
  BASE="$WORK/$BASE_NAME"
  git -C "$REPO" cat-file --filters "HEAD:.pakku/overrides/mods/$BASE_NAME" > "$BASE" || \
    git -C "$REPO" cat-file --filters "cc244d46a96515f5f641b6023326d562e9fa531c:.pakku/overrides/mods/abysscreatures-0.2.2-neoforge-1.21.1.jar" > "$BASE"
fi
if ! unzip -t "$BASE" >/dev/null; then
  echo "0.2.3 jar is not a zip. Fetch the LFS object and re-run." >&2
  exit 1
fi

python3 - "$BASE" "$OUT_JAR" "$ROOT/build/classes/java/main" "$ROOT/src/main/resources/abysscreatures.mixins.json" <<'PY'
import pathlib, sys, zipfile
base, out, classes, mixin_json = map(pathlib.Path, sys.argv[1:])
extra = {}
for path in classes.rglob("*"):
    if path.is_file():
        extra[path.relative_to(classes).as_posix()] = path.read_bytes()
extra["abysscreatures.mixins.json"] = mixin_json.read_bytes()
replaced = {"META-INF/neoforge.mods.toml"}
with zipfile.ZipFile(base) as src, zipfile.ZipFile(out, "w") as dst:
    names = set(src.namelist())
    clash = (set(extra) & names) - replaced
    if clash:
        raise SystemExit("new class collides with the 0.2.3 jar:\n" + "\n".join(sorted(clash)))
    for info in src.infolist():
        data = src.read(info.filename)
        if info.filename == "META-INF/neoforge.mods.toml":
            text = data.decode()
            if 'version="0.2.3"' not in text:
                raise SystemExit("mods.toml is not 0.2.3")
            text = text.replace('version="0.2.3"', 'version="0.2.4"', 1)
            if "abysscreatures.mixins.json" not in text:
                text = text.rstrip() + '\n\n[[mixins]]\nconfig="abysscreatures.mixins.json"\n'
            data = text.encode()
        entry = zipfile.ZipInfo(info.filename, date_time=info.date_time)
        entry.compress_type = info.compress_type
        entry.external_attr = info.external_attr
        dst.writestr(entry, data)
    for name, data in sorted(extra.items()):
        dst.writestr(name, data)
print("packed", out, "added", len(extra))
PY

python3 - "$BASE" "$OUT_JAR" <<'PY'
import hashlib, sys, zipfile
old, new = sys.argv[1:]
new_zip = zipfile.ZipFile(new)
text = new_zip.read("META-INF/neoforge.mods.toml").decode()
if 'version="0.2.4"' not in text or "abysscreatures.mixins.json" not in text:
    raise SystemExit("toml was not bumped")
for name in (
    "abysscreatures/ai/DirectSwimNavigation.class",
    "abysscreatures/ai/HuntGoal.class",
    "abysscreatures/ai/SwimPursueGoal.class",
    "abysscreatures/ai/Bait.class",
    "abysscreatures/ai/Course.class",
    "abysscreatures/mixin/WaterAbyssCreatureMixin.class",
    "abysscreatures/mixin/CorpseAnglerMixin.class",
    "abysscreatures.mixins.json",
):
    if name not in new_zip.namelist():
        raise SystemExit("missing " + name)
for name in ("abysscreatures/ai/BoidMoveControl.class", "abysscreatures/ai/SwimMoveControl.class"):
    blob = new_zip.read(name)
    if b"yHeadRot" in blob:
        raise SystemExit(name + " writes yHeadRot again")
    if b"abysscreatures/ai/Course" not in blob:
        raise SystemExit(name + " lost the heading gate")
print("parity ok; sha1", hashlib.sha1(open(new, "rb").read()).hexdigest())
PY

if [[ -f "$BASE_JAR" && "$BASE_JAR" != "$OUT_JAR" ]]; then
  rm -f "$BASE_JAR"
fi
echo "built $OUT_JAR"
