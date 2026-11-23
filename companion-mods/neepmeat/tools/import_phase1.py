#!/usr/bin/env python3
"""Copy phase-1 building-block assets from an upstream NeepMeat checkout.

The upstream tree is not committed. Pass it as the first argument.
"""

import json
import os
import shutil
import struct
import gzip
from pathlib import Path

UPSTREAM = Path(os.environ.get("NEEPMEAT_UPSTREAM", "/tmp/upstream/NeepMeat"))
ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
SRC_ASSETS = UPSTREAM / "src/main/resources"
GEN_ASSETS = UPSTREAM / "src/main/generated"

DYES = [
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
    "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
]

# id, kind (cube|pillar|glass), wall, material
FAMILIES = [
    ("polished_metal_bricks", "cube", True, "METAL"),
    ("polished_metal_small_bricks", "cube", True, "METAL"),
    ("polished_metal", "cube", True, "METAL"),
    ("blue_polished_metal", "cube", True, "METAL"),
    ("rusty_metal", "cube", True, "METAL"),
    ("rusty_metal_sheet", "cube", True, "METAL"),
    ("white_rough_concrete", "cube", True, "CONCRETE"),
    ("grey_rough_concrete", "cube", True, "CONCRETE"),
    ("yellow_rough_concrete", "cube", True, "CONCRETE"),
    ("red_rough_concrete", "cube", True, "CONCRETE"),
    ("blue_rough_concrete", "cube", True, "CONCRETE"),
    ("green_rough_concrete", "cube", True, "CONCRETE"),
    ("yellow_tiles", "cube", False, "TILE"),
    ("caution_block", "cube", True, "TILE"),
    ("dirty_red_tiles", "cube", True, "TILE"),
    ("dirty_white_tiles", "cube", True, "TILE"),
    ("sandy_bricks", "cube", True, "BRICKS"),
    ("meat_steel_block", "cube", True, "NETHERITE"),
    ("asbestos", "cube", True, "STONE"),
    ("asbestos_tile", "cube", True, "STONE"),
    ("corrugated_asbestos", "pillar", True, "STONE"),
    ("asbestos_shingle", "cube", False, "STONE"),
    ("bloody_bricks", "cube", True, "BRICKS"),
    ("bloody_tiles", "cube", False, "BRICKS"),
    ("reinforced_glass", "glass", False, "GLASS"),
    ("browne_block", "cube", True, "IRON"),
    ("blood_bubble_planks", "cube", True, "WOOD"),
]

ITEM_TAGS = {
    "rough_concrete": [
        "white_rough_concrete", "grey_rough_concrete", "yellow_rough_concrete",
        "red_rough_concrete", "blue_rough_concrete", "green_rough_concrete",
    ],
    "smooth_tile": [f"smooth_tile_{dye}" for dye in DYES],
    "painted_corrugated_asbestos": [f"painted_corrugated_asbestos_{dye}" for dye in DYES],
}


def find_file(rel: str):
    for base in (SRC_ASSETS, GEN_ASSETS):
        path = base / rel
        if path.is_file():
            return path
    return None


def load_json(path: Path):
    with path.open() as handle:
        return json.load(handle)


