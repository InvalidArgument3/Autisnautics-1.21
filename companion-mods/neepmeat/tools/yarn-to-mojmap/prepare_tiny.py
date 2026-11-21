#!/usr/bin/env python3
"""Build owner-aware tiny v2 files: official->yarn and yarn->mojmap.

Member descriptors stay in the first namespace, which is what tiny-remapper expects.
"""
from pathlib import Path

root = Path("/tmp/remap")


def parse_classes_and_members(path):
    classes = []
    methods = []
    fields = []
    current = None
    for line in path.read_text(errors="replace").splitlines():
        if line.startswith("c\t"):
            parts = line.split("\t")
            current = parts[1]
            classes.append((parts[1], parts[2]))
        elif line.startswith("\tm\t") and current:
            parts = line.split("\t")
            methods.append((current, parts[2], parts[3], parts[4]))
        elif line.startswith("\tf\t") and current:
            parts = line.split("\t")
            fields.append((current, parts[2], parts[3], parts[4]))
    return classes, methods, fields


def translate_desc(desc, class_map):
    out = []
    i = 0
    while i < len(desc):
        char = desc[i]
        if char in "BCDFIJSZV([)":
            out.append(char)
            i += 1
        elif char == "L":
            end = desc.index(";", i)
            internal = desc[i + 1:end]
            out.append("L" + class_map.get(internal, internal) + ";")
            i = end + 1
        else:
            out.append(char)
            i += 1
    return "".join(out)


def obf_desc_to_moj_types(desc, obf_to_moj):
    if desc.startswith("("):
        desc = desc[1:desc.index(")")]
    types = []
    i = 0
    while i < len(desc):
        dim = ""
        while desc[i] == "[":
            dim += "[]"
            i += 1
        if desc[i] == "L":
            end = desc.index(";", i)
            internal = desc[i + 1:end]
            i = end + 1
            moj = obf_to_moj.get(internal, internal).replace("/", ".")
            types.append(moj + dim)
        else:
            prim = {"B": "byte", "C": "char", "D": "double", "F": "float", "I": "int",
                    "J": "long", "S": "short", "Z": "boolean", "V": "void"}[desc[i]]
            i += 1
            types.append(prim + dim)
    return tuple(types)


inter_classes, inter_methods, inter_fields = parse_classes_and_members(root / "intermediary.tiny")
yarn_classes, yarn_methods, yarn_fields = parse_classes_and_members(root / "yarn.tiny")

obf_to_inter = dict(inter_classes)
inter_to_yarn = dict(yarn_classes)
obf_to_moj = {}
moj_methods = {}
current_moj = current_obf = None
for line in (root / "client-1.20.1.txt").read_text(errors="replace").splitlines():
    if not line or line.startswith("#"):
        continue
    if not line.startswith(" "):
        if " -> " not in line:
            continue
        left, right = line.split(" -> ")
        current_moj = left.strip().replace(".", "/")
        current_obf = right.strip().rstrip(":")
        obf_to_moj[current_obf] = current_moj
    else:
        line = line.strip()
        if " -> " not in line or not current_obf or "(" not in line:
            continue
        left, obf_name = line.split(" -> ")
        sig = left.split(" ")[-1]
        name = sig[:sig.index("(")]
        args = sig[sig.index("(") + 1:sig.index(")")]
        arglist = tuple(part.strip() for part in args.split(",")) if args else ()
        moj_methods.setdefault((current_obf, obf_name.strip()), []).append((arglist, name))

inter_to_moj = {}
for obf, inter in obf_to_inter.items():
    moj = obf_to_moj.get(obf)
    if moj:
        inter_to_moj[inter] = moj

by_id_inter = {mid: (owner, desc, obf_name) for owner, desc, obf_name, mid in inter_methods}
by_id_yarn = {mid: (owner, yarn_name) for owner, desc, mid, yarn_name in yarn_methods}

def moj_method_name(method_id, obf_owner, obf_desc, obf_name):
    types = obf_desc_to_moj_types(obf_desc, obf_to_moj)
    candidates = moj_methods.get((obf_owner, obf_name), [])
    for argt, name in candidates:
        if argt == types:
            return name
    if len(candidates) == 1:
        return candidates[0][1]
    return None

