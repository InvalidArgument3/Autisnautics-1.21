#!/usr/bin/env bash
# Build the client-only end flash guard against VanillaBackport 1.2.2 and a
# Mojmap Minecraft 1.21.1 client. The Immersive Portals jar is an audit input.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
VERSION=1.0.0
OUT="$REPO/.pakku/client-overrides/mods/endflashguard-${VERSION}.jar"
VB_SHA1=12871e5330f8b33df7740a975047fc9a584d0387
IP="$REPO/.pakku/overrides/mods/ip_aeronautics-2.8.17.jar"
LIB="${LIB_DIR:-$ROOT/build/deps}"
WORK="$ROOT/build/classes-work"
NAMED="$LIB/client-1.21.1-mojmap.jar"
VB="$LIB/VanillaBackport-neoforge-1.21.1-1.2.2.jar"

fetch() {
  local dest="$1" url="$2"
  if [[ ! -f "$dest" ]]; then
    mkdir -p "$(dirname "$dest")"
    curl -fsSL -o "$dest" "$url"
  fi
}

if [[ ! -f "$IP" || "$(wc -c < "$IP")" -lt 1000000 ]]; then
  echo "Need the real Immersive Portals jar via git lfs, not the pointer: $IP" >&2
  exit 1
fi

fetch "$LIB/mixin-0.8.5.jar" https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar
fetch "$LIB/asm-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar
fetch "$LIB/asm-tree-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar
fetch "$LIB/asm-commons-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar
fetch "$VB" https://cdn.modrinth.com/data/6xwxDTgf/versions/mGS7BxD4/VanillaBackport-neoforge-1.21.1-1.2.2.jar
vb_sha="$(sha1sum "$VB" | awk '{print $1}')"
if [[ "$vb_sha" != "$VB_SHA1" ]]; then
  echo "VanillaBackport hash mismatch: $vb_sha" >&2
  exit 1
fi

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
CP="$NAMED:$VB:$LIB/mixin-0.8.5.jar"

find "$ROOT/src/dev" -name '*.java' > "$WORK/sources.list"
javac --release 21 -proc:none -cp "$CP" -d "$WORK/classes" @"$WORK/sources.list"

javac --release 21 -cp "$WORK/classes" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/endflashguard/FlashGateHarness.java"
java -cp "$WORK/classes:$WORK/test" dev.wyseman.endflashguard.FlashGateHarness

javac --release 21 -cp "$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/endflashguard/MixinAudit.java"
java -cp "$WORK/test:$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" \
  dev.wyseman.endflashguard.MixinAudit \
  "$WORK/classes" "$ROOT/src/resources/endflashguard.mixins.json" "$VB" "$IP"

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
        "endflashguard.mixins.json",
        "dev/wyseman/endflashguard/EndFlashPolicy.class",
        "dev/wyseman/endflashguard/EndFlashGuard.class",
        "dev/wyseman/endflashguard/mixin/EndFlashRendererMixin.class",
    ):
        if required not in names:
            raise SystemExit(f"missing {required}")
    for banned in ("com/mojang/", "com/blackgear/", "qouteall/", "net/minecraft/", "org/spongepowered/"):
        if any(name.startswith(banned) for name in names):
            raise SystemExit(f"dependency leaked: {banned}")
    toml = jar.read("META-INF/neoforge.mods.toml").decode()
    if 'version = "1.0.0"' not in toml or 'side = "CLIENT"' not in toml:
        raise SystemExit("toml version or client side is wrong")
    mixin = json.loads(jar.read("endflashguard.mixins.json"))
    if mixin.get("client") != ["EndFlashRendererMixin"] or "mixins" in mixin or "server" in mixin:
        raise SystemExit("mixin config is not client-only")
print("wrote", out, "entries", len(names))
PY

sha1sum "$OUT"
