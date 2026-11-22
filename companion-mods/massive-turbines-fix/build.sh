#!/usr/bin/env bash
# Build the dedicated-server fix for Create: Massive Turbines 1.0.1.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
VERSION=1.0.0
OUT="$REPO/.pakku/overrides/mods/massiveturbinesfix-${VERSION}.jar"
TURBINES_SHA1=1838d1e1338e5bc30afcb282baa0b5819b43c41f
TURBINES_URL="https://edge.forgecdn.net/files/9738/559/create_massive_turbines-1.0.1-NeoForge-1.21.1.jar"

LIB="${LIB_DIR:-$ROOT/build/deps}"
WORK="$ROOT/build/classes-work"
NAMED="$LIB/client-1.21.1-mojmap.jar"
SHARED="$ROOT/../fwa-chain-fix/build/deps"

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

reuse() {
  local name="$1"
  if [[ ! -f "$LIB/$name" && -f "$SHARED/$name" ]]; then
    mkdir -p "$LIB"
    cp "$SHARED/$name" "$LIB/$name"
  fi
}

mkdir -p "$LIB"
reuse mixin-0.8.5.jar
reuse asm-9.7.1.jar
reuse asm-tree-9.7.1.jar
reuse asm-commons-9.7.1.jar
reuse art-2.0.3-all.jar
reuse client-1.21.1.jar
reuse client-1.21.1-mappings.txt
reuse client-1.21.1-mojmap.jar

fetch "$LIB/mixin-0.8.5.jar" https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar
fetch "$LIB/asm-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar
fetch "$LIB/asm-tree-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar
fetch "$LIB/asm-commons-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar
fetch "$LIB/art-2.0.3-all.jar" https://maven.neoforged.net/releases/net/neoforged/AutoRenamingTool/2.0.3/AutoRenamingTool-2.0.3-all.jar

TURBINES="$LIB/create_massive_turbines-1.0.1-NeoForge-1.21.1.jar"
if [[ ! -f "$TURBINES" ]]; then
  if [[ -f /tmp/cmt/turbines.jar ]]; then
    cp /tmp/cmt/turbines.jar "$TURBINES"
  else
    fetch "$TURBINES" "$TURBINES_URL"
  fi
fi
turbines_sha="$(sha1sum "$TURBINES" | awk '{print $1}')"
if [[ "$turbines_sha" != "$TURBINES_SHA1" ]]; then
  echo "Massive Turbines hash mismatch: $turbines_sha (expected $TURBINES_SHA1)" >&2
  exit 1
fi

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

PONDER="$LIB/ponder-neoforge-1.0.82+mc1.21.1.jar"
if [[ ! -f "$PONDER" ]]; then
  python3 - "$TURBINES" "$PONDER" <<'PY'
import pathlib, sys, zipfile
src, dest = sys.argv[1:]
# Catnip's PlacementOffset ships inside Create's nested Ponder jar.
# The turbines jar on the compile path is not the one we compile against for that type;
# Create 6.0.10 in the pack is the matching copy. Fall back to the turbines download's sibling.
create = pathlib.Path("/workspace/mods/create-1.21.1-6.0.10.jar")
if not create.is_file():
    create = pathlib.Path("/tmp/autis-server/mods/create-1.21.1-6.0.10.jar")
with zipfile.ZipFile(create) as jar:
    pathlib.Path(dest).write_bytes(jar.read("META-INF/jarjar/ponder-neoforge-1.0.82+mc1.21.1.jar"))
PY
fi

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/test"
CP="$NAMED:$PONDER:$LIB/mixin-0.8.5.jar"

find "$ROOT/src/dev" -name '*.java' > "$WORK/sources.list"
"$JAVAC" --release 21 -proc:none -cp "$CP" -d "$WORK/classes" @"$WORK/sources.list"

"$JAVAC" --release 21 -cp "$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/massiveturbinesfix/MixinAudit.java"
"$JAVA" -cp "$WORK/test:$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" \
  dev.wyseman.massiveturbinesfix.MixinAudit \
  "$WORK/classes" "$ROOT/src/resources/massiveturbinesfix.mixins.json" "$TURBINES"

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
        "massiveturbinesfix.mixins.json",
        "dev/wyseman/massiveturbinesfix/mixin/CasingHelperMixin.class",
    ):
        if required not in names:
            raise SystemExit(f"missing {required}")
    for banned in ("com/mojang/", "com/github/", "net/minecraft/", "net/neoforged/", "org/spongepowered/", "net/createmod/"):
        if any(name.startswith(banned) for name in names):
            raise SystemExit(f"dependency leaked: {banned}")
    toml = jar.read("META-INF/neoforge.mods.toml").decode()
    if 'version = "1.0.0"' not in toml or 'side = "BOTH"' not in toml:
        raise SystemExit("toml version or side is wrong")
    if 'create_massive_turbines' not in toml or '[1.0.1,1.0.2)' not in toml:
        raise SystemExit("toml is not pinned to Massive Turbines 1.0.1")
    mixin = json.loads(jar.read("massiveturbinesfix.mixins.json"))
    if mixin.get("server") != ["CasingHelperMixin"] or "client" in mixin or "mixins" in mixin:
        raise SystemExit("mixin config is not dedicated-server only")
print("wrote", out, "entries", len(names))
PY

sha1sum "$OUT"
