#!/usr/bin/env python3
"""Build Yarn 1.20.1 -> Mojmap 1.20.1 class, method, and field maps."""
import re
from collections import defaultdict
from pathlib import Path

root = Path("/tmp/remap")

def parse_tiny(path, ns0, ns1):
    classes = {}  # ns0 class -> ns1 class
    methods = []  # (owner0, desc0, name0, name1)
    fields = []
    current = None
    for line in path.read_text(errors="replace").splitlines():
        if line.startswith("tiny\t") or line.startswith("v1\t"):
            continue
        if line.startswith("c\t"):
            parts = line.split("\t")
            classes[parts[1]] = parts[2]
            current = parts[1]
        elif line.startswith("\tm\t") and current:
            parts = line.split("\t")
            # m, desc, name0, name1
            methods.append((current, parts[2], parts[3], parts[4] if len(parts) > 4 else parts[3]))
        elif line.startswith("\tf\t") and current:
            parts = line.split("\t")
            fields.append((current, parts[2], parts[3], parts[4] if len(parts) > 4 else parts[3]))
    return classes, methods, fields

# intermediary tiny: official(obf) -> intermediary
obf_to_inter_cls, inter_methods_raw, inter_fields_raw = parse_tiny(root / "intermediary.tiny", "obf", "inter")
# The parser stored ns0->ns1 in classes, and methods as (owner0, desc, name0, name1)
# owner0 is obfuscated class, name0 obfuscated method, name1 intermediary method

# yarn tiny: intermediary -> named
inter_to_yarn_cls, yarn_methods_raw, yarn_fields_raw = parse_tiny(root / "yarn.tiny", "inter", "yarn")

# proguard: mojmap -> obf, with mojmap signatures
moj_to_obf_cls = {}
obf_to_moj_cls = {}
# methods: (obf owner, obf name, moj desc) -> moj name   we'll rebuild desc
moj_methods = []  # (moj owner, moj name, moj desc, obf name)
moj_fields = []

current_moj = None
current_obf = None
for line in (root / "client-1.20.1.txt").read_text(errors="replace").splitlines():
    if not line or line.startswith("#"):
        continue
    if not line.startswith(" "):
        if " -> " not in line:
            continue
        left, right = line.split(" -> ")
        moj = left.strip()
        obf = right.strip().rstrip(":")
        moj_to_obf_cls[moj.replace(".", "/")] = obf
        obf_to_moj_cls[obf] = moj.replace(".", "/")
        current_moj = moj.replace(".", "/")
        current_obf = obf
    else:
        line = line.strip()
        if " -> " not in line or current_moj is None:
            continue
        left, obf_name = line.split(" -> ")
        obf_name = obf_name.strip()
        # field: type name
        # method: return name(args)
        if "(" in left:
            ret, rest = left.split(" ")[-2], left.split(" ")[-1] if False else None
            # last token before -> is name(args) or name
            sig = left.split(" ")[-1]
            name = sig[:sig.index("(")]
            args = sig[sig.index("(")+1:sig.index(")")]
            moj_desc_args = args
            moj_methods.append((current_moj, current_obf, name, obf_name, args, left))
        else:
            parts = left.split(" ")
            fname = parts[-1]
            ftype = parts[-2]
            moj_fields.append((current_moj, current_obf, fname, obf_name, ftype))

print("classes", len(obf_to_inter_cls), len(inter_to_yarn_cls), len(obf_to_moj_cls))
print("yarn methods", len(yarn_methods_raw), "moj methods", len(moj_methods))

# intermediary class (dot) -> mojmap class (dot)
inter_to_moj = {}
for obf, inter in obf_to_inter_cls.items():
    moj = obf_to_moj_cls.get(obf)
    if moj:
        inter_to_moj[inter] = moj

yarn_to_moj_cls = {}
for inter, yarn in inter_to_yarn_cls.items():
    moj = inter_to_moj.get(inter)
    if moj and yarn.startswith("net/minecraft/"):
        yarn_to_moj_cls[yarn.replace("/", ".")] = moj.replace("/", ".")

print("class map", len(yarn_to_moj_cls))
# sample
for a,b in list(yarn_to_moj_cls.items())[:8]:
    if "Minecraft" in a or "BlockPos" in a or "World" in a:
        print(a, "->", b)

Path("/tmp/remap/class_map.tsv").write_text("\n".join(f"{a}\t{b}" for a,b in sorted(yarn_to_moj_cls.items()))+"\n")
print("wrote class map")
