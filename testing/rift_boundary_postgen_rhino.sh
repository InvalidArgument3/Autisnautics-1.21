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
    '"dimension": "minecraft:overworld", "lo": -153, "hi": 23, "anchor": -52, "sentinel": -2000, "clearLo": -153, "clearHi": 23, "waterBelow": 188',
    '"dimension": "infinite_abyss:first_layer", "lo": 103, "hi": 128, "anchor": 127',
    '"dimension": "oceanworld:deepsea", "lo": 275, "hi": 320',
    '"dimension": "minecraft:the_nether", "lo": 231, "hi": 256',
    '"boundary": "0_1", "dimension": "oceanworld:deepsea", "lo": 275',
    '"boundary": "7_8", "dimension": "autisnautics:molten_depths", "lo": 295',
    '"boundary": "8_9", "dimension": "autisnautics:molten_depths", "lo": -64',
    '"boundary": "8_9", "dimension": "minecraft:the_end", "lo": 359',
    "'autisnautics:molten_depths':'minecraft:obsidian'",
]
forbidden = [
    '"hi": 640',
    '"lo": 40, "hi": 128',
    '"lo": 160, "hi": 320',
    '"lo": 112, "hi": 256',
    '"lo": 80, "hi": 384',
    '"boundary": "0_1", "dimension": "infinite_abyss:first_layer"',
    '"boundary": "2_3", "dimension": "oceanworld:deepsea"',
]
for needle in needles:
    if needle not in text:
        sys.exit("missing Rhino-safe binding:\n" + needle)
for needle in forbidden:
    if needle in text:
        sys.exit("wide pre-slab span returned:\n" + needle)
if "const previous=recent[keyOf" in text:
    sys.exit("neighbor scan still redeclares const previous")
print("binding scan ok")
PY
javac -cp "$jar" -d "$out" "$root/testing/rhino/RhinoRun.java"
java -cp "$jar:$out" RhinoRun \
  "$root/testing/rift_boundary_postgen_harness.js" \
  "$script" \
  "$root/testing/rift_boundary_postgen_drive.js"
