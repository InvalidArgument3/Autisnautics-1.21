#!/usr/bin/env python3
"""Yarn method/field simple-name -> Mojmap simple-name when unambiguous."""
from collections import defaultdict
from pathlib import Path

root = Path("/tmp/remap")

def parse_tiny_members(path):
    methods = {}  # id -> (owner, desc, name0, name1)  name0 is second name column
    fields = {}
    current = None
    for line in path.read_text(errors="replace").splitlines():
        if line.startswith("c\t"):
            current = line.split("\t")[1]
        elif line.startswith("\tm\t") and current:
            p = line.split("\t")
            # p[2]=desc p[3]=ns0 name p[4]=ns1 name
            methods[p[4] if False else p[3]] = (current, p[2], p[3], p[4])
        elif line.startswith("\tf\t") and current:
            p = line.split("\t")
            fields[p[3]] = (current, p[2], p[3], p[4])
    return methods, fields

# WARNING: method ids are ns0 names for intermediary file (obf) not method_
# Re-parse properly.

def parse(path):
    methods = []
    fields = []
    current = None
    for line in path.read_text(errors="replace").splitlines():
        if line.startswith("c\t"):
            current = line.split("\t")[1]
        elif line.startswith("\tm\t") and current:
            p = line.split("\t")
            methods.append((current, p[2], p[3], p[4]))
        elif line.startswith("\tf\t") and current:
            p = line.split("\t")
            fields.append((current, p[2], p[3], p[4]))
    return methods, fields

inter_m, inter_f = parse(root/"intermediary.tiny")  # owner obf, desc obf, obf name, method_id
yarn_m, yarn_f = parse(root/"yarn.tiny")  # owner inter, desc inter, method_id, yarn name

by_id_inter = {}
for owner, desc, obf_name, mid in inter_m:
    by_id_inter[mid] = (owner, desc, obf_name)
by_id_yarn = {}
for owner, desc, mid, yarn_name in yarn_m:
    by_id_yarn[mid] = (owner, yarn_name)

obf_to_moj = {}
moj_methods = defaultdict(list)  # (obf owner, obf name) -> list of (arg types moj dotted, moj name)
current_moj = current_obf = None
for line in (root/"client-1.20.1.txt").read_text(errors="replace").splitlines():
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
        line=line.strip()
        if " -> " not in line or not current_obf:
            continue
        left, obf_name = line.split(" -> ")
        obf_name=obf_name.strip()
        if "(" in left:
            sig=left.split(" ")[-1]
            name=sig[:sig.index("(")]
            args=sig[sig.index("(")+1:sig.index(")")]
            arglist=[a.strip() for a in args.split(",")] if args else []
            moj_methods[(current_obf, obf_name)].append((tuple(arglist), name))

def obf_desc_to_types(desc):
    types=[]
    i=0
    # only args inside ()
    if desc.startswith("("):
        desc=desc[1:desc.index(")")]
    while i < len(desc):
        dim=""
        while desc[i]=="[":
            dim+="[]"
            i+=1
        if desc[i]=="L":
            j=desc.index(";", i)
            internal=desc[i+1:j]
            i=j+1
            moj=obf_to_moj.get(internal, internal).replace("/", ".")
            types.append(moj+dim)
        else:
            prim={"B":"byte","C":"char","D":"double","F":"float","I":"int","J":"long","S":"short","Z":"boolean","V":"void"}[desc[i]]
            i+=1
            types.append(prim+dim)
    return tuple(types)

yarn_to_moj_method = {}  # yarn simple -> set of moj simple
ambiguous=0
mapped=0
missing=0
examples=[]
for mid, (owner_inter, yarn_name) in by_id_yarn.items():
    info=by_id_inter.get(mid)
    if not info or yarn_name in ("<init>","<clinit>"):
        continue
    obf_owner, obf_desc, obf_name = info
    types=obf_desc_to_types(obf_desc)
    cands=moj_methods.get((obf_owner, obf_name), [])
    hit=None
    for argt, moj_name in cands:
        if argt==types:
            hit=moj_name
            break
    if hit is None and len(cands)==1:
        hit=cands[0][1]
    if hit is None:
        missing+=1
        continue
    mapped+=1
    yarn_to_moj_method.setdefault(yarn_name, set()).add(hit)
    if len(examples)<12 and yarn_name!=hit and yarn_name in ("getBlockState","getWorld","isClient","getPos","markDirty","getCachedState","spawnParticles"):
        examples.append((yarn_name, hit, types[:3]))

unique={}
multi={}
for y, s in yarn_to_moj_method.items():
    if len(s)==1:
        moj=next(iter(s))
        if moj!=y:
            unique[y]=moj
    else:
        multi[y]=s

print("mapped ids", mapped, "missing", missing)
print("unique renames", len(unique), "ambiguous names", len(multi))
print("examples", examples)
print("sample unique", list(unique.items())[:25])
Path=root
(root/"method_rename.tsv").write_text("\n".join(f"{a}\t{b}" for a,b in sorted(unique.items()))+"\n")
print("multi sample")
for k,v in list(multi.items())[:8]:
    print(k, v)
