#!/usr/bin/env bash
# Build the client-only neodymium particle guard against Alex's Caves 2.0.10 and
# a Mojmap Minecraft 1.21.1 client. The Alex's Caves jar is an audit input.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
VERSION=1.0.0
OUT="$REPO/.pakku/client-overrides/mods/neodymiumguard-${VERSION}.jar"

# Alex's Caves is pakku-managed, so it lives in mods/, not in the overrides.
AC="$REPO/mods/alexscaves-2.0.10.jar"
AC_SHA1=366ddaac40f14cd0102f026b4d999753fc1db2a4

LIB="${LIB_DIR:-$ROOT/build/deps}"
WORK="$ROOT/build/classes-work"
NAMED="$LIB/client-1.21.1-mojmap.jar"

# The pack's JDK is not on PATH; prefer JAVA_HOME, then the Prism temurin-25.
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
  JAVAC="$JAVA_HOME/bin/javac"
  JAVA="$JAVA_HOME/bin/java"
elif [[ -x "$HOME/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/java/temurin-25/bin/javac" ]]; then
  JAVAC="$HOME/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/java/temurin-25/bin/javac"
  JAVA="$HOME/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/java/temurin-25/bin/java"
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

if [[ ! -f "$AC" ]]; then
  echo "Need Alex's Caves at $AC" >&2
  exit 1
fi
ac_sha="$(sha1sum "$AC" | awk '{print $1}')"
if [[ "$ac_sha" != "$AC_SHA1" ]]; then
  echo "Alex's Caves hash mismatch: $ac_sha (expected $AC_SHA1)" >&2
  echo "Update AC_SHA1 and re-run the audit if the mod was updated on purpose." >&2
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
mkdir -p "$WORK/classes" "$WORK/test"
CP="$NAMED:$AC:$LIB/mixin-0.8.5.jar"

find "$ROOT/src/dev" -name '*.java' > "$WORK/sources.list"
"$JAVAC" --release 21 -proc:none -cp "$CP" -d "$WORK/classes" @"$WORK/sources.list"

"$JAVAC" --release 21 -cp "$WORK/classes:$NAMED" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/neodymiumguard/ThrottleHarness.java"
"$JAVA" -cp "$WORK/classes:$WORK/test" dev.wyseman.neodymiumguard.ThrottleHarness

"$JAVAC" --release 21 -cp "$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/neodymiumguard/MixinAudit.java"
"$JAVA" -cp "$WORK/test:$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" \
  dev.wyseman.neodymiumguard.MixinAudit \
  "$WORK/classes" "$ROOT/src/resources/neodymiumguard.mixins.json" "$AC"

python3 - "$WORK/classes" "$ROOT/src/resources" "$OUT" <<'PY'
import json, pathlib, sys, zipfile
classes, resources, out = sys.argv[1:]
outp = pathlib.Path(out)
outp.parent.mkdir(parents=True, exist_ok=True)
if outp.exists():
    outp.unlink()
# Fixed entry timestamps so a rebuild is byte-identical and the printed sha1
# can be compared against a previous build.
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
        "neodymiumguard.mixins.json",
        "dev/wyseman/neodymiumguard/NeodymiumThrottle.class",
        "dev/wyseman/neodymiumguard/NeodymiumParticleGuard.class",
        "dev/wyseman/neodymiumguard/mixin/NeodymiumNodeBlockMixin.class",
    ):
        if required not in names:
            raise SystemExit(f"missing {required}")
    for banned in ("com/mojang/", "com/github/", "net/minecraft/", "net/neoforged/", "org/spongepowered/"):
        if any(name.startswith(banned) for name in names):
            raise SystemExit(f"dependency leaked: {banned}")
    toml = jar.read("META-INF/neoforge.mods.toml").decode()
    if 'version = "1.0.0"' not in toml or 'side = "CLIENT"' not in toml:
        raise SystemExit("toml version or client side is wrong")
    mixin = json.loads(jar.read("neodymiumguard.mixins.json"))
    if mixin.get("client") != ["NeodymiumNodeBlockMixin"] or "mixins" in mixin or "server" in mixin:
        raise SystemExit("mixin config is not client-only")
print("wrote", out, "entries", len(names))
PY

sha1sum "$OUT"
