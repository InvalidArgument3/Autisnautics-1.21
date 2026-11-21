#!/usr/bin/env bash
# Autisnautics updater bootstrap (Linux/macOS).
#
# Drops autis-updater.jar into a Prism/MultiMC instance and writes the pre-launch
# command into instance.cfg. One-time step for an instance imported before the
# updater existed. The launcher must be closed - it rewrites instance.cfg on exit.
#
# CurseForge has no pre-launch command. Its profile JVM arguments get:
#   -javaagent:autis/updater.jar
# The agent reads --gameDir from the game command. Do not point CurseForge's
# Java path at a wrapper; it rewrites minecraftinstance.json and drops that.
#
#   ./autis-bootstrap.sh "/home/user/.local/share/PrismLauncher/instances/Autisnautics 1.21"
set -euo pipefail

REPO="InvalidArgument3/Autisnautics-1.21"
INSTANCE="${1:-}"

if pgrep -x prismlauncher >/dev/null 2>&1 || pgrep -x PrismLauncher >/dev/null 2>&1 || pgrep -x multimc >/dev/null 2>&1; then
    echo "Prism/MultiMC is running. Close it first - it rewrites instance.cfg on exit." >&2
    exit 1
fi

if [ -z "$INSTANCE" ]; then
    shopt -s nullglob
    candidates=()
    for root in "$HOME/.local/share/PrismLauncher/instances" "$HOME/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances"; do
        for d in "$root"/*Autisnautics*; do
            [ -f "$d/instance.cfg" ] && candidates+=("$d")
        done
    done
    if [ "${#candidates[@]}" -eq 1 ]; then
        INSTANCE="${candidates[0]}"
    else
        printf 'Pass the instance path. Candidates:\n%s\n' "$(printf '  %s\n' "${candidates[@]:-none}")" >&2
        exit 1
    fi
fi

CFG="$INSTANCE/instance.cfg"
MC="$INSTANCE/minecraft"
[ -f "$CFG" ] || { echo "No instance.cfg in $INSTANCE" >&2; exit 1; }
[ -d "$MC" ] || { echo "No minecraft folder in $INSTANCE" >&2; exit 1; }

mkdir -p "$MC/autis"
URL="https://github.com/$REPO/releases/latest/download/autis-updater.jar"
echo "Downloading $URL"
curl -fsSL "$URL" -o "$MC/autis/updater.jar"

ENCODED='PreLaunchCommand="\"$INST_JAVA\" -jar \"$INST_MC_DIR/autis/updater.jar\" --game-dir \"$INST_MC_DIR\""'

python3 - "$CFG" "$ENCODED" <<'PY'
import sys, pathlib
cfg, encoded = pathlib.Path(sys.argv[1]), sys.argv[2]
text = cfg.read_bytes().decode("utf-8")
nl = "\r\n" if "\r\n" in text else "\n"
lines = text.split(nl)
kept = [l for l in lines if not l.startswith("OverrideCommands=") and not l.startswith("PreLaunchCommand=")]
out, inserted = [], False
for line in kept:
    out.append(line)
    if not inserted and line == "InstanceType=OneSix":
        out += ["OverrideCommands=true", encoded]
        inserted = True
if not inserted:
    idx = out.index("[General]") if "[General]" in out else 0
    out[idx + 1:idx + 1] = ["OverrideCommands=true", encoded]
cfg.write_bytes(nl.join(out).encode("utf-8"))
print(f"patched {cfg}")
PY

echo "Installed: $MC/autis/updater.jar"
echo "Start the launcher and press Play - the pack syncs itself before Minecraft starts."
