# Port status

Upstream is about 1,889 Java files in NeepMeat, 310 in meatlib, 38 in NEEPSpeak, and 11 in Myron, plus 4,244 resource files. A line-by-line Fabric 1.20.1 Yarn port does not compile on NeoForge 1.21.1: registries, transfer APIs, menus, networking, data components, and rendering all moved.

## Phase 2 milestone: meatlib

meatlib is a second mod id in the same jar (`meatlib` 0.30.0-neoforge.2). It is inlined, not a Gradle subproject, so machines can import `com.neep.meatlib` without another build. NeoForge 1.21 freezes the block registry outside `RegisterEvent`, and `new Block()` writes an intrusive holder, so registration is `DeferredRegister` suppliers. Upstream's "construct, then register the instance" pattern throws `Registry is already frozen`.

### meatlib now covers

| Area | State | What it does |
| --- | --- | --- |
| Mod init | ported | `@Mod("meatlib")`, engineering formatter, datapack-sync log |
| Registries | ported | `RegistrationContext` with `DeferredRegister` blocks and items. `BaseBlock.register` and `BaseHorFacingBlock.register` build the block in the block event and the item in the item event. `meatlib:probe` is a no-item stone cube that proves the path. It is not a gameplay block. |
| Block settings | ported | `MeatlibBlockSettings` unwraps to `BlockBehaviour.Properties`. Tag names are recorded for later datagen. They are not injected yet. |
| Items and tooltips | ported | `BaseItem`, `BaseBlockItem`, `TooltipSupplier`. Tooltips apply from `ItemTooltipEvent`, not a mixin. |
| Inventories | ported | `ImplementedInventory` on `Container`, saving item stacks with 1.21 `ItemStack.save(HolderLookup)`. |
| Block entities | ported | `SyncableBlockEntity` splits save NBT from the client update tag and sends `ClientboundBlockEntityDataPacket`. No block entity type is registered until a machine exists. |
| Fluids and items capabilities | ported | `MultiFluidBuffer` is a multi-tank `IFluidHandler` in milli-buckets. `MeatlibCapabilities.itemHandler` / `fluidHandler` register block-entity capabilities. Nothing calls them yet. |
| Payloads | ported | `ChannelFormat` dynamic proxy and `ParamCodec` (int, long, float, double, boolean, string, uuid, nbt) ride on one NeoForge payload, `meatlib:channel`. |
| Data components | ported | `meatlib:unique_id` (`DataComponentType<UUID>`), the old Cardinal Components item id. |
| Attachments | ported | `meatlib:unique_id` entity attachment, new UUID on first read, copied on death. |
| Menus | ported as a registry | `MeatlibMenus` is on the mod bus. No menu is registered. Client logs `meatlib client screen hook ready`. |
| Sounds | ported | `ui_beep`, `plc_select`, `click`, `wrench_click`, with the ogg files and subtitle keys. |
| Wrench | ported | `Wrenchable` rotates horizontal facing or facing. `PlayerInteractEvent.RightClickBlock` runs it when the held item is in `#meatlib:wrenches` or `#c:tools/wrenches`. The meatlib tag is empty. |
| Screens / auto layout | missing | The upstream widget kit (GUIUtil, BetterHandledScreen, panels, config widgets) is not ported. Menus can be registered. The screens that draw them are the next client slice. |
| Flywheel `client/flw` | blocked | Not compiled. When a machine renderer needs it, depend `compileOnly` on the Flywheel Create 6.0.10 bundles, and do not jar it. |
| Mixins | not ported | Sync, tooltips, and wrench use events and block-entity methods. Spawn helper, enchantment helper, and client accessors are still open. |
| Larkdown, datagen, Valkyrian Skies, fake player | missing | Not required to boot. |
| Myron | replaced later | NeoForge's built-in OBJ loader (`ObjLoader` / model json `"loader": "neoforge:obj"`) is the planned replacement. No OBJ model is loaded in this milestone. |

Boot evidence is below. Machines, pipes, the PLC, weapons, fluids-as-content, and entities are still not registered.

### Plan for the rest

1. Machines on `BaseBlock` / `BaseHorFacingBlock`, `SyncableBlockEntity`, `ImplementedInventory`, and `MeatlibCapabilities`. One machine end to end (block, block entity, menu, JEI category) before the rest.
2. Pipes on `MultiFluidBuffer` and item handlers. Blood as a NeoForge fluid type, milli-buckets, not Fabric droplets. `MiscUtil.dropletsToMb` divides by 81 only for old Fabric numbers.
3. PLC on `ChannelFormat` and NEEPSpeak, which is already in the jar.
4. Weapons and entities after that. Item NBT becomes `MeatlibComponents` or new component types. GeckoLib for the animated ones.
5. OBJ models through NeoForge's loader, not Myron.
6. JEI categories first, then the same recipes in EMI.
7. Flywheel only as `compileOnly` against the jar Create 6.0.10 already requires. No bundling.
8. Atlas still owns recipe changes. `blood_bubble_planks` stays in `#minecraft:planks`.

## Phase 2b continued: materials and the real pipe meshes

Jar `0.30.0-neoforge.4`.

