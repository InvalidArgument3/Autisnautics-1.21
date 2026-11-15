#!/usr/bin/env bash
# Build the client-only portal render guard. Stubs are compile-only.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
OUT="$REPO/.pakku/client-overrides/mods/portalrenderguard-1.0.0.jar"
LIB="${LIB_DIR:-/tmp/libs}"
WORK="$ROOT/build"

fetch() {
  local dest="$1" url="$2"
  if [[ ! -f "$dest" ]]; then
    mkdir -p "$(dirname "$dest")"
    curl -fsSL -o "$dest" "$url"
  fi
}

fetch "$LIB/mixin-0.8.5.jar" https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar
fetch "$LIB/asm-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar
fetch "$LIB/asm-tree-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar
fetch "$LIB/lwjgl-3.3.3.jar" https://repo1.maven.org/maven2/org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3.jar
fetch "$LIB/lwjgl-opengl-3.3.3.jar" https://repo1.maven.org/maven2/org/lwjgl/lwjgl-opengl/3.3.3/lwjgl-opengl-3.3.3.jar

rm -rf "$WORK"
mkdir -p "$WORK/stub" "$WORK/classes" "$WORK/test"

find "$ROOT/stub" -name '*.java' > "$WORK/stubs.list"
javac --release 21 -d "$WORK/stub" @"$WORK/stubs.list"

find "$ROOT/src/dev" -name '*.java' > "$WORK/sources.list"
javac --release 21 -proc:none \
  -cp "$WORK/stub:$LIB/mixin-0.8.5.jar:$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar:$LIB/lwjgl-3.3.3.jar:$LIB/lwjgl-opengl-3.3.3.jar" \
  -d "$WORK/classes" @"$WORK/sources.list"

javac --release 21 -cp "$WORK/classes" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/portalrenderguard/ClipMathHarness.java"
java -cp "$WORK/classes:$WORK/test" dev.wyseman.portalrenderguard.ClipMathHarness

python3 - "$WORK/classes" "$ROOT/src/resources" "$OUT" <<'PY'
import json, pathlib, sys, zipfile
classes, resources, out = sys.argv[1:]
outp = pathlib.Path(out)
outp.parent.mkdir(parents=True, exist_ok=True)
if outp.exists():
    outp.unlink()
with zipfile.ZipFile(out, "w", compression=zipfile.ZIP_DEFLATED) as jar:
    for path in pathlib.Path(classes).rglob("*"):
        if path.is_file():
            jar.write(path, path.relative_to(classes).as_posix())
    root = pathlib.Path(resources)
    for path in root.rglob("*"):
        if path.is_file():
            jar.write(path, path.relative_to(root).as_posix())
    names = set(jar.namelist())
    for required in (
        "META-INF/neoforge.mods.toml",
        "portalrenderguard.mixins.json",
        "dev/wyseman/portalrenderguard/GlStateGuard.class",
        "dev/wyseman/portalrenderguard/ClipPlane.class",
        "dev/wyseman/portalrenderguard/mixin/VoxyRenderSystemMixin.class",
        "dev/wyseman/portalrenderguard/mixin/VoxyClipMixin.class",
        "dev/wyseman/portalrenderguard/mixin/PortalCompositeMixin.class",
    ):
        if required not in names:
            raise SystemExit(f"missing {required}")
    for banned in ("com/mojang/", "me/cortex/", "qouteall/", "org/joml/"):
        if any(name.startswith(banned) for name in names):
            raise SystemExit(f"stub leaked: {banned}")
    json.loads(jar.read("portalrenderguard.mixins.json"))
print("wrote", out, "entries", len(names))
PY
