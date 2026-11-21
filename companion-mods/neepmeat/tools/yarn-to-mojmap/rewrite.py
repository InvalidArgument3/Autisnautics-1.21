#!/usr/bin/env python3
"""Rewrite Yarn Java sources to 1.20.1 Mojmap using the class map and unique method renames."""
import re
import sys
from pathlib import Path

classes = []
for line in Path("/tmp/remap/class_map.tsv").read_text().splitlines():
    a, b = line.split("\t")
    if a != b:
        classes.append((a, b))
classes.sort(key=lambda ab: len(ab[0]), reverse=True)

methods = {}
for line in Path("/tmp/remap/method_rename.tsv").read_text().splitlines():
    if not line:
        continue
    a, b = line.split("\t")
    if a != b and a.isidentifier() and b.isidentifier():
        methods[a] = b

# simple class name replacements for names that change and are unambiguous
simple = {}
collisions = {}
for a, b in classes:
    sa, sb = a.rsplit(".", 1)[-1], b.rsplit(".", 1)[-1]
    if sa == sb:
        continue
    collisions.setdefault(sa, set()).add(sb)
for sa, sbs in collisions.items():
    if len(sbs) == 1:
        simple[sa] = next(iter(sbs))

owned = set()
for base in (Path("/tmp/upstream/NeepMeat/subprojects/meatlib/src/main/java"), Path("/tmp/upstream/NeepMeat/src/main/java")):
    if base.exists():
        for java in base.rglob("*.java"):
            owned.add(java.stem)
upper_simple = {k: v for k, v in simple.items() if k[:1].isupper() and k not in owned}
method_re = re.compile(r"\b(" + "|".join(sorted(map(re.escape, methods), key=len, reverse=True)) + r")\b(?=\s*\()")
type_re = re.compile(r"\b(" + "|".join(sorted(map(re.escape, upper_simple), key=len, reverse=True)) + r")\b")

def protected_spans(text):
    spans = []
    i = 0
    n = len(text)
    while i < n:
        if text.startswith("//", i):
            j = text.find("\n", i)
            j = n if j < 0 else j
            spans.append((i, j))
            i = j
        elif text.startswith("/*", i):
            j = text.find("*/", i + 2)
            j = n if j < 0 else j + 2
            spans.append((i, j))
            i = j
        elif text[i] in "\"'":
            q = text[i]
            j = i + 1
            while j < n:
                if text[j] == "\\":
                    j += 2
                    continue
                if text[j] == q:
                    j += 1
                    break
                j += 1
            spans.append((i, j))
            i = j
        else:
            i += 1
    return spans

def rewrite(text):
    for a, b in classes:
        text = text.replace(a, b)
    spans = protected_spans(text)

    def in_span(idx):
        for s, e in spans:
            if s <= idx < e:
                return True
            if s > idx:
                return False
        return False

    def sub_method(m):
        if in_span(m.start()):
            return m.group(0)
        return methods.get(m.group(1), m.group(0))

    def sub_type(m):
        if in_span(m.start()):
            return m.group(0)
        if m.start() > 0 and text[m.start() - 1] == ".":
            return m.group(0)
        return upper_simple.get(m.group(1), m.group(0))

    text = method_re.sub(sub_method, text)
    return type_re.sub(sub_type, text)

src = Path(sys.argv[1])
dst = Path(sys.argv[2])
count = 0
for path in src.rglob("*.java"):
    rel = path.relative_to(src)
    out = dst / rel
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(rewrite(path.read_text(errors="replace")))
    count += 1
print("rewrote", count, "to", dst)