def write_json(path: Path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w") as handle:
        json.dump(data, handle, indent=2)
        handle.write("\n")


def copy_bytes(src: Path, dest: Path):
    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(src, dest)


COPIED_MODELS = set()


def copy_model(model_id: str):
    if model_id in COPIED_MODELS:
        return
    COPIED_MODELS.add(model_id)
    if ":" in model_id:
        namespace, path = model_id.split(":", 1)
    else:
        namespace, path = "minecraft", model_id
    if namespace != "neepmeat":
        return
    rel = f"assets/neepmeat/models/{path}.json"
    src = find_file(rel)
    if src is None:
        raise SystemExit(f"missing model {model_id} ({rel})")
    data = load_json(src)
    data.pop("__comment__", None)
    parent = data.get("parent")
    if isinstance(parent, str):
        copy_model(parent if ":" in parent else "minecraft:" + parent)
    textures = data.get("textures") or {}
    for value in textures.values():
        if not isinstance(value, str) or value.startswith("#"):
            continue
        tex = value if ":" in value else "minecraft:" + value
        tex_ns, tex_path = tex.split(":", 1)
        if tex_ns != "neepmeat":
            continue
        tex_rel = f"assets/neepmeat/textures/{tex_path}.png"
        tex_src = find_file(tex_rel)
        if tex_src is None:
            raise SystemExit(f"missing texture {tex} for {model_id}")
        copy_bytes(tex_src, RES / tex_rel)
    parent = data.get("parent")
    if isinstance(parent, str) and parent.startswith("meatlib:block/concrete_wall_"):
        # Rough concrete walls parented meatlib's custom wall models. Those files are not
        # in this jar. Vanilla wall templates take the same #wall texture. The inventory
        # model is block/wall_inventory, not template_wall_inventory.
        suffix = parent.removeprefix("meatlib:block/concrete_wall_")
        vanilla = "wall_inventory" if suffix == "inventory" else "template_wall_" + suffix
        data["parent"] = "minecraft:block/" + vanilla
    if "reinforced_glass" in json.dumps(textures):
        data["render_type"] = "minecraft:cutout"
    write_json(RES / rel, data)


def collect_models(node, found):
    if isinstance(node, dict):
        model = node.get("model")
        if isinstance(model, str):
            found.add(model)
        for value in node.values():
            collect_models(value, found)
    elif isinstance(node, list):
        for value in node:
            collect_models(value, found)


def with_waterlogged(data):
    variants = data.get("variants")
    if not isinstance(variants, dict) or "multipart" in data:
        return data
    if any("waterlogged=" in key for key in variants):
        return data
    if not any("shape=" in key or key.startswith("type=") or ",type=" in key for key in variants):
        return data
    expanded = {}
    for key, value in variants.items():
        expanded[f"{key},waterlogged=false"] = value
        expanded[f"{key},waterlogged=true"] = value
    data = dict(data)
    data["variants"] = expanded
    data.pop("__comment__", None)
    return data


def copy_blockstate(block_id: str):
    rel = f"assets/neepmeat/blockstates/{block_id}.json"
    src = find_file(rel)
    if src is None:
        return False
    data = load_json(src)
    data.pop("__comment__", None)
    data = with_waterlogged(data)
    write_json(RES / rel, data)
    models = set()
    collect_models(data, models)
    for model in models:
        copy_model(model)
    item_rel = f"assets/neepmeat/models/item/{block_id}.json"
    item_src = find_file(item_rel)
    if item_src is not None:
        item = load_json(item_src)
        item.pop("__comment__", None)
        parent = item.get("parent")
        if isinstance(parent, str):
            copy_model(parent if ":" in parent else "minecraft:" + parent)
        write_json(RES / item_rel, item)
    return True


def variant_ids(base, wall):
    ids = [base, f"{base}_stairs", f"{base}_slab"]
    if wall:
        ids.append(f"{base}_wall")
    return ids


def build_id_plan():
    """Return (blocks, java_calls) where blocks is a list of dicts."""
    blocks = []
    dropped = []

    def add(block_id, kind, material, role):
        if not copy_blockstate(block_id):
            dropped.append(block_id)
            return False
        blocks.append({"id": block_id, "kind": kind, "material": material, "role": role})
        return True

    for base, kind, wall, material in FAMILIES:
        if not add(base, kind, material, "base"):
            raise SystemExit(f"base blockstate missing for {base}")
        add(f"{base}_stairs", "stairs", material, "stairs")
        add(f"{base}_slab", "slab", material, "slab")
        if wall:
            add(f"{base}_wall", "wall", material, "wall")

    for dye in DYES:
        add(f"smooth_tile_{dye}", "tinted", "TILE", "tint")
        add(f"painted_corrugated_asbestos_{dye}", "pillar", "STONE", "painted_pillar")

    return blocks, dropped


def recipe_refs(node, found):
    if isinstance(node, dict):
        item = node.get("item")
        if isinstance(item, str):
            found.append(("item", item))
        tag = node.get("tag")
        if isinstance(tag, str):
            found.append(("tag", tag))
        ident = node.get("id")
        if isinstance(ident, str) and (ident.startswith("neepmeat:") or ident.startswith("minecraft:")):
            found.append(("item", ident))
        for value in node.values():
            recipe_refs(value, found)
    elif isinstance(node, list):
        for value in node:
            recipe_refs(value, found)
    elif isinstance(node, str) and node.startswith("neepmeat:"):
        found.append(("item", node))


def convert_recipe(data):
    data = json.loads(json.dumps(data))
    data.pop("__comment__", None)
    result = data.get("result")
    if isinstance(result, str):
        count = data.pop("count", 1)
        data["result"] = {"id": result, "item": result, "count": count}
    elif isinstance(result, dict) and "item" in result and "id" not in result:
        result["id"] = result["item"]
    return data


def copy_recipes(known_ids):
    known_items = {f"neepmeat:{block_id}" for block_id in known_ids}
    known_tags = {f"neepmeat:{name}" for name in ITEM_TAGS}
    copied = []
    skipped = []
    seen = set()
    roots = [
        SRC_ASSETS / "data/neepmeat/recipes",
        GEN_ASSETS / "data/neepmeat/recipes",
    ]
    for root in roots:
        if not root.is_dir():
            continue
        for path in sorted(root.rglob("*.json")):
            data = load_json(path)
            refs = []
            recipe_refs(data, refs)
            bad = False
            reason = ""
            for kind, value in refs:
                if kind == "tag" and value.startswith("neepmeat:") and value not in known_tags:
                    bad = True
                    reason = f"tag {value}"
                    break
                if kind == "item" and not value.startswith("minecraft:") and value not in known_items:
                    bad = True
                    reason = f"item {value}"
                    break
            name = path.name
            if name in seen:
                name = path.stem + "_extra.json" if not name.endswith("_extra.json") else path.name
            if bad:
                skipped.append(f"{path.name}: {reason}")
                continue
            seen.add(name)
            write_json(RES / "data/neepmeat/recipe" / name, convert_recipe(data))
            copied.append(name)
    return copied, skipped


def write_tags(blocks):
    def ids(pred):
        return [f"neepmeat:{block['id']}" for block in blocks if pred(block)]

    pickaxe = ids(lambda b: b["material"] != "WOOD" and b["material"] != "GLASS")
    axe = ids(lambda b: b["material"] == "WOOD")
    stone_tool = ids(lambda b: b["material"] in {"METAL", "IRON"})
    diamond_tool = ids(lambda b: b["material"] == "NETHERITE")
    stairs = ids(lambda b: b["role"] == "stairs")
    slabs = ids(lambda b: b["role"] == "slab")
    walls = ids(lambda b: b["role"] == "wall")
    planks = ids(lambda b: b["id"] == "blood_bubble_planks")

    def tag(rel, values):
        write_json(RES / rel, {"replace": False, "values": values})

    tag("data/minecraft/tags/block/mineable/pickaxe.json", pickaxe)
    tag("data/minecraft/tags/block/mineable/axe.json", axe)
    tag("data/minecraft/tags/block/needs_stone_tool.json", stone_tool)
    tag("data/minecraft/tags/block/needs_diamond_tool.json", diamond_tool)
    tag("data/minecraft/tags/block/stairs.json", stairs)
    tag("data/minecraft/tags/block/slabs.json", slabs)
    tag("data/minecraft/tags/block/walls.json", walls)
    tag("data/minecraft/tags/block/planks.json", planks)
    tag("data/minecraft/tags/item/stairs.json", stairs)
    tag("data/minecraft/tags/item/slabs.json", slabs)
    tag("data/minecraft/tags/item/walls.json", walls)
    tag("data/minecraft/tags/item/planks.json", planks)
    for name, members in ITEM_TAGS.items():
        tag(f"data/neepmeat/tags/item/{name}.json", [f"neepmeat:{member}" for member in members])


def write_loot(blocks):
    for block in blocks:
        block_id = block["id"]
        write_json(RES / f"data/neepmeat/loot_table/blocks/{block_id}.json", {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "conditions": [{"condition": "minecraft:survives_explosion"}],
                "entries": [{"type": "minecraft:item", "name": f"neepmeat:{block_id}"}],
            }],
        })


