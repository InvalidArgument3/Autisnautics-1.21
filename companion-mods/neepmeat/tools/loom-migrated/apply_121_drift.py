#!/usr/bin/env python3
"""Rewrite common 1.20.1 Mojmap calls to 1.21.1 on a copy of the Loom tree.

Does not modify tools/loom-migrated. Pass a destination directory.
"""
import re
import shutil
import sys
from pathlib import Path

SOURCE = Path(__file__).resolve().parent

REPLACEMENTS = [
    (re.compile(r"\bnew ResourceLocation\(\s*([^,\n]+),\s*([^)\n]+)\)"),
     r"ResourceLocation.fromNamespaceAndPath(\1, \2)"),
    (re.compile(r"\bnew ResourceLocation\(\s*([^,\n)]+)\)"),
     r"ResourceLocation.parse(\1)"),
    (re.compile(r"(public|protected) void saveAdditional\(CompoundTag (\w+)\)"),
     r"\1 void saveAdditional(CompoundTag \2, HolderLookup.Provider registries)"),
    (re.compile(r"(public|protected) void load\(CompoundTag (\w+)\)"),
     r"\1 void loadAdditional(CompoundTag \2, HolderLookup.Provider registries)"),
    (re.compile(r"super\.saveAdditional\(([^)\n]+)\)"),
     r"super.saveAdditional(\1, registries)"),
    (re.compile(r"super\.load\(([^)\n]+)\)"),
     r"super.loadAdditional(\1, registries)"),
    (re.compile(r"\bgetUpdateTag\(\)"),
     r"getUpdateTag(registries)"),
    (re.compile(r"(public|protected) void onRemove\("),
     r"protected void onRemove("),
    (re.compile(r"(public|protected) BlockState rotate\("),
     r"protected BlockState rotate("),
    (re.compile(r"(public|protected) BlockState mirror\("),
     r"protected BlockState mirror("),
    (re.compile(r"(public|protected) InteractionResult use\((.*?)InteractionHand \w+, (BlockHitResult \w+)\)"),
     r"protected InteractionResult useWithoutItem(\2\3)"),
    (re.compile(r"super\.use\(([^,]+), ([^,]+), ([^,]+), ([^,]+), [^,]+, ([^)]+)\)"),
     r"super.useWithoutItem(\1, \2, \3, \4, \5)"),
]


def drift(text):
    for pattern, repl in REPLACEMENTS:
        text = pattern.sub(repl, text)
    if "HolderLookup" in text and "import net.minecraft.core.HolderLookup;" not in text:
        text = text.replace("package ", "package ", 1)
        newline = text.find("\n", text.find("package "))
        text = text[:newline + 1] + "\nimport net.minecraft.core.HolderLookup;\n" + text[newline + 1:]
    return text


def main():
    destination = Path(sys.argv[1])
    if destination.exists():
        shutil.rmtree(destination)
    count = 0
    changed = 0
    for root_name in ("neepmeat", "meatlib"):
        root = SOURCE / root_name
        for path in root.rglob("*.java"):
            relative = path.relative_to(SOURCE)
            target = destination / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            original = path.read_text(errors="replace")
            updated = drift(original)
            target.write_text(updated)
            count += 1
            if updated != original:
                changed += 1
    print(f"wrote {count} files, {changed} changed, to {destination}")


if __name__ == "__main__":
    main()
