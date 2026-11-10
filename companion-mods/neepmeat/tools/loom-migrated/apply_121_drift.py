#!/usr/bin/env python3
"""Rewrite common 1.20.1 Mojmap calls to 1.21.1 on a copy of the Loom tree.

Does not modify tools/loom-migrated. Pass a destination directory.
"""
import re
import shutil
import sys
from pathlib import Path

SOURCE = Path(__file__).resolve().parent

IMPORT_REWRITES = {
    "net.fabricmc.fabric.api.transfer.v1.item.ItemVariant": "com.neep.meatlib.transfer.ItemVariant",
    "net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant": "com.neep.meatlib.transfer.FluidVariant",
    "net.fabricmc.fabric.api.transfer.v1.storage.Storage": "com.neep.meatlib.transfer.Storage",
    "net.fabricmc.fabric.api.transfer.v1.storage.StorageView": "com.neep.meatlib.transfer.StorageView",
    "net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount": "com.neep.meatlib.transfer.ResourceAmount",
    "net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage": "com.neep.meatlib.transfer.SingleVariantStorage",
    "net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage": "com.neep.meatlib.transfer.CombinedStorage",
    "net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage": "com.neep.meatlib.transfer.Storage",
    "net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage": "com.neep.meatlib.transfer.ExtractionOnlyStorage",
    "net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage": "com.neep.meatlib.transfer.InventoryStorage",
    "net.fabricmc.fabric.api.transfer.v1.transaction.Transaction": "com.neep.meatlib.transfer.Transaction",
    "net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext": "com.neep.meatlib.transfer.TransactionContext",
    "net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants": "com.neep.meatlib.transfer.FluidConstants",
    "net.fabricmc.api.EnvType": "net.neoforged.api.distmarker.Dist",
    "net.fabricmc.api.Environment": "net.neoforged.api.distmarker.OnlyIn",
    "net.fabricmc.fabric.api.util.NbtType": "net.minecraft.nbt.Tag",
}

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
    (re.compile(r"CompoundTag getUpdateTag\(\)"),
     "CompoundTag getUpdateTag(HolderLookup.Provider registries)"),
    (re.compile(r"super\.getUpdateTag\(\)"),
     "super.getUpdateTag(registries)"),
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
    (re.compile(r"@Environment\(EnvType\.CLIENT\)"), "@OnlyIn(Dist.CLIENT)"),
    (re.compile(r"\bEnvType\.CLIENT\b"), "Dist.CLIENT"),
    (re.compile(r"\bEnvType\.SERVER\b"), "Dist.DEDICATED_SERVER"),
    (re.compile(r"\bNbtType\.COMPOUND\b"), "Tag.TAG_COMPOUND"),
    (re.compile(r"\bNbtType\.LIST\b"), "Tag.TAG_LIST"),
    (re.compile(r"\bNbtType\.STRING\b"), "Tag.TAG_STRING"),
    (re.compile(r"\bNbtType\.INT\b"), "Tag.TAG_INT"),
    (re.compile(r"\bFluidVariant\.blank\(\)"), "FluidVariant.BLANK"),
    (re.compile(r"\bItemVariant\.blank\(\)"), "ItemVariant.BLANK"),
    (re.compile(r"protected void defineSynchedData\(\)"),
     "protected void defineSynchedData(SynchedEntityData.Builder builder)"),
]


def drift(text):
    for source, target in IMPORT_REWRITES.items():
        text = text.replace(source, target)
    for pattern, repl in REPLACEMENTS:
        text = pattern.sub(repl, text)
    if "HolderLookup" in text and "import net.minecraft.core.HolderLookup;" not in text:
        newline = text.find("\n", text.find("package "))
        text = text[:newline + 1] + "\nimport net.minecraft.core.HolderLookup;\n" + text[newline + 1:]
    if "SynchedEntityData" in text and "import net.minecraft.network.syncher.SynchedEntityData;" not in text:
        newline = text.find("\n", text.find("package "))
        text = text[:newline + 1] + "\nimport net.minecraft.network.syncher.SynchedEntityData;\n" + text[newline + 1:]
    if "OnlyIn" in text and "import net.neoforged.api.distmarker.OnlyIn;" not in text:
        newline = text.find("\n", text.find("package "))
        text = text[:newline + 1] + "\nimport net.neoforged.api.distmarker.Dist;\nimport net.neoforged.api.distmarker.OnlyIn;\n" + text[newline + 1:]
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