def write_structure():
    def named(tag_type, name, payload):
        raw = name.encode("utf-8")
        return bytes([tag_type]) + struct.pack(">H", len(raw)) + raw + payload

    def string_tag(name, value):
        raw = value.encode("utf-8")
        return named(8, name, struct.pack(">H", len(raw)) + raw)

    def int_tag(name, value):
        return named(3, name, struct.pack(">i", value))

    def int_list(name, values):
        payload = bytes([3]) + struct.pack(">i", len(values))
        for value in values:
            payload += struct.pack(">i", value)
        return named(9, name, payload)

    def compound_list(name, bodies):
        payload = bytes([10]) + struct.pack(">i", len(bodies))
        for body in bodies:
            payload += body
        return named(9, name, payload)

    air = string_tag("Name", "minecraft:air") + b"\x00"
    blocks = []
    index = 0
    for y in range(3):
        for z in range(3):
            for x in range(3):
                blocks.append(int_list("pos", [x, y, z]) + int_tag("state", 0) + b"\x00")
                index += 1
    root_body = (
        int_tag("DataVersion", 3955)
        + int_list("size", [3, 3, 3])
        + compound_list("entities", [])
        + compound_list("blocks", blocks)
        + compound_list("palette", [air])
        + b"\x00"
    )
    blob = named(10, "", root_body)
    dest = RES / "data/neepmeat/structure/empty.nbt"
    dest.parent.mkdir(parents=True, exist_ok=True)
    with gzip.open(dest, "wb") as handle:
        handle.write(blob)


