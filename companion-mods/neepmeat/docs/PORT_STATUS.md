# Port status

Upstream is about 1,889 Java files in NeepMeat, 310 in meatlib, 38 in NEEPSpeak, and 11 in Myron, plus 4,244 resource files. A line-by-line Fabric 1.20.1 Yarn port does not compile on NeoForge 1.21.1: registries, transfer APIs, menus, networking, data components, and rendering all moved. This phase gets a real mod on the pack toolchain and ports the slice that can be honest full blocks.

Legend: **ported** means it registers and has the upstream model and, where the ingredients exist, the upstream recipe. **stubbed** means the id exists but behaviour is thinner than upstream. **missing** means the id is not registered. **blocked** means the next step needs a subsystem that is not ported yet.

## Decisions

**meatlib** is not compiled and is not a mod id. It is the Fabric registry, transfer, screen, and mixin layer every machine is written against (310 files). Rewriting that against `DeferredRegister`, NeoForge fluid and item handlers, and menus is the gate for phase 2. Shipping a stub `meatlib` mod would claim the id without the behaviour.

**NEEPSpeak** is inlined. It is plain Java (TTS parser and synthesiser), LGPL-3.0, and it compiled and passed its upstream pattern unit test plus an in-game game test that calls `RuleParser.parsePattern`. It is not hooked to the automatic mouth, because that block is not ported. Class init does not read the wav files; those sit on the classpath under `phonemes/normal/` for a later block.

**Myron** is dropped. It is a Fabric OBJ model loader with mixins and a namespace filter. Machines that need OBJ models are blocked on a json model, a GeckoLib model, or a NeoForge OBJ loader. MIT would have allowed copying it. The mixins would not load.

**Flywheel** is not bundled. See `COMPATIBILITY.md`. Instanced machine renderers are missing, not replaced.

**Cardinal Components** (chunk, entity, item, world attachments in `fabric.mod.json`) become NeoForge data attachments when those systems are ported. None are registered now.

**Fabric transfer** (fluid, item) becomes NeoForge capabilities. No pipe or tank is registered, so no capability is registered.

**NBT item data** (meatgun modules, implants, filters) becomes data components. No such item is registered, so no component is registered.

## Ported

| System | State | Notes |
| --- | --- | --- |
| Mod bootstrap `neepmeat` | ported | NeoForge 21.1.265, ModDevGradle 2.0.141, Java 21 |
| Creative tab `neepmeat:general` | ported | Holds the 136 blocks below. Upstream also had one general group. |
| Building-block families | ported | Full cube or pillar, plus stairs, slab, and wall when upstream `BaseBuildingBlock` made them. 136 block ids, listed in `phase1-block-ids.txt`. |
| Smooth tile, 16 dyes | ported | Shared tinted model. Block and item colours use `DyeColor.getFireworkColor()`, same call as upstream. |
| Painted corrugated asbestos, 16 dyes | ported | Pillar (`axis`) and the generated per-colour textures. No stairs. |
| Reinforced glass | ported | Cutout render type, no occlusion, adjacent faces of the same block (and the stair facing the other way) are hidden. Stairs and slab are vanilla shapes with the glass textures. |
| Block loot | ported | Each registered block drops itself. |
| Tool tags | ported | Pickaxe, axe for blood-bubble planks, stone tool for metal and iron, diamond for meat steel. |
| `#minecraft:planks` | ported | `blood_bubble_planks` only. See Atlas review. |
| Building recipes | ported | 221 recipes under `data/neepmeat/recipe/`. Upstream 1.20 paths `recipes/` were moved to 1.21 `recipe/`. Results carry both `id` and `item`, matching other 1.21.1 datapacks in this repo. |
| Language | ported | Upstream `en_us.json` and `zh_cn.json` copied whole. |
| NEEPSpeak | ported as a library | Not wired to a block. |
| JEI plugin | ported as an empty plugin | `NeepMeatJeiPlugin`. No categories. |
| EMI plugin | ported as an empty plugin | `NeepMeatEmiPlugin`. No categories. |
| GeckoLib dependency | ported as a dependency | Version logged. No animated block. |

Families: polished metal, polished metal bricks, polished metal small bricks, blue polished metal, rusty metal, rusty metal sheet, six rough concretes, yellow tiles, caution block, dirty red tiles, dirty white tiles, sandy bricks, meat steel, asbestos, asbestos tile, corrugated asbestos, asbestos shingle, bloody bricks, bloody tiles, browne block, blood bubble planks, reinforced glass, smooth tiles, painted corrugated asbestos.

## Stubbed

