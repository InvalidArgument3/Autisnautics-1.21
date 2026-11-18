#!/usr/bin/env bash
# Build the client-only dimension stack guard against Immersive Portals:
# Aeronautics 2.8.17. The IP jar is an audit input only; the mod reaches the
# mod loader by reflection, so Mixin is the whole compile classpath.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
VERSION=1.0.0
OUT="$REPO/.pakku/client-overrides/mods/dimstackguard-${VERSION}.jar"

IP="$REPO/.pakku/overrides/mods/ip_aeronautics-2.8.17.jar"
IP_SHA1=aac253b7d2f04b514c041ed1197866a16ca57828

LIB="${LIB_DIR:-$ROOT/build/deps}"
WORK="$ROOT/build/classes-work"

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

if [[ ! -f "$IP" || "$(wc -c < "$IP")" -lt 1000000 ]]; then
  echo "Need the real Immersive Portals jar via git lfs, not the pointer: $IP" >&2
  exit 1
fi
ip_sha="$(sha1sum "$IP" | awk '{print $1}')"
if [[ "$ip_sha" != "$IP_SHA1" ]]; then
  echo "Immersive Portals hash mismatch: $ip_sha (expected $IP_SHA1)" >&2
  echo "Update IP_SHA1 and re-run the audit if the mod was updated on purpose." >&2
  exit 1
fi

fetch "$LIB/mixin-0.8.5.jar" https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar
fetch "$LIB/asm-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar
fetch "$LIB/asm-tree-9.7.1.jar" https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/test"
CP="$LIB/mixin-0.8.5.jar"

find "$ROOT/src/dev" -name '*.java' > "$WORK/sources.list"
"$JAVAC" --release 21 -proc:none -cp "$CP" -d "$WORK/classes" @"$WORK/sources.list"

"$JAVAC" --release 21 -cp "$WORK/classes" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/dimstackguard/GuardHarness.java"
"$JAVA" -cp "$WORK/classes:$WORK/test" dev.wyseman.dimstackguard.GuardHarness

"$JAVAC" --release 21 -cp "$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/dimstackguard/MixinAudit.java"
"$JAVA" -cp "$WORK/test:$LIB/asm-9.7.1.jar:$LIB/asm-tree-9.7.1.jar" \
  dev.wyseman.dimstackguard.MixinAudit \
  "$WORK/classes" "$ROOT/src/resources/dimstackguard.mixins.json" "$IP"

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
        "dimstackguard.mixins.json",
        "dev/wyseman/dimstackguard/DimStackGuard.class",
        "dev/wyseman/dimstackguard/ModLookup.class",
        "dev/wyseman/dimstackguard/mixin/OOModNameMixin.class",
    ):
        if required not in names:
            raise SystemExit(f"missing {required}")
    for banned in ("com/mojang/", "net/minecraft/", "net/neoforged/", "org/spongepowered/", "qouteall/"):
        if any(name.startswith(banned) for name in names):
            raise SystemExit(f"dependency leaked: {banned}")
    toml = jar.read("META-INF/neoforge.mods.toml").decode()
    if 'version = "1.0.0"' not in toml or 'side = "CLIENT"' not in toml:
        raise SystemExit("toml version or client side is wrong")
    mixin = json.loads(jar.read("dimstackguard.mixins.json"))
    if mixin.get("client") != ["OOModNameMixin"] or "mixins" in mixin or "server" in mixin:
        raise SystemExit("mixin config is not client-only")
print("wrote", out, "entries", len(names))
PY

sha1sum "$OUT"