JAVA_HEADER = """\
/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Derivative NeoForge 1.21.1 port for the Autisnautics pack. Upstream source headers
 * release the work under the GNU General Public License, version 3 or later. The
 * LICENSE file shipped with NeepMeat is the GNU Lesser General Public License,
 * version 3. This port does not relicense the work.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

"""


def java_string(value):
    return '"' + value + '"'


def write_java(blocks):
    lines = [
        JAVA_HEADER,
        "package com.neep.neepmeat.building;",
        "",
        "import com.neep.neepmeat.NeepMeat;",
        "import net.minecraft.world.item.BlockItem;",
        "import net.minecraft.world.item.CreativeModeTab;",
        "import net.minecraft.world.item.DyeColor;",
        "import net.minecraft.world.item.Item;",
        "import net.minecraft.world.item.ItemStack;",
        "import net.minecraft.world.level.block.Block;",
        "import net.minecraft.world.level.block.RotatedPillarBlock;",
        "import net.minecraft.world.level.block.SlabBlock;",
        "import net.minecraft.world.level.block.SoundType;",
        "import net.minecraft.world.level.block.StairBlock;",
        "import net.minecraft.world.level.block.WallBlock;",
        "import net.minecraft.world.level.block.state.BlockBehaviour;",
        "import net.minecraft.world.level.material.MapColor;",
        "import net.minecraft.network.chat.Component;",
        "import net.neoforged.neoforge.registries.DeferredBlock;",
        "import net.neoforged.neoforge.registries.DeferredHolder;",
        "import net.neoforged.neoforge.registries.DeferredItem;",
        "",
        "import java.util.ArrayList;",
        "import java.util.List;",
        ""
        "/**",
        " * Building blocks whose upstream classes were full cubes, pillars, stairs, slabs, or walls.",
        " * Generated by tools/import_phase1.py from NeepMeat 0.30.0-beta+1.20.1.",
        " */",
        "public final class BuildingBlocks {",
        "    public static final List<DeferredItem<? extends Item>> TAB_ITEMS = new ArrayList<>();",
        "    public static final List<Tint> TINTS = new ArrayList<>();",
        "    private static DeferredBlock<StairBlock> glassStairs;",
        "",
        "    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = NeepMeat.TABS.register(\"general\", () -> CreativeModeTab.builder()",
        "            .title(Component.translatable(\"itemGroup.neepmeat.general\"))",
        "            .icon(() -> new ItemStack(TAB_ITEMS.getFirst().get()))",
        "            .displayItems((params, output) -> TAB_ITEMS.forEach(item -> output.accept(item.get())))",
        "            .build());",
        "",
        "    private BuildingBlocks() {}",
        "",
        "    public static void bootstrap() {",
        "        // Static registration runs when this class is initialized.",
        "    }",
        "",
        "    static {",
        "        registerAll();",
        "    }",
        "",
        "    private static void registerAll() {",
    ]
    by_base = {}
    for block in blocks:
        by_base.setdefault(family_base(block), []).append(block)

    emitted = set()
    for base, kind, wall, material in FAMILIES:
        members = [b for b in blocks if family_base(b) == base or b["id"] == base]
        # members include base and variants because family_base of stairs is the base
        wanted = [b for b in blocks if b["id"] == base or b["id"].startswith(base + "_")]
        # blood_bubble_planks must not swallow unrelated ids; startswith is ok if bases are not prefixes of each other.
        if any(other != base and (other.startswith(base + "_") or base.startswith(other + "_")) for other, *_ in FAMILIES):
            pass
        lines.append(f"        family({java_string(base)}, Kind.{kind.upper()}, Material.{material}, {str(wall).lower()});")
        emitted.add(base)
    lines.append("        tinted(\"smooth_tile\", Material.SMOOTH);")
    lines.append("        paintedPillars();")
    lines.append("    }")
    lines += [
        "",
        "    private static void family(String base, Kind kind, Material material, boolean wall) {",
        "        DeferredBlock<?> block = switch (kind) {",
        "            case CUBE -> cube(base, material);",
        "            case PILLAR -> pillar(base, material);",
        "            case GLASS -> glass(base, material);",
        "            case STAIRS, SLAB, WALL, TINTED -> throw new IllegalStateException(kind.name());",
        "        };",
        "        if (kind != Kind.GLASS) {",
        "            stairs(base + \"_stairs\", block, material);",
        "            slab(base + \"_slab\", block, material);",
        "        }",
        "        if (wall) {",
        "            wall(base + \"_wall\", material);",
        "        }",
        "    }",
        "",
        "    private static void tinted(String prefix, Material material) {",
        "        for (DyeColor color : DyeColor.values()) {",
        "            String id = prefix + \"_\" + color.getName();",
        "            DeferredBlock<Block> block = cube(id, material);",
        "            TINTS.add(new Tint(block, TAB_ITEMS.getLast(), color));",
        "        }",
        "    }",
        "",
        "    private static void paintedPillars() {",
        "        for (DyeColor color : DyeColor.values()) {",
        "            pillar(\"painted_corrugated_asbestos_\" + color.getName(), Material.STONE);",
        "        }",
        "    }",
        "",
        "    private static DeferredBlock<Block> cube(String id, Material material) {",
        "        DeferredBlock<Block> block = NeepMeat.BLOCKS.register(id, () -> new Block(material.properties()));",
        "        item(id, block);",
        "        return block;",
        "    }",
        "",
        "    private static DeferredBlock<RotatedPillarBlock> pillar(String id, Material material) {",
        "        DeferredBlock<RotatedPillarBlock> block = NeepMeat.BLOCKS.register(id, () -> new RotatedPillarBlock(material.properties()));",
        "        item(id, block);",
        "        return block;",
        "    }",
        "",
        "    private static DeferredBlock<ReinforcedGlassBlock> glass(String id, Material material) {",
        "        DeferredBlock<ReinforcedGlassBlock> block = NeepMeat.BLOCKS.register(id,",
        "                () -> new ReinforcedGlassBlock(material.properties(), () -> glassStairs.get()));",
        "        item(id, block);",
        "        glassStairs = stairs(id + \"_stairs\", block, material);",
        "        slab(id + \"_slab\", block, material);",
        "        return block;",
        "    }",
        "",
        "    private static DeferredBlock<StairBlock> stairs(String id, DeferredBlock<?> base, Material material) {",
        "        DeferredBlock<StairBlock> block = NeepMeat.BLOCKS.register(id,",
        "                () -> new StairBlock(base.get().defaultBlockState(), material.properties()));",
        "        item(id, block);",
        "        return block;",
        "    }",
        "",
        "    private static DeferredBlock<SlabBlock> slab(String id, DeferredBlock<?> base, Material material) {",
        "        DeferredBlock<SlabBlock> block = NeepMeat.BLOCKS.register(id, () -> new SlabBlock(material.properties()));",
        "        item(id, block);",
        "        return block;",
        "    }",
        "",
        "    private static DeferredBlock<WallBlock> wall(String id, Material material) {",
        "        DeferredBlock<WallBlock> block = NeepMeat.BLOCKS.register(id, () -> new WallBlock(material.properties()));",
        "        item(id, block);",
        "        return block;",
        "    }",
        "",
        "    private static void item(String id, DeferredBlock<?> block) {",
        "        TAB_ITEMS.add(NeepMeat.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));",
        "    }",
        "",
        "    public enum Kind { CUBE, PILLAR, GLASS, STAIRS, SLAB, WALL, TINTED }",
        "",
        "    public enum Material {",
        "        METAL {",
        "            @Override public BlockBehaviour.Properties properties() {",
        "                return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F)",
        "                        .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops();",
        "            }",
        "        },",
        "        CONCRETE {",
        "            @Override public BlockBehaviour.Properties properties() {",
        "                return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.8F)",
        "                        .sound(SoundType.STONE).requiresCorrectToolForDrops();",
        "            }",
        "        },",
        "        TILE {",
        "            @Override public BlockBehaviour.Properties properties() {",
        "                return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.8F)",
        "                        .sound(SoundType.STONE).requiresCorrectToolForDrops();",
        "            }",
        "        },",
        "        SMOOTH {",
        "            @Override public BlockBehaviour.Properties properties() {",
        "                return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F)",
        "                        .sound(SoundType.STONE).requiresCorrectToolForDrops();",
        "            }",
        "        },",
        "        BRICKS {",
        "            @Override public BlockBehaviour.Properties properties() {",
        "                return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(2.0F, 6.0F)",
        "                        .sound(SoundType.STONE).requiresCorrectToolForDrops();",
        "            }",
        "        },",
        "        STONE {",
        "            @Override public BlockBehaviour.Properties properties() {",
        "                return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 6.0F)",
        "                        .sound(SoundType.STONE).requiresCorrectToolForDrops();",
        "            }",
        "        },",
        "        IRON {",
        "            @Override public BlockBehaviour.Properties properties() {",
        "                return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F)",
        "                        .sound(SoundType.METAL).requiresCorrectToolForDrops();",
        "            }",
        "        },",
        "        NETHERITE {",
        "            @Override public BlockBehaviour.Properties properties() {",
        "                return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(50.0F, 1200.0F)",
        "                        .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops();",
        "            }",
        "        },",
        "        WOOD {",
        "            @Override public BlockBehaviour.Properties properties() {",
        "                return BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_STEM).strength(2.0F, 3.0F)",
        "                        .sound(SoundType.WOOD);",
        "            }",
        "        },",
        "        GLASS {",
        "            @Override public BlockBehaviour.Properties properties() {",
        "                return BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(1.0F)",
        "                        .sound(SoundType.GLASS).noOcclusion()",
        "                        .isValidSpawn((state, level, pos, type) -> false)",
        "                        .isRedstoneConductor((state, level, pos) -> false)",
        "                        .isSuffocating((state, level, pos) -> false)",
        "                        .isViewBlocking((state, level, pos) -> false);",
        "            }",
        "        };",
        "        public abstract BlockBehaviour.Properties properties();",
        "    }",
        "",
        "    public record Tint(DeferredBlock<Block> block, DeferredItem<? extends Item> item, DyeColor color) {}",
        "}",
        "",
    ]
    # The family() method always registers stairs and slabs. Blocks whose assets were
    # dropped must not be referenced. Fail here if a variant asset is missing so the
    # Java and the resources stay aligned.
    missing = []
    for base, kind, wall, material in FAMILIES:
        for suffix in ("", "_stairs", "_slab") + (("_wall",) if wall else ()):
            if not any(block["id"] == base + suffix for block in blocks):
                missing.append(base + suffix)
    for dye in DYES:
        for block_id in (f"smooth_tile_{dye}", f"painted_corrugated_asbestos_{dye}"):
            if not any(block["id"] == block_id for block in blocks):
                missing.append(block_id)
    if missing:
        raise SystemExit("refusing to emit Java; missing assets for: " + ", ".join(missing))

    dest = ROOT / "src/main/java/com/neep/neepmeat/building/BuildingBlocks.java"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text("\n".join(lines) + "\n")