yarn_lines = ["tiny\t2\t0\tyarn\tmojmap"]
official_lines = ["tiny\t2\t0\tofficial\tyarn"]
class_count = method_count = 0
for inter, yarn in yarn_classes:
    moj = inter_to_moj.get(inter)
    obf = None
    # reverse obf_to_inter
    if moj:
        yarn_lines.append(f"c\t{yarn}\t{moj}")
        class_count += 1
for obf, inter in inter_classes:
    yarn = inter_to_yarn.get(inter)
    if yarn:
        official_lines.append(f"c\t{obf}\t{yarn}")

# Methods and fields are nested under classes in tiny. Rebuild grouped.
from collections import defaultdict
yarn_members = defaultdict(list)
official_members = defaultdict(list)

for owner, desc, method_id, yarn_name in yarn_methods:
    info = by_id_inter.get(method_id)
    if not info or yarn_name in ("<init>", "<clinit>"):
        continue
    obf_owner, obf_desc, obf_name = info
    moj_name = moj_method_name(method_id, obf_owner, obf_desc, obf_name)
    yarn_owner = inter_to_yarn.get(owner, owner)
    yarn_desc = translate_desc(desc, inter_to_yarn)
    if moj_name and moj_name != yarn_name:
        yarn_members[yarn_owner].append(f"\tm\t{yarn_desc}\t{yarn_name}\t{moj_name}")
        method_count += 1
    official_desc = obf_desc
    official_members[obf_owner].append(f"\tm\t{official_desc}\t{obf_name}\t{yarn_name}")

field_by_id_inter = {fid: (owner, desc, obf_name) for owner, desc, obf_name, fid in inter_fields}
# Mojang field names: rebuild from proguard
moj_fields = {}
current_obf = None
for line in (root / "client-1.20.1.txt").read_text(errors="replace").splitlines():
    if not line or line.startswith("#"):
        continue
    if not line.startswith(" "):
        if " -> " not in line:
            continue
        left, right = line.split(" -> ")
        current_obf = right.strip().rstrip(":")
    else:
        line = line.strip()
        if " -> " not in line or "(" in line or not current_obf:
            continue
        left, obf_name = line.split(" -> ")
        moj_fields[(current_obf, obf_name.strip())] = left.split(" ")[-1]

for owner, desc, field_id, yarn_name in yarn_fields:
    info = field_by_id_inter.get(field_id)
    if not info:
        continue
    obf_owner, obf_desc, obf_name = info
    moj_name = moj_fields.get((obf_owner, obf_name))
    yarn_owner = inter_to_yarn.get(owner, owner)
    yarn_desc = translate_desc(desc, inter_to_yarn)
    if moj_name and moj_name != yarn_name:
        yarn_members[yarn_owner].append(f"\tf\t{yarn_desc}\t{yarn_name}\t{moj_name}")
    official_members[obf_owner].append(f"\tf\t{obf_desc}\t{obf_name}\t{yarn_name}")

# Re-emit with members nested. The class lines above are incomplete.
yarn_out = ["tiny\t2\t0\tyarn\tmojmap"]
for inter, yarn in yarn_classes:
    moj = inter_to_moj.get(inter)
    if not moj:
        continue
    yarn_out.append(f"c\t{yarn}\t{moj}")
    yarn_out.extend(yarn_members.get(yarn, []))

official_out = ["tiny\t2\t0\tofficial\tyarn"]
for obf, inter in inter_classes:
    yarn = inter_to_yarn.get(inter)
    if not yarn:
        continue
    official_out.append(f"c\t{obf}\t{yarn}")
    official_out.extend(official_members.get(obf, []))

(root / "yarn-to-mojmap.tiny").write_text("\n".join(yarn_out) + "\n")
(root / "official-to-yarn.tiny").write_text("\n".join(official_out) + "\n")
print("classes", class_count, "renamed methods", method_count)
print("yarn tiny lines", len(yarn_out), "official tiny lines", len(official_out))
