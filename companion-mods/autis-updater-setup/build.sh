#!/usr/bin/env bash
# Build the client-only updater setup mod. client-overrides is not copied
# into the server pack; overrides/mods is.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
VERSION=1.0.2
OUT="$REPO/.pakku/client-overrides/mods/autis_updater_setup-${VERSION}.jar"
rm -f "$REPO/.pakku/client-overrides/mods/autis_updater_setup-1.0.0.jar"
rm -f "$REPO/.pakku/client-overrides/mods/autis_updater_setup-1.0.1.jar"

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

LIB="$ROOT/build/deps"
WORK="$ROOT/build/classes-work"
mkdir -p "$LIB" "$WORK/classes" "$WORK/stub" "$WORK/test"

# A compile-only stand-in for net.neoforged.fml.common.Mod. The runtime class
# is NeoForge's own; this stub is not packed into the jar.
mkdir -p "$WORK/stub-src/net/neoforged/fml/common"
cat > "$WORK/stub-src/net/neoforged/fml/common/Mod.java" <<'EOF'
package net.neoforged.fml.common;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    String value();
}
EOF
"$JAVAC" --release 21 -encoding UTF-8 -proc:none -d "$WORK/stub" "$WORK/stub-src/net/neoforged/fml/common/Mod.java"

find "$ROOT/src/dev" -name '*.java' ! -name 'SetupMod.java' > "$WORK/sources.list"
"$JAVAC" --release 21 -encoding UTF-8 -proc:none -d "$WORK/classes" @"$WORK/sources.list"
"$JAVAC" --release 21 -encoding UTF-8 -proc:none -cp "$WORK/classes" -d "$WORK/test" \
  "$ROOT/src/test/dev/wyseman/autisupdatersetup/SetupHarness.java" \
  "$ROOT/src/test/dev/wyseman/autisupdatersetup/ExitRewriteHarness.java"
"$JAVA" -cp "$WORK/classes:$WORK/test" dev.wyseman.autisupdatersetup.SetupHarness

"$JAVAC" --release 21 -encoding UTF-8 -proc:none -cp "$WORK/classes:$WORK/stub" -d "$WORK/classes" \
  "$ROOT/src/dev/wyseman/autisupdatersetup/SetupMod.java"

python3 - "$WORK/classes" "$ROOT/src/resources" "$OUT" "$VERSION" <<'PY'
import pathlib, sys, zipfile
classes, resources, out, version = sys.argv[1:]
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
        "dev/wyseman/autisupdatersetup/Setup.class",
        "dev/wyseman/autisupdatersetup/SetupMod.class",
        "dev/wyseman/autisupdatersetup/PrismPatch.class",
        "dev/wyseman/autisupdatersetup/CursePatch.class",
        "dev/wyseman/autisupdatersetup/CurseUpdate.class",
        "dev/wyseman/autisupdatersetup/CurseNotice.class",
        "dev/wyseman/autisupdatersetup/ModrinthPatch.class",
    ):
        if required not in names:
            raise SystemExit(f"missing {required}")
    if any(name.startswith("dev/wyseman/autisupdatersetup/SetupHarness") for name in names):
        raise SystemExit("test class leaked into the jar")
    for banned in ("net/neoforged/", "net/minecraft/", "com/mojang/"):
        if any(name.startswith(banned) for name in names):
            raise SystemExit(f"dependency leaked: {banned}")
    toml = jar.read("META-INF/neoforge.mods.toml").decode()
    if f'version = "{version}"' not in toml or 'side = "CLIENT"' not in toml:
        raise SystemExit("toml version or client side is wrong")
    if toml.count('side = "CLIENT"') < 2:
        raise SystemExit("minecraft and neoforge must both be client-only")
    if 'displayTest = "IGNORE_SERVER_VERSION"' not in toml:
        raise SystemExit("displayTest must not require the mod on the server")
print("wrote", out, "entries", len(names))
PY

# The packaged jar runs the same launch path the game will.
SANITY="$(mktemp -d)"
mkdir -p "$SANITY/instance/minecraft"
printf '%s\n' '[General]' 'InstanceType=OneSix' 'name=Sanity' > "$SANITY/instance/instance.cfg"
"$JAVA" -cp "$OUT" dev.wyseman.autisupdatersetup.Setup apply "$SANITY/instance/minecraft" --no-helper
grep -q 'autis/updater.jar' "$SANITY/instance/instance.cfg"
grep -q 'patched prism' "$SANITY/instance/minecraft/autis/updater-setup.log"
grep -q '\[autis_updater_setup\] patched prism' "$SANITY/instance/minecraft/logs/latest.log"
rm -rf "$SANITY"

# A second process restores the pre-launch instance.cfg the moment the game JVM
# exits, which is what Prism does from the copy it held in memory.
"$JAVA" -cp "$WORK/test:$OUT" dev.wyseman.autisupdatersetup.ExitRewriteHarness "$OUT"

sha1sum "$OUT"