| System | State | Notes |
| --- | --- | --- |
| Crafting materials | ported | The 35 `BaseCraftingItem` entries from `NMItems` that are not already blocks: refractory brick, meat steel, whisper brass, raw and cooked meat brick, organs, coils, asbestos dust, and the rest. Food stats for meat scrap, meat bricks, blood bubble, and asbestos dust match `NMFoodComponents`. Tools, weapons, implants, and enlightenment foods are separate items and are not registered. |
| Recipes | partial | Vanilla crafting, smelting, smoking, and stonecutting recipes whose items all exist are copied, plus all three upstream `alloy_smelting` recipes. The kiln craft is the upstream pattern (sandy bricks, blast furnace, refractory bricks). Recipes that need an unregistered block (data cable, machine block, tanks, ejectors, and so on) are left out, because a missing item fails the datapack. Machine recipe types other than alloy smelting have no serializer yet. |
| Alloy kiln | ported, screen is not upstream | Same behaviour as the previous checkpoint. The screen is still a flat menu, not the meatlib widget kit. |
| Item pipe | partial real port | Multipart model, arm shapes, and connection properties are the upstream pipe. `ConnectionType` still serialises as `true` / `false`. An inserted stack crosses at speed `0.1` (ten ticks) and round-robins into another pipe or an item handler. The pipe does not pull by itself. Filters, merge pipes, drivers, the route list, and `PipeVertex` are not ported. |
| Fluid pipe | partial real port | Upstream rusty-pipe multipart model, except the unconditional part `rusty_pipe/pipe_sides`, which the upstream git tree does not contain (no json anywhere in the checkout). One packet of 125 mB, the upstream `basic_pipe_throughput` of 10125 fabric droplets. Pumps, filters, windows, coloured pipes, and the fluid node graph are not ported. Nothing enters unless `insert` is called. |
| Fluid tank | visual stub | Still the 8000 mB cube. Not an upstream tank. |
| Program board | removed | It was not an upstream block. |
| PLC, robot arm, NEEPASM | missing | The real blocks are `plc`, `robotic_arm`, `executor`, `program_cabinet`, processor, builder, implanter, and the redstone interface. The instruction package is 68 classes plus the compiler and its screen, on Fabric transfer and the unported screen kit. |
| Screen widget kit | missing | |
| Other machines, blood fluid, weapons, entities | missing | |
| Mixins | none added | |
| Flywheel / Myron | not used | |

## Phase 2b checkpoint: one machine, pipes, a program board

Jar `0.30.0-neoforge.3`. This is a working slice, not the rest of the mod.

| System | State | Notes |
| --- | --- | --- |
| Alloy kiln | ported | Facing + lit, four slots (fuel, two inputs, output), vanilla fuel burn time, menu and a plain screen. Recipe type `neepmeat:alloy_smelting`. The only shipped recipe is upstream `brick_from_asbestos` (asbestos + sand → bricks) because the other two need unported items. |
| Item pipe | ported, visual stub | Pulls one item from the block behind `facing` and pushes it forward, one stack buffer, every 5 ticks. Cube model. Upstream connected meshes, filters, and routing are not ported. |
| Fluid pipe | ported, visual stub | Pulls up to 250 mB from behind and pushes forward. Cube model. No valves, pumps, or blood network. |
| Fluid tank | ported, visual stub | 8000 mB `FluidTank`, bucket interaction. Cube model. Not the upstream multi-tank. |
| Program board | stubbed | Stores one string. Right-click with an item copies that item's hover name; otherwise it prints the stored text. No assembler, robot arm, NEEPASM execution, or interrupts. |
| JEI | ported for alloy smelting | Category `neepmeat:alloy_smelting`. Recipes are read from the client recipe manager when a world or connection exists. |
| EMI | ported for alloy smelting | Same recipes and the kiln as the workstation. Client only. |
| Other machines | missing | Grinder, mixer, crusher, living machines, reactor, motor, heater, and the rest. |
| Other pipes | missing | Chutes, routers, filters, vascular conduits, blood network. |
| PLC executor | blocked | Needs the instruction set, robot arm, and screen kit. The board only stores text. NEEPSpeak is still in the jar and unused by the board. |
| Weapons, entities, blood fluid | missing | No meatgun, no mobs, no `neepmeat:blood` fluid. Tanks hold any NeoForge fluid, tested with water. |
| Screen widget kit | missing | The kiln screen is a flat `AbstractContainerScreen`, not GUIUtil. |
| Mixins | none added | |
| Flywheel / Myron | not used | No renderer needs them yet. |

## Phase 1 (still true)

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
| Mod bootstrap `neepmeat` and `meatlib` | ported | NeoForge 21.1.265, ModDevGradle 2.0.141, Java 21. Jar version `0.30.0-neoforge.2`. |
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
| meatlib core (registries, caps, payloads, inventories, block entities, sounds) | see milestone above | ported | Screen widget kit, Flywheel helpers, larkdown, and mixins are not in this jar. |
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

`./gradlew runGameTestServer` on JDK 21, jar `0.30.0-neoforge.4`:

```
meatlib 0.30.0-neoforge.4 loaded
NeepMeat 0.30.0-neoforge.4 loaded with GeckoLib 4.9.3
All 10 required tests passed :)
BUILD SUCCESSFUL
```

The ten tests are the earlier six plus the alloy kiln (asbestos + sand + coal → bricks), the three alloy recipes and the kiln crafting recipe loading, an item pushed into an item pipe arriving in the chest after the ten-tick crossing, and 125 mB pushed through a fluid pipe.

`./gradlew runClient` on `DISPLAY=:1` reached `Backend library: LWJGL version 3.3.3+5` and `meatlib client screen hook ready`. After `pipe_sides` was dropped from the fluid-pipe blockstate, the only `ERROR` was `Error starting SoundSystem` because this VM has no sound card. The main menu was not clicked through. A dedicated server boot was not repeated on this jar; the game-test server is the dedicated boot that ran the tests.

Not tested: Java 25, a Prism/Pakku instance with the pack's 402 mods, Sodium/Iris together with this jar, crafting in a GUI, or multiplayer with the jar in `.pakku/overrides/mods`. The jar was not added there.
