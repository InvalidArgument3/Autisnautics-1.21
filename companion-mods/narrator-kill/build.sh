#!/usr/bin/env bash
# Fetch and prepare the compile inputs for the narrator-kill companion.
# Downloads the official 1.21.1 client + mappings, remaps them to Mojmap so we
# can target Narrator call sites by name, and caches everything in build/deps.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
LIB="$ROOT/build/deps"
mkdir -p "$LIB"

# WARNING: native tools (curl, java) need cygpath -w paths. MSYS path conversion
# is disabled here, so a bare /c/... path is passed through unconverted and the
# write fails with curl error 23.
fetch() {
  local dest="$1" url="$2" destw
  destw="$(cygpath -w "$dest")"
  if [[ ! -f "$dest" ]]; then
    mkdir -p "$(dirname "$dest")"
    echo "fetch $(basename "$dest")"
    curl -fsSL -o "$destw" "$url"
  fi
}

CLIENT_SHA1=30c73b1c5da787909b2f73340419fdf13b9def88
MAP_SHA1=2244b6f072256667bcd9a73df124d6c58de77992

fetch "$LIB/mixin-0.8.5.jar" https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar
fetch "$LIB/art-2.0.3-all.jar" https://maven.neoforged.net/releases/net/neoforged/AutoRenamingTool/2.0.3/AutoRenamingTool-2.0.3-all.jar
fetch "$LIB/client-1.21.1.jar" https://piston-data.mojang.com/v1/objects/${CLIENT_SHA1}/client.jar
fetch "$LIB/client-1.21.1-mappings.txt" https://piston-data.mojang.com/v1/objects/${MAP_SHA1}/client.txt

[[ "$(sha1sum "$LIB/client-1.21.1.jar" | awk '{print $1}')" == "$CLIENT_SHA1" ]] || { echo "client hash mismatch" >&2; exit 1; }
[[ "$(sha1sum "$LIB/client-1.21.1-mappings.txt" | awk '{print $1}')" == "$MAP_SHA1" ]] || { echo "mappings hash mismatch" >&2; exit 1; }

fetch "$LIB/text2speech-1.17.9.jar" https://libraries.minecraft.net/com/mojang/text2speech/1.17.9/text2speech-1.17.9.jar
fetch "$LIB/slf4j-api-2.0.9.jar" https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar

if [[ ! -f "$LIB/client-1.21.1-mojmap.jar" ]]; then
  "C:/Program Files/Zulu/zulu-21/bin/java" -jar "$(cygpath -w "$LIB/art-2.0.3-all.jar")" \
    --input "$(cygpath -w "$LIB/client-1.21.1.jar")" \
    --output "$(cygpath -w "$LIB/client-1.21.1-mojmap.jar")" \
    --names "$(cygpath -w "$LIB/client-1.21.1-mappings.txt")" \
    --reverse --threads 4 >/dev/null
fi

# Every string literal in the mixin is checked against the real 1.21.1 class here,
# because javac does not look inside @Inject method names.
NAMED="$LIB/client-1.21.1-mojmap.jar"
JAVAP="C:/Program Files/Zulu/zulu-21/bin/javap"
TARGETS="$("$JAVAP" -p -classpath "$(cygpath -w "$NAMED")" net.minecraft.client.GameNarrator)"
for needle in \
  "boolean isActive()" \
  "void sayChat(net.minecraft.network.chat.Component)" \
  "void say(net.minecraft.network.chat.Component)" \
  "void sayNow(net.minecraft.network.chat.Component)" \
  "void sayNow(java.lang.String)" \
  "void updateNarratorStatus(net.minecraft.client.NarratorStatus)"; do
  grep -qF "$needle" <<<"$TARGETS" || { echo "missing narrator target: $needle" >&2; exit 1; }
done
if ! "$JAVAP" -c -p -classpath "$(cygpath -w "$NAMED")" net.minecraft.client.GameNarrator \
  | grep -qF 'com/mojang/text2speech/Narrator.getNarrator:()Lcom/mojang/text2speech/Narrator;'; then
  echo "GameNarrator no longer builds its engine through Narrator.getNarrator()" >&2
  exit 1
fi

WORK="$ROOT/build/classes"
rm -rf "$WORK"
mkdir -p "$WORK"
CP="$(cygpath -w "$NAMED");$(cygpath -w "$LIB/mixin-0.8.5.jar");$(cygpath -w "$LIB/text2speech-1.17.9.jar");$(cygpath -w "$LIB/slf4j-api-2.0.9.jar")"
find "$ROOT/src/dev" -name '*.java' > "$ROOT/build/sources.raw"
: > "$ROOT/build/sources.list"
while IFS= read -r f; do cygpath -w "$f" >> "$ROOT/build/sources.list"; done < "$ROOT/build/sources.raw"
"C:/Program Files/Zulu/zulu-21/bin/javac" --release 21 -proc:none -cp "$CP" -d "$(cygpath -w "$WORK")" @"$(cygpath -w "$ROOT/build/sources.list")"

OUT="$ROOT/../../.pakku/client-overrides/mods/narratorkill-1.0.0.jar"
python3 - "$(cygpath -w "$WORK")" "$(cygpath -w "$ROOT/src/resources")" "$(cygpath -w "$OUT")" <<'PY'
import json, pathlib, sys, zipfile
classes, resources, out = sys.argv[1:4]
outp = pathlib.Path(out)
outp.parent.mkdir(parents=True, exist_ok=True)
if outp.exists():
    outp.unlink()
with zipfile.ZipFile(out, "w", compression=zipfile.ZIP_DEFLATED) as jar:
    def add(path, base):
        arc = str(path.relative_to(base)).replace("\\", "/")
        jar.writestr(arc, path.read_bytes())
    for path in pathlib.Path(classes).rglob("*"):
        if path.is_file():
            add(path, pathlib.Path(classes))
    root = pathlib.Path(resources)
    for path in root.rglob("*"):
        if path.is_file():
            add(path, root)
    names = set(jar.namelist())
    for required in (
        "META-INF/neoforge.mods.toml",
        "narratorkill.mixins.json",
        "dev/wyseman/narratorkill/mixin/GameNarratorMixin.class",
    ):
        if required not in names:
            raise SystemExit(f"missing {required}")
    for banned in ("com/mojang/", "net/minecraft/", "org/spongepowered/"):
        if any(name.startswith(banned) for name in names):
            raise SystemExit(f"dependency leaked: {banned}")
    toml = jar.read("META-INF/neoforge.mods.toml").decode()
    if 'version = "1.0.0"' not in toml or 'side = "CLIENT"' not in toml:
        raise SystemExit("toml version or client side is wrong")
    mixin = json.loads(jar.read("narratorkill.mixins.json"))
    if mixin.get("client") != ["GameNarratorMixin"] or "mixins" in mixin or "server" in mixin:
        raise SystemExit("mixin config is not client-only")
print("wrote", out, "entries", len(names))
PY

sha1sum "$OUT"
