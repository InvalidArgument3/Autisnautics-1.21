#!/bin/sh
set -eu
root=$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)
out=$root/testing/out/rhino
mkdir -p "$out"
jar=$out/rhino-2101.2.8-build.91.jar
if [ ! -f "$jar" ]; then
  curl -fsSL -o "$jar" "https://cdn.modrinth.com/data/sk9knFPE/versions/SqkDvOLG/rhino-2101.2.8-build.91.jar"
fi
javac -cp "$jar" -d "$out" "$root/testing/rhino/RhinoRun.java"
java -cp "$jar:$out" RhinoRun \
  "$root/testing/autis_item_dump_drive.js" \
  "$root/kubejs/server_scripts/_autis_item_dump.js" \
  "$root/testing/autis_item_dump_assert.js"
