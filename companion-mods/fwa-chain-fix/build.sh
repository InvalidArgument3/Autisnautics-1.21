#!/usr/bin/env bash
# Build the client-only FWA chain fix against Fancy World Animations
# 1.3.7-patch.2 and a Mojmap Minecraft 1.21.1 client.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
VERSION=1.0.1
OUT="$REPO/.pakku/overrides/mods/fwachainfix-${VERSION}.jar"
FWA_SHA1=e646970e0d5fc23a78c2bc155e025819f2c56379
FWA_URL="https://cdn.modrinth.com/data/IAzUFvS6/versions/rPdp8rQH/fwa+1.21.1-neoforge-1.3.7-patch.2.jar"

LIB="${LIB_DIR:-$ROOT/build/deps}"
WORK="$ROOT/build/classes-work"
NAMED="$LIB/client-1.21.1-mojmap.jar"

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
  JAVAC="$JAVA_HOME/bin/javac"
  JAVA="$JAVA_HOME/bin/java"
elif [[ -x /tmp/jdk25/bin/javac ]]; then
  JAVAC=/tmp/jdk25/bin/javac
  JAVA=/tmp/jdk25/bin/java
else
  JAVAC=javac
  JAVA=java
fi
"$JAVAC" -version >/dev/null

fetch() {
  local dest="$1" url="$2"
  if [[ ! -f "$dest" ]]; then
    mkdir -p "$(dirname "$dest")"
    curl -fsSL -o "$dest" "$url"
  fi
}

mkdir -p "$LIB"
FWA="$LIB/fwa+1.21.1-neoforge-1.3.7-patch.2.jar"
if [[ ! -f "$FWA" ]]; then
  if [[ -f /tmp/client-mods/fwa+1.21.1-neoforge-1.3.7-patch.2.jar ]]; then
    cp /tmp/client-mods/fwa+1.21.1-neoforge-1.3.7-patch.2.jar "$FWA"
  else
    fetch "$FWA" "$FWA_URL"
  fi
fi
fwa_sha="$(sha1sum "$FWA" | awk '{print $1}')"
if [[ "$fwa_sha" != "$FWA_SHA1" ]]; then
  echo "FWA hash mismatch: $fwa_sha (expected $FWA_SHA1)" >&2
  exit 1
fi

fetch "$LIB/mixin-0.8.5.jar" https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar
fetch "$LIB/asm-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar
fetch "$LIB/asm-tree-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar
fetch "$LIB/asm-commons-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar
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
  "$JAVA" -jar "$LIB/art-2.0.3-all.jar" \
    --input "$LIB/client-1.21.1.jar" \
    --output "$NAMED" \
    --names "$LIB/client-1.21.1-mappings.txt" \
    --reverse \
    --threads 4 >/dev/null
fi

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/test" "$LIB"
CP="$NAMED:$FWA:$LIB/mixin-0.8.5.jar"

find "$ROOT/src/dev" -name '*.java' > "$WORK/sources.list"
"$JAVAC" --release 21 -proc:none -cp "$CP" -d "$WORK/classes" @"$WORK/sources.list"

"$JAVAC" --release 21 -cp "$WORK/classes" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/fwachainfix/ChainHarness.java"
"$JAVA" -cp "$WORK/classes:$WORK/test" dev.wyseman.fwachainfix.ChainHarness

"$JAVAC" --release 21 -cp "$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/fwachainfix/MixinAudit.java"
"$JAVA" -cp "$WORK/test:$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" \
  dev.wyseman.fwachainfix.MixinAudit \
  "$WORK/classes" "$ROOT/src/resources/fwachainfix.mixins.json" "$FWA"

python3 - "$WORK/classes" "$ROOT/src/resources" "$OUT" <<'PY'
import json, pathlib, sys, zipfile
classes, resources, out = sys.argv[1:]
outp = pathlib.Path(out)
outp.parent.mkdir(parents=True, exist_ok=True)
if outp.exists():
    outp.unlink()
STAMP = (1980, 1, 1, 0, 0, 0)
with zipfile.ZipFile(out, "w", compression=zipfile.ZIP_DEFLATED) as jar:
    def add(path, arcname):
        info = zipfile.ZipInfo(arcname, date_time=STAMP)
        info.compress_type = zipfile.ZIP_DEFLATED
        info.external_attr = 0o644 << 16
        jar.writestr(info, path.read_bytes())
    for path in pathlib.Path(classes).rglob("*"):
        if path.is_file():
            add(path, path.relative_to(classes).as_posix())
    root = pathlib.Path(resources)
    for path in root.rglob("*"):
        if path.is_file():
            add(path, path.relative_to(root).as_posix())
    names = set(jar.namelist())
    for required in (
        "META-INF/neoforge.mods.toml",
        "fwachainfix.mixins.json",
        "dev/wyseman/fwachainfix/ChainColumn.class",
        "dev/wyseman/fwachainfix/ChainMesh.class",
        "dev/wyseman/fwachainfix/mixin/AbstractChainAnimationMixin.class",
        "dev/wyseman/fwachainfix/mixin/ChainAnimationMixin.class",
        "dev/wyseman/fwachainfix/mixin/LanternAnimationMixin.class",
    ):
        if required not in names:
            raise SystemExit(f"missing {required}")
    for banned in ("com/mojang/", "com/github/", "net/minecraft/", "net/neoforged/", "org/spongepowered/", "fr/madu59/"):
        if any(name.startswith(banned) for name in names):
            raise SystemExit(f"dependency leaked: {banned}")
    toml = jar.read("META-INF/neoforge.mods.toml").decode()
    if 'version = "1.0.1"' not in toml or 'side = "CLIENT"' not in toml:
        raise SystemExit("toml version or client side is wrong")
    mixin = json.loads(jar.read("fwachainfix.mixins.json"))
    if mixin.get("client") != ["AbstractChainAnimationMixin", "ChainAnimationMixin", "LanternAnimationMixin"] or "mixins" in mixin or "server" in mixin:
        raise SystemExit("mixin config is not client-only")
print("wrote", out, "entries", len(names))
PY

sha1sum "$OUT"