| System | State | Why it is thinner |
| --- | --- | --- |
| Rough concrete walls | stubbed | Upstream used meatlib wall models and a custom `ConcreteWall` voxel shape. Those models are not in this jar. Parents now point at vanilla `template_wall_*` and `wall_inventory`. Collision is a vanilla wall. |
| Reinforced glass stairs and slabs | stubbed | Upstream stairs were stock stairs. Only the full block has the face-culling override. |
| Sounds | stubbed | `NMSoundGroups.METAL` was already netherite block sounds. This phase uses `SoundType.NETHERITE_BLOCK` directly. Custom sound events are not registered. |
| Recipe book toasts | stubbed | Advancements that unlock the recipes were not copied. The recipes still match in the crafting grid. |
| NEEPSpeak playback | stubbed | Parser is live. The mouth, wav playback, and the automatic mouth block are not. |

## Missing or blocked

Grouped by upstream package. Counts are Java files, from the 1.20.1 tree, so they include screens and renderers.

| System | Files (approx.) | State | Why |
| --- | --- | --- | --- |
| meatlib registry, screens, transfer, mixins | 310 | blocked | Phase 2 prerequisite. Fabric APIs throughout. |
| Machines (`machine/`, including living machines, reactor, crusher, mixer, kiln, mincer, press, power flower, phage ray, synthesiser, surgery, charnel pump, …) | 276+ | missing | Each block class extends meatlib blocks and uses Fabric transfer, block entities, and often Flywheel or GeckoLib. |
| Item pipes, fluid pipes, blood network, tanks (`transport/`) | 238 | blocked | Fabric `FluidStorage` / `ItemStorage` and a custom graph. Needs NeoForge handlers. |
| Client screens and renderers | 195 | missing | Follows the machines. |
| PLC, NEEPASM, instructions, robot arm | 143 + 30 | blocked | Depends on block entities, menus, and networking. NEEPSpeak (the speech side) is separate and is in. |
| NeepBus (redstone-like data bus) | ~100 | missing | Own screens, cables, and components. |
| API (processing, multiblock, enlightenment, big blocks) | 110 | missing | Used by the machines above. |
| Entities (worms, keeper, hound, horror, scutter, skyworm) | 71 | blocked | GeckoLib models and Fabric entity renderers. |
| Blocks that are not plain building blocks (doors, trapdoors, scaffolds, crops, logs, leaves, sinks, fans, tracks, cake) | 59 | missing | Doors and crops need block classes, not just a cube. Assets exist upstream and were not copied, so they cannot be placed under the old ids yet. |
| Items (ingredients, tools, food, filters) | 57 | missing | Many carry NBT that has to become data components, and recipes that reference them were not copied. |
| Meatweapons (meatgun modules, drill, implants, client) | ~200 | missing | Same component and renderer problem. Two upstream recipes (`pistol`, `iron_barrel`) were dropped because they reference `meatweapons:` items. |
| REI plugin | 33 | missing | Pack uses JEI and EMI, not REI. |
| EMI plugin body | 23 | missing | The empty entrypoint is in. The recipe categories are not. |
| Thord language | 50 | missing | In-game language for the guide. Not required to boot. |
| Mixins | 92 | blocked | See `COMPATIBILITY.md`. |
| Myron OBJ models | 11 | blocked | Fabric model loader. |
| Fluids (blood and others) | 8 | blocked | NeoForge fluid types and a client render type. Not registered, so no fluid-tag crash. |
| Worldgen, structures, loot chests | resources | missing | Would fail datapack load without the features and items. |
| Guide / Patchouli-style markdown | resources | missing | Custom guide loader, not ported. |
| Valkyrian Skies integration | compile-only upstream | missing | Not required by this pack's brief. |
| Datagen | 6 | missing | Assets for this phase were copied and adjusted, not regenerated. |

## What "boots" means

`./gradlew runGameTestServer` (headless) on JDK 21:

- Mod list includes NEEPMeat 0.30.0-neoforge.1, GeckoLib 4.9.3, JEI 19.64.0.455, EMI 1.1.24+1.21.1
- Log line: `NeepMeat 0.30.0-neoforge.1 loaded with GeckoLib 4.9.3`
- `Loaded 1512 recipes` with no recipe parse error
- `All 3 required tests passed`: place `neepmeat:polished_metal`, find its recipe, run the NEEPSpeak pattern parser

`./gradlew runServer` reached `Done (0.337s)! For help, type "help"` with the same load line and no `ERROR` from NeepMeat.

`./gradlew runClient` on `DISPLAY=:1` reached LWJGL 3.3.3 and built the block atlas. The only `ERROR` was `Failed to open OpenAL device` / ALSA `cannot find card '0'`, which is this VM having no sound device. After the concrete-wall parent fix, the client log has no `Unable to load model` line. The main menu was not clicked through; the process was stopped once resources had loaded. No world was joined on the client.

Not tested: Java 25, a Prism/Pakku instance with the pack's 402 mods, Sodium/Iris together with this jar, crafting in a GUI, or multiplayer with the jar in `.pakku/overrides/mods`. The jar was not added there.