def family_base(block):
    block_id = block["id"]
    for suffix in ("_stairs", "_slab", "_wall"):
        if block_id.endswith(suffix):
            return block_id[: -len(suffix)]
    return block_id


def main():
    if not SRC_ASSETS.is_dir():
        raise SystemExit(f"upstream resources not found at {SRC_ASSETS}")
    blocks, dropped = build_id_plan()
    if dropped:
        raise SystemExit("dropped blocks (missing blockstates): " + ", ".join(dropped))
    known = [block["id"] for block in blocks]
    recipes, skipped = copy_recipes(known)
    write_tags(blocks)
    write_loot(blocks)
    write_structure()
    for lang in ("en_us.json", "zh_cn.json"):
        src = SRC_ASSETS / "assets/neepmeat/lang" / lang
        copy_bytes(src, RES / "assets/neepmeat/lang" / lang)
    icon = SRC_ASSETS / "assets/neepmeat/icon.png"
    copy_bytes(icon, RES / "assets/neepmeat/icon.png")
    copy_bytes(icon, RES / "icon.png")
    write_java(blocks)
    manifest = ROOT / "docs/phase1-block-ids.txt"
    manifest.parent.mkdir(parents=True, exist_ok=True)
    manifest.write_text("\n".join(known) + "\n")
    print(f"blocks {len(known)}")
    print(f"recipes {len(recipes)}")
    print(f"skipped recipes {len(skipped)}")
    (ROOT / "docs/phase1-skipped-recipes.txt").write_text("\n".join(skipped) + "\n")


if __name__ == "__main__":
    main()
