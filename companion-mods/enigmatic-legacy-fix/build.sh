#!/usr/bin/env bash
# Build the common-setup fix for EnigmaticLegacy+ 1.21.1-1.1.2.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
VERSION=1.0.0
OUT="$REPO/.pakku/overrides/mods/enigmaticlegacyfix-${VERSION}.jar"
EL_SHA1=717add3fabab571b88d97eadcfabafe6a27ad1e8
EL_URL="https://edge.forgecdn.net/files/9141/458/enigmaticlegacyplus-1.21.1-1.1.2.jar"

LIB="${LIB_DIR:-$ROOT/build/deps}"
WORK="$ROOT/build/classes-work"
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
fetch "$LIB/mixin-0.8.5.jar" https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar
fetch "$LIB/asm-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar
fetch "$LIB/asm-tree-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar
fetch "$LIB/loader-4.0.45.jar" https://maven.neoforged.net/releases/net/neoforged/fancymodloader/loader/4.0.45/loader-4.0.45.jar
fetch "$LIB/bus-8.0.5.jar" https://maven.neoforged.net/releases/net/neoforged/bus/8.0.5/bus-8.0.5.jar

EL="$LIB/enigmaticlegacyplus-1.21.1-1.1.2.jar"
if [[ ! -f "$EL" ]]; then
  if [[ -f /workspace/mods/enigmaticlegacyplus-1.21.1-1.1.2.jar ]]; then
    cp /workspace/mods/enigmaticlegacyplus-1.21.1-1.1.2.jar "$EL"
  else
    fetch "$EL" "$EL_URL"
  fi
fi
el_sha="$(sha1sum "$EL" | awk '{print $1}')"
if [[ "$el_sha" != "$EL_SHA1" ]]; then
  echo "EnigmaticLegacy+ hash mismatch: $el_sha (expected $EL_SHA1)" >&2
  exit 1
fi

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/test"
CP="$EL:$LIB/loader-4.0.45.jar:$LIB/bus-8.0.5.jar:$LIB/mixin-0.8.5.jar"

find "$ROOT/src/dev" -name '*.java' > "$WORK/sources.list"
"$JAVAC" --release 21 -proc:none -cp "$CP" -d "$WORK/classes" @"$WORK/sources.list"

"$JAVAC" --release 21 -cp "$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/enigmaticlegacyfix/MixinAudit.java"
"$JAVA" -cp "$WORK/test:$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" \
  dev.wyseman.enigmaticlegacyfix.MixinAudit \
  "$WORK/classes" "$ROOT/src/resources/enigmaticlegacyfix.mixins.json" "$EL"

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
        "enigmaticlegacyfix.mixins.json",
        "dev/wyseman/enigmaticlegacyfix/mixin/EnigmaticLegacyCommonSetupMixin.class",
    ):
        if required not in names:
            raise SystemExit(f"missing {required}")
    for banned in ("com/mojang/", "net/minecraft/", "net/neoforged/", "org/spongepowered/", "auviotre/"):
        if any(name.startswith(banned) for name in names):
            raise SystemExit(f"dependency leaked: {banned}")
    toml = jar.read("META-INF/neoforge.mods.toml").decode()
    if 'version = "1.0.0"' not in toml or 'side = "BOTH"' not in toml:
        raise SystemExit("toml version or side is wrong")
    if 'enigmaticlegacyplus' not in toml or '[1.21.1-1.1.2,1.21.1-1.1.3)' not in toml:
        raise SystemExit("toml is not pinned to EnigmaticLegacy+ 1.21.1-1.1.2")
    mixin = json.loads(jar.read("enigmaticlegacyfix.mixins.json"))
    if mixin.get("mixins") != ["EnigmaticLegacyCommonSetupMixin"] or "client" in mixin or "server" in mixin:
        raise SystemExit("mixin config is not both-sides")
print("wrote", out)
PY

sha1sum "$OUT"
