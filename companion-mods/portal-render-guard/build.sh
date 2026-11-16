#!/usr/bin/env bash
# Build the client-only portal render guard against the real mod jars and a
# Mojmap Minecraft 1.21.1 client. Stubs are not on the compile classpath.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
VERSION=1.0.1
OUT="$REPO/.pakku/client-overrides/mods/portalrenderguard-${VERSION}.jar"
OLD="$REPO/.pakku/client-overrides/mods/portalrenderguard-1.0.0.jar"
VOXY="$REPO/.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10m.jar"
IP="$REPO/.pakku/overrides/mods/ip_aeronautics-2.8.17.jar"
LIB="${LIB_DIR:-$ROOT/build/deps}"
WORK="$ROOT/build/classes-work"
NAMED="$LIB/client-1.21.1-mojmap.jar"

fetch() {
  local dest="$1" url="$2"
  if [[ ! -f "$dest" ]]; then
    mkdir -p "$(dirname "$dest")"
    curl -fsSL -o "$dest" "$url"
  fi
}

if [[ ! -f "$VOXY" || "$(wc -c < "$VOXY")" -lt 1000000 ]]; then
  echo "Need the real Voxy jar via git lfs, not the pointer: $VOXY" >&2
  exit 1
fi
if [[ ! -f "$IP" || "$(wc -c < "$IP")" -lt 1000000 ]]; then
  echo "Need the real Immersive Portals jar via git lfs, not the pointer: $IP" >&2
  exit 1
fi

fetch "$LIB/mixin-0.8.5.jar" https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar
fetch "$LIB/asm-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar
fetch "$LIB/asm-tree-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar
fetch "$LIB/lwjgl-3.3.3.jar" https://repo1.maven.org/maven2/org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3.jar
fetch "$LIB/lwjgl-opengl-3.3.3.jar" https://repo1.maven.org/maven2/org/lwjgl/lwjgl-opengl/3.3.3/lwjgl-opengl-3.3.3.jar
fetch "$LIB/joml-1.10.5.jar" https://repo1.maven.org/maven2/org/joml/joml/1.10.5/joml-1.10.5.jar
fetch "$LIB/art-2.0.3-all.jar" https://maven.neoforged.net/releases/net/neoforged/AutoRenamingTool/2.0.3/AutoRenamingTool-2.0.3-all.jar

if [[ ! -f "$NAMED" ]]; then
  fetch "$LIB/client-1.21.1.jar" https://piston-data.mojang.com/v1/objects/30c73b1c5da787909b2f73340419fdf13b9def88/client.jar
  fetch "$LIB/client-1.21.1-mappings.txt" https://piston-data.mojang.com/v1/objects/2244b6f072256667bcd9a73df124d6c58de77992/client.txt
  client_sha="$(sha1sum "$LIB/client-1.21.1.jar" | awk '{print $1}')"
  map_sha="$(sha1sum "$LIB/client-1.21.1-mappings.txt" | awk '{print $1}')"
  if [[ "$client_sha" != "30c73b1c5da787909b2f73340419fdf13b9def88" || "$map_sha" != "2244b6f072256667bcd9a73df124d6c58de77992" ]]; then
    echo "Minecraft client or mappings hash mismatch: $client_sha $map_sha" >&2
    exit 1
  fi
  java -jar "$LIB/art-2.0.3-all.jar" \
    --input "$LIB/client-1.21.1.jar" \
    --output "$NAMED" \
    --names "$LIB/client-1.21.1-mappings.txt" \
    --reverse \
    --threads 4 >/dev/null
fi

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/test"

CP="$NAMED:$VOXY:$IP:$LIB/joml-1.10.5.jar:$LIB/mixin-0.8.5.jar:$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar:$LIB/lwjgl-3.3.3.jar:$LIB/lwjgl-opengl-3.3.3.jar"

find "$ROOT/src/dev" -name '*.java' > "$WORK/sources.list"
javac --release 21 -proc:none -cp "$CP" -d "$WORK/classes" @"$WORK/sources.list"

javac --release 21 -cp "$WORK/classes" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/portalrenderguard/ClipMathHarness.java"
java -cp "$WORK/classes:$WORK/test" dev.wyseman.portalrenderguard.ClipMathHarness

javac --release 21 -cp "$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/portalrenderguard/MixinAudit.java"
java -cp "$WORK/test:$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" \
  dev.wyseman.portalrenderguard.MixinAudit \
  "$WORK/classes" "$ROOT/src/resources/portalrenderguard.mixins.json" "$VOXY" "$IP" "$NAMED"

python3 - "$WORK/classes" "$ROOT/src/resources" "$OUT" "$OLD" <<'PY'
import json, pathlib, sys, zipfile
classes, resources, out, old = sys.argv[1:]
outp = pathlib.Path(out)
outp.parent.mkdir(parents=True, exist_ok=True)
if outp.exists():
    outp.unlink()
oldp = pathlib.Path(old)
if oldp.exists():
    oldp.unlink()
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
        "dev/wyseman/portalrenderguard/mixin/PortalCompositeMixin.class",
    ):
        if required not in names:
            raise SystemExit(f"missing {required}")
    if any("VoxyClipMixin" in name for name in names):
        raise SystemExit("VoxyClipMixin must not ship")
    for banned in ("com/mojang/", "me/cortex/", "qouteall/", "org/joml/", "net/minecraft/"):
        if any(name.startswith(banned) for name in names):
            raise SystemExit(f"dependency leaked: {banned}")
    toml = jar.read("META-INF/neoforge.mods.toml").decode()
    if 'version = "1.0.1"' not in toml:
        raise SystemExit("toml version is not 1.0.1")
    json.loads(jar.read("portalrenderguard.mixins.json"))
print("wrote", out, "entries", len(names))
PY
