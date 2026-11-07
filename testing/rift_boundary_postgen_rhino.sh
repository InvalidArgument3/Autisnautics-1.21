#!/bin/sh
set -eu
root=$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)
script=$root/kubejs/server_scripts/rift_boundary_postgen.js
out=$root/testing/out/rhino
mkdir -p "$out"
jar=$out/rhino-2101.2.8-build.91.jar
if [ ! -f "$jar" ]; then
  curl -fsSL -o "$jar" "https://cdn.modrinth.com/data/sk9knFPE/versions/SqkDvOLG/rhino-2101.2.8-build.91.jar"
fi
python3 - "$script" << 'PY'
import sys
text = open(sys.argv[1], encoding="utf-8").read()
needles = [
    "while (recentOrder.length>4096) {let expired=",
    "let previous=recent[keyOf(dim,x,z)]",
    "for (let f=0;f<task.faces.length;f++) {\n            let s=task.faces[f]",
    "let task=queue[head++]",
    "let before=Date.now(),result=sweep(task,deadline),spent=Date.now()-before",
]
for needle in needles:
    if needle not in text:
        sys.exit("missing Rhino-safe binding:\n" + needle)
if "const previous=recent[keyOf" in text:
    sys.exit("neighbor scan still redeclares const previous")
print("binding scan ok")
PY
javac -cp "$jar" -d "$out" "$root/testing/rhino/RhinoRun.java"
java -cp "$jar:$out" RhinoRun \
  "$root/testing/rift_boundary_postgen_harness.js" \
  "$script" \
  "$root/testing/rift_boundary_postgen_drive.js"
