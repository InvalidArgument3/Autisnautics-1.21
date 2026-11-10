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

## Version and Yarn remap

Jar `0.30.0-neoforge.8`. `./gradlew runGameTestServer` on that string:

```
meatlib 0.30.0-neoforge.8 loaded
NeepMeat 0.30.0-neoforge.8 loaded with GeckoLib 4.9.3
All 22 required tests passed :)
BUILD SUCCESSFUL
```

The previous jar, `0.30.0-neoforge.7`, passed the same 22 tests before the screen kit.

`tools/yarn-to-mojmap/` builds a Yarn 1.20.1 to Mojmap 1.20.1 map (6,913 classes, 17,006 unambiguous method renames) from Yarn tiny, Fabric intermediary, and the 1.20.1 official client mappings, then rewrites sources. The whole upstream tree was rewritten into a scratch directory. The meatlib screen kit from that rewrite is now in the jar.

## Screen kit, REI dropped

| System | State | Notes |
| --- | --- | --- |
| Widget kit | ported | Panels, text fields, buttons, config widgets, slot blocks, `BetterHandledScreen`, and `GUIUtil`. 1.21 moves: component event types, four-argument `mouseScrolled`, `narrationPriority` / `updateNarration`, `GuiGraphics.blit` / `drawString`, and `BufferBuilder.addVertex` / `build`. |
| Slot hit testing | ported | `AbstractContainerScreen.findSlot` is private in 1.21. One access transformer makes it public so the widget screen can override it. No mixin. |
| Quick-craft ghost stack | partial | The 1.20 touch-drag fields and `recalculateQuickCraftRemaining` are private. The slot widget previews a split of the carried stack. The click itself is still vanilla. |
| REI | dropped | `REIScreenSupport` called Architectury `ClientGuiEvent`. The pack ships JEI and EMI only, and does not ship Architectury. The file is not in this jar. |
| EMI in widget screens | ported | `EMIScreenSupport` draws EMI's screen manager on the foreground listener, using EMI's public `EmiApi` plus the runtime classes already in the pack's EMI jar (`EmiDrawContext`, `EmiScreenManager`). |
| PLC screens | missing | `PLCProgramScreen` and the other PLC screens import the PLC block entity, the compiler, and NEEPASM. They land with those systems. The widgets they extend are in. |

## Robot arm

| System | State | Notes |
| --- | --- | --- |
| Block and entity | ported | `robotic_arm`. Tip steps toward the target at a speed clamped from input power, and stops at 4.5 blocks from the shoulder. Below 0.05 power, `setTarget` throws `NeepASM.RuntimeException`. The held stack is one `ItemStack` (upstream's Fabric `ResourceAmount<ItemVariant>`). |
| Renderer | ported | The two-bone solve is the upstream one. The base is the block model. The spinny bit and both segments are the upstream JSON models, registered as extra models and drawn by the block-entity renderer. Upstream's Flywheel visual is not compiled. Flywheel stays off the compile classpath until a renderer actually needs the Create copy. |
| Recipe | ported | `contractile_actuator`, `control_unit`, and `meat_steel_ingot` were already registered. The shaped craft is the upstream pattern. The control unit's own craft still needs `enlightened_brain`, and the actuator is a manufacture recipe. |
| Game test | ported | An unpowered arm refuses a target. A powered arm moves toward one. The held cobblestone survives save and load. 23 tests passed on `0.30.0-neoforge.8`. |

## NEEPASM

| System | State | Notes |
| --- | --- | --- |
| Data types | ported | `DataVariant`, `Argument`, `MemoryEntry`, `MemoryType`, `ThordData`, and `WorldTargetImpl`. Fabric `PlayerLookup` in `PLC.say` is a nearby-player scan on the server level. |
| VM and stack | ported | `VM`, `DataStack`, and `VariableStack`. A direct stepper in the game test runs a compiled program. The processor block's own loop arrives with the PLC. |
| Parser and compiler | ported | `NeepAsmParser`, `NeepAsmPLCCompiler`, and `PLCProgramImpl`. |
| Instruction registry | partial | Stack, arithmetic, comparison, call, return, and jumps (`push`, `add`, `jmp`, `bit`, `bif`, and the rest of that group). Item, fluid, redstone, block, and memory opcodes are not registered yet. `takeItem` and `takeFluid` still use Fabric transfer and move to NeoForge handlers with those opcodes. |
| Held stacks | ported shape | `PlcInstruction.writeItem` and `writeFluid` store an item id and count, or a fluid id and amount. That replaces `ResourceAmount<ItemVariant>`. |
| Game test | ported | `push 2` / `push 3` / `add` compiles and leaves 5 on the stack. 24 tests passed on `0.30.0-neoforge.8`. |

## Opcodes, jar 0.30.0-neoforge.9

`./gradlew runGameTestServer` loaded `0.30.0-neoforge.9` and passed all 28 tests.

| System | State | Notes |
| --- | --- | --- |
| Memory | ported | `store`, `fetch`, `allot`, `free`, `fill`, and `PLCMemory`. A program allots one cell, stores 42, and fetches it. |
| Item | ported | `move` extracts and inserts through `IItemHandler`. `takeItem` and `giveItem` replace Fabric item transfer. The arm carries one cobblestone from one chest to another. |
| Fluid | ported | `inject` takes one bucket (1000 mB) through `IFluidHandler`, moves the arm, and returns the fluid when the destination is not a workpiece. Workpiece mutation lands with manufacture. |
| Redstone | ported | `redstone_interface`, `rread`, and `remit`. The block emits the stored strength and records the best neighbour signal. |
| Block | partial | `moveto` aims the actuator. `bplace` and `bbreak` need the builder entity, which arrives with the assembler. |

## PLC, executor, and builder, jar 0.30.0-neoforge.10

`./gradlew runGameTestServer` loaded `0.30.0-neoforge.10` and passed all 30 tests.

| System | State | Notes |
| --- | --- | --- |
| PLC | ported | `plc` stores source, compiles it, and runs it against the actuator in front. Right-click opens a screen with the source field and Run and Stop. The Fabric shell, editor, and projector HUD are not in this screen. |
| Executor | ported | `executor` accepts instructions through `PLCExecutor` and runs them against the actuator in front. A test pushes 4 onto its stack. |
| Builder | ported | `builder` holds 27 slots, moves its tip, places the selected block item, and breaks blocks up to obsidian hardness into its inventory. `bplace` and `bbreak` use it. A test places cobblestone and breaks dirt. |
| Screens | partial | `PLCScreen` edits and runs the stored source. The upstream 16-file shell is not copied. |

## Motor, jar 0.30.0-neoforge.11

`./gradlew runGameTestServer` loaded `0.30.0-neoforge.11` and passed all 31 tests.

| System | State | Notes |
| --- | --- | --- |
| Motor | ported | `motor_unit` faces a `MotorisedBlock` and calls `motorTick(MotorEntity)` and `setInputPower(1)`. Fuel extraction from `LiquidFuelMachine` is not in this block, so the output is a constant one mechanical unit. A test powers the arm from the motor in front. |

## Manufacture, jar 0.30.0-neoforge.12

`./gradlew runGameTestServer` loaded `0.30.0-neoforge.12` and passed all 33 tests.

| System | State | Notes |
| --- | --- | --- |
| Serializer | ported | `neepmeat:manufacture` loads a base item, ordered steps, and a result. Shipped recipes are `contractile_actuator` (bone + internal components), `pipe_driver` (motor unit + control unit), and obsidian (cobblestone + lava + water). Recipes whose results are not registered are left out. |
| Workpiece | ported | The `neepmeat:workpiece` component stores the steps on the stack. `ManufactureRecipe.apply` appends a step and replaces the stack when a recipe matches. |
| MutateInPlace | ported for containers | `ITEM.find` wraps the first occupied slot of a container. A chest of bone plus internal components becomes a contractile actuator. Entity mutation still finds nothing. |
| enlightened_brain | ported | Food: 6 hunger, 0.6 saturation, Night Vision for 30 seconds. The shaped control-unit recipe uses it. The `neepmeat:enlightening` recipe type is not registered. |
| JEI and EMI | ported | Both show the manufacture category with the builder as the workstation. |

## Fuel motor, jar 0.30.0-neoforge.13

`./gradlew runGameTestServer` loaded `0.30.0-neoforge.13` and passed all 33 tests.

The motor burns ethereal fuel. Upstream stores two energy units per Fabric droplet. A millibucket is 81 droplets, so the registry stores 162 energy per millibucket. Each tick asks for 400 absolute units, drains 2 millibuckets, and gets 324 back, which is 0.324 per unit. Rotor speed is that power in watts divided by the load torque of 300. An empty motor supplies nothing. The tank holds 8000 millibuckets so a bucket fits. Upstream's buffer was 200 droplets.

## Enlightening and entity mutation, jar 0.30.0-neoforge.14

`./gradlew runGameTestServer` loaded `0.30.0-neoforge.14` and passed all 35 tests.

The integrator holds one item and a data counter. With 1024 data, a rough brain becomes an enlightened brain. Seven enlightening recipes load. Four are left out because their results are not registered: blood bubble sapling, flesh potato, grave brome, and whisper wheat seeds.

`ENTITY.find` returns the surgery platform's target. That target is the non-spectator, non-minecart entity standing on the block above. A pig spawned there is the entity the lookup returns.

## Motor buffer, jar 0.30.0-neoforge.15

The fuel buffer is 200 droplets again, which is `basicMotorMaxPower / dropletEnergy` (400 / 2). A full buffer burns in one tick and delivers 0.4 per unit. The energy registry stores energy per droplet, matching upstream.

NeoForge fluid handlers move whole millibuckets, and one millibucket is 81 droplets. A bucket fill therefore stores 162 droplets and leaves 38 droplets of capacity unused. The internal buffer is still 200. That is the trade-off, not a larger tank.

## Living machine graph, jar 0.30.0-neoforge.16

A controller, one machine block, and a motor port form a machine. Max power averages to 300. Mass starts at the property default of 1 and adds 100, so the result is 101. A second controller in the flood fill clears the result, matching upstream. The search stops at 128 blocks.

Component type ids follow upstream order. The accessor is `componentType()` because Mojmap `BlockEntity.getType()` already exists.

Registered so far: `living_machine_controller`, `machine_block`, `motor_port`. The other structure variants, ports, processes, degradation, and the machine worm are not ported.

## Metaboliser and machine worm, jar 0.30.0-neoforge.18

A controller, a machine block, a stomach, and one metaboliser segment select the metaboliser process. Four droplets of `neepmeat:meat` become 240 energy on the segment: 40 energy per droplet, times 1.5. More than six segments would produce nothing. The degradation formula matches upstream. A controller loaded at full degradation is replaced by a `machine_worm`.

## Big blocks, jar 0.30.0-neoforge.20

`BigBlock2` places the upright stomach cylinder (radius 1, one below to one above) and the metaboliser disc (radius 1). Breaking a structure block removes the origin and the rest of the volume. The metaboliser's four cardinal faces delegate blood to the origin. 43 game tests passed.

Deviations: only the upright volumes are placed. Upstream also has X and Z orientations. The outline shape is a normal cube, not the union of the volume. Structure ids are `stomach_structure` and `metaboliser_segment_structure`.

The worm's three segments are not separate entities.

## Transfer amounts, jar 0.30.0-neoforge.21

`TransferAmounts` divides Fabric droplets by 81 to get millibuckets and drops the remainder. 200 droplets are 2 millibuckets with 38 droplets left over. Multiplying those 2 millibuckets back gives 162 droplets. Simulate and execute call `IFluidHandler.FluidAction` and `IItemHandler.extractItem`. The remaining living-machine classes still name the Fabric transfer types directly. They are not rewritten onto this adapter yet. Hurt and death sounds are the iron golem and the generic explosion, because the upstream sound events are not registered. The other living-machine processes are not registered.

## Blood network, jar 0.30.0-neoforge.19

A metaboliser segment emitting 1000 absolute units feeds a lucky one through two conduits. The lucky one stores 0.05 per unit, which is the upstream 50 eJ/t threshold, and consumes that amount. Each conduit face is `SIDE` or `NONE`. Breaking the middle conduit leaves the two ends on different networks. `onChunkUnloaded` drops the conduit from the network without splitting it. Balancers run before sinks, highest priority first. A balancer that returns the negation of the internal total leaves the lucky one at zero.

`test_blood_source` and `test_blood_sink` are no longer registered.

Deviations: the voltage-sensor block that owns the upstream balancer is not registered. The balancer test attaches an acceptor through `include`, which is the same list discovery uses. The conduit connection is a boolean face, not the full precise-pipe auto-connect rules. Chunk reload does not rejoin a conduit by itself.

## Deviations

- The motor buffer is 200 droplets. A bucket fill stores 162, because NeoForge moves whole millibuckets and one millibucket is 81 droplets. The unused 38 droplets of capacity stay empty.
- Living-machine component lookup is `componentType()`. Mojmap `BlockEntity.getType()` already returns the block entity type.
- The flood fill, the 128 block cap, the second-controller clear, and the property math are upstream. Only the controller, the machine block, and the motor port are registered. The other 66 living-machine classes, processes, degradation, and the machine worm are not.
- Blood flow uses the upstream split: sources are summed, divided by 1000, active sinks take first, and passive sinks share the rest. Balancers, chunk unload, and splitting a network when a conduit is removed are not implemented. The conduit connects on a face when the neighbour is a conduit or an acceptor. It does not use the upstream precise-pipe connection property. `test_blood_source` and `test_blood_sink` are the test ends, not upstream machines.

`BuilderRobot`, the full PLC shell, the remaining machines, and the meatgun are still out.

## This run: requests finished, kiln screen, basic tank, blood

Jar still `0.30.0-neoforge.6` plus the commits after that version string. The game-test server that included blood reported 22 passing tests.

| System | State | Notes |
| --- | --- | --- |
| Crafting requests | ported | A network crafter whose result is missing from storage pulls each ingredient with a nested request. Depth stops at 30, matching `RoutingNetworkImpl`. |
| Deferred callbacks | ported | The driver keeps the request. SUCCESS when the destination holds the stack, PASS while a pipe is carrying it, FAIL after the wait with nothing moving. |
| Kiln screen | ported | `GUIUtil.drawTexture` blits `textures/gui/alloy_kiln.png`, including the burn flame and the progress arrow. The rest of the 66-file widget kit is not ported. |
| Basic tank | ported | `basic_tank`, upstream model, axis, 8-bucket (8000 mB) capacity, bucket click. The older `fluid_tank` cube remains for the earlier pipe tests. |
| Blood | ported | Source, flowing fluid, liquid block, bucket. It does not make itself from flowing fluid, and it ticks every 5 ticks. Tag `neepmeat:blood`. The basic tank holds it. The blood network, vascular conduits, and the integrator's blood check are not ported. |
| Remaining machines | missing | Blocked on the size of the machine tree (living machines alone are 69 classes) and Fabric transfer, screens, and world lookups those classes call. |
| PLC, robot arm, executor, assembler | missing | Blocked. The instruction package is 4,853 lines. `Instruction.start` takes the NEEPASM `VM`, and most opcodes read the world or the robot arm. The parser also depends on that instruction registry. Porting them as Yarn sources does not compile. |
| Weapons and entities | missing | Blocked on the same world and GeckoLib entity classes (meatgun, implants, mobs). |

## Phase 2b continued: requests, priority routes, filter screen, pump buffer

Jar `0.30.0-neoforge.6`.

| System | State | Notes |
| --- | --- | --- |
| Route search | ported | Priority queue. Entering a filter pipe costs one less than a plain pipe, so filters are expanded first. The first handler that accepts the stack under simulate is the route. |
| Requests | partial | A storage bus exposes the inventory it touches. An item requester asks for one item every ten ticks. `RoutingNetwork.request` simulates the extract, builds the route, then extracts and inserts. Crafting requests, recursion through other requesters, and the deferred `RoutablePipe` callbacks from `RoutingNetworkImpl` are not ported. |
| Filter entries | ported | Item entries, tag entries, and the inverted flag. An empty list still matches everything. Any one entry may match. The screen has three slots, Invert, and Tag (the item's first item tag). |
| Pump | ported for modes and buffer | Active when redstone and `activeWithoutRedstone` differ. Right-click toggles that flag. While active, the back mode is PULL and the front mode is PUSH. Fluid moves through a 1000 mB buffer at 125 mB per ten ticks. |
| Screen widget kit, upstream tank, blood, other machines, PLC, weapons | missing | Next. |

## Phase 2b continued: pipe graph, filters, merge, driver, pump, colours

Jar `0.30.0-neoforge.5`.

| System | State | Notes |
| --- | --- | --- |
| Pipe vertex graph | ported | `PipeVertex`, `ConnectablePipe`, `PipeConnections`, and `PipeGraph` follow upstream `PipeGraphUtil`: a pipe with two connections is hidden, and junctions link to each other across that run. |
| Item routes | partial | `ItemRoute` is the upstream direction list. `PipeRouteFinder` is a breadth-first search that stops at the first inventory `insertItem(..., true)` accepts, then the pipe consumes the route. The upstream priority-queue `PipeRouteFinder.Operation` and `RoutingNetworkImpl` (requests, storage drivers, requesters) are not ported. |
| Filter item pipe | partial | Empty filter matches every item, as `FilterList` does. Up to three item entries, matched by item identity. Tag filters, inverted entries, and the filter screen are not ported. The pipe is a vertex. |
| Merge item pipe | ported | Without a route, output is `FACING` and items cannot enter from that side. With a route, the route wins. Upstream model. |
| Pipe driver | partial | Every ten ticks it simulates an extraction, finds a route, and inserts one item. It does not host `RoutingNetworkImpl`. Blockstate `valid` matches the upstream property. |
| Pump | partial | Moves 125 mB per ten ticks from the rear handler to the front, simulate then execute. That amount is upstream `basic_pipe_throughput`. Redstone modes and the internal buffer are not ported. `active` and `facing` match the blockstate. |
| Coloured fluid pipes | ported | All sixteen `fluid_pipe_<color>` blocks. Two coloured pipes connect only when the colours match; either side may be the uncoloured pipe or a fluid handler. Item tag `neepmeat:fluid_pipes` lists them. Their multipart is the uncoloured one, because upstream's `rusty_pipe/pipe_sides` model is not in the git tree. |
| Screen widget kit, upstream tank, blood, PLC, weapons | missing | Next chunk. |

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
| meatlib core (registries, caps, payloads, inventories, block entities, sounds, screen kit) | see milestone above | ported | Flywheel helpers, larkdown, and mixins are not in this jar. REI is dropped. |
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
| REI plugin | 33 | dropped | Pack ships JEI and EMI only. `REIScreenSupport` needed Architectury `ClientGuiEvent`, which this pack does not ship. |
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

`./gradlew runGameTestServer` on JDK 21, after the blood fluid:

```
meatlib 0.30.0-neoforge.6 loaded
NeepMeat 0.30.0-neoforge.6 loaded with GeckoLib 4.9.3
All 22 required tests passed :)
BUILD SUCCESSFUL
```

The twenty-two tests add crafting recursion (cobble becomes stone and arrives at the requester), recursion that stops at depth 30, and a basic tank holding 1000 mB of blood. The version string was still `0.30.0-neoforge.6` for that run.

`./gradlew runClient` on `DISPLAY=:1` reached `Backend library: LWJGL version 3.3.3+5` and `meatlib client screen hook ready`. After `pipe_sides` was dropped from the fluid-pipe blockstate, the only `ERROR` was `Error starting SoundSystem` because this VM has no sound card. The main menu was not clicked through. A dedicated server boot was not repeated on this jar; the game-test server is the dedicated boot that ran the tests.

Not tested: Java 25, a Prism/Pakku instance with the pack's 402 mods, Sodium/Iris together with this jar, crafting in a GUI, or multiplayer with the jar in `.pakku/overrides/mods`. The jar was not added there.

## Living machines through jar 0.30.0-neoforge.24

Upstream `machine/live_machine` is 69 Java files. This jar has 22 files in that package. The ones with upstream behaviour are the controller flood fill, machine block, motor port, stomach, metaboliser segment, lucky one, item ports, crusher segment, large compressor, extractor, silky extractor, and service port, plus the metaboliser, crusher, and large-compressor processes.

Registered and tested: metaboliser (240 energy from four meat droplets), crusher (takes the input stack and adds the progress increment), compressor (400 droplets at full power). X and Z volumes exist. `vsc` removes `min(internal, desired)` from a blood network. A conduit calls `join` again from `onLoad`.

Still not copied, about 47 upstream files: the other big machines (trommel, hopper, tree vacuum, ball mill, distributor, phage ray, stomach item), their processes, and the living-machine screens.

## Transfer shim, jar 0.30.0-neoforge.25

`com.neep.meatlib.transfer` has `Storage`, `StorageView`, `Transaction`, `ItemVariant`, `FluidVariant`, `SingleVariantStorage`, and `FluidConstants` (81000 droplets in a bucket). `onRename` commits, because the Yarn rewrite renamed Fabric's `commit`. Abort runs the snapshot callbacks. A pipe fill of 1 millibucket stores 81 droplets. The game test aborts 200 droplets back to zero, then commits them.

The large compressor inserts 400 droplets of `compressed_air` through that storage. It is still one block, not the upstream multiblock. The crusher turns cobblestone into gravel after a second tick. That is one hardcoded result, not the advanced-crushing recipe map. `vsc` still has no screen. The 47 uncopied classes do not import this shim yet. The package still has 22 files against upstream's 69.


The grouped unresolved-symbol list from compiling the 59 not-yet-ported living-machine files is `docs/LIVING_MACHINE_UNRESOLVED.md`. Seven of those files compiled after the transfer import rewrite and are now in the tree. 52 did not.

## Distributor, jar 0.30.0-neoforge.38

`DistributorPointBlock` uses the upstream collision boxes and a radius-1 disc. `busWrite("Send")` sets `sendNext`. `busWrite("Set channel")` replaces the address. The receiver worm spawns only when `hasNearbyAlivePlayer` is true within 60 blocks. `DistributorPointProcess` extracts a parcel from the item and fluid inputs, locks a route, and inserts a received parcel into the outputs, dropping leftover items at the dump position. `SkywormEntity.spawnFromAltar` requires a player and a ring of `neepmeat:worm_altar`. The centre lava is cleared to air because `contaminated_rubble` is not registered. Data-cable connection properties are not on the structure block.

## Registered shells, jar 0.30.0-neoforge.37

`block/StomachBlock`, `block/MetaboliserSegmentBlock`, `block/entity/LivingMachineControllerBlockEntity`, and `block/entity/MotorPortBlockEntity` are the classes that are registered. The previous parent copies are gone. `LivingMachines.CRUSHER` is `CrusherSegmentBlock`, a radius-1 disc. The largest hopper is that same disc and `strike` keeps a killed mob's drops. The phage beam is an end-rod particle line, and silk touch is applied to the harvest tool when a silky extractor is present. The tree vacuum sends `TreeVacuumAnimationPacket`, which sets `animationTicks` to 10 on the client.

The five former shells are no longer wrappers. Nineteen files are still partial. `docs/PORT_STATUS.md` below still has the 0.36 table; the shell rows in that table are stale and are replaced by this section.

## Shell effects, jar 0.30.0-neoforge.36

The phage ray breaks the block its beam points at and puts the drops in the item output. The charnel pump consumes compressed air and moves one bottle of work fluid into a well head. The disruptor writes its disruption into the reaction core, and the core's organisation falls by `disruption * 5`. The largest hopper swallows an item entity. The tree vacuum breaks the log two blocks in front of it and collects the drop. The integration port accepts one ingot of work fluid from an item and closes when the machine has power. The bioreactor turns 1000 droplets each of blood, meat, and work fluid into 3000 droplets of work fluid. The digester turns one raw iron and one ingot of blood into one ingot of meat.

`shell` means the upstream path only forwards to another class, or the block is not the one that is registered. `partial` means the effect works and a piece of the Loom file is still absent. `faithful` means the file does the upstream job this jar relies on.

| File | Ours / Loom | Status |
| --- | --- | --- |
| `GroundInfuserBlock.java` | 70 / 70 | faithful |
| `LargeCompressorBlock.java` | 104 / 204 | partial |
| `LivingMachineComponents.java` | 54 / 110 | faithful |
| `LivingMachines.java` | 189 / 241 | faithful |
| `Processes.java` | 75 / 91 | faithful |
| `block/BallMillBlock.java` | 61 / 241 | partial |
| `block/CrusherSegmentBlock.java` | 41 / 104 | faithful |
| `block/DistributorPointBlock.java` | 59 / 283 | partial |
| `block/ExtractorBlock.java` | 38 / 38 | faithful |
| `block/FluidPortBlock.java` | 82 / 77 | faithful |
| `block/IntegrationPortBlock.java` | 56 / 62 | faithful |
| `block/ItemInputPortBlock.java` | 48 / 50 | faithful |
| `block/ItemOutputPortBlock.java` | 43 / 43 | faithful |
| `block/ItemPortBlockItem.java` | 54 / 54 | faithful |
| `block/LargeTrommelBlock.java` | 92 / 165 | partial |
| `block/LargestHopperBlock.java` | 62 / 156 | partial |
| `block/LuckyOneBlock.java` | 98 / 98 | faithful |
| `block/MetaboliserSegmentBlock.java` | 13 / 190 | faithful |
| `block/PortBlock.java` | 78 / 88 | faithful |
| `block/ReprocessorBlock.java` | 73 / 88 | faithful |
| `block/ServicePortBlock.java` | 63 / 69 | faithful |
| `block/SilkyExtractorBlock.java` | 38 / 38 | faithful |
| `block/StomachBlock.java` | 13 / 225 | faithful |
| `block/TestLivingMachineBlock.java` | 42 / 121 | partial |
| `block/TreeVacuumBlock.java` | 65 / 177 | partial |
| `block/entity/BallMillBlockEntity.java` | 85 / 180 | partial |
| `block/entity/CrusherSegmentBlockEntity.java` | 84 / 273 | partial |
| `block/entity/DistributorPoint.java` | 32 / 50 | faithful |
| `block/entity/DistributorPointBlockEntity.java` | 172 / 310 | partial |
| `block/entity/ExtractorBlockEntity.java` | 41 / 41 | faithful |
| `block/entity/FluidInputPortBlockEntity.java` | 71 / 70 | faithful |
| `block/entity/FluidOutputPortBlockEntity.java` | 80 / 80 | faithful |
| `block/entity/IntegrationPortBlockEntity.java` | 101 / 135 | faithful |
| `block/entity/ItemInputPortBlockEntity.java` | 86 / 85 | faithful |
| `block/entity/ItemOutputPortBlockEntity.java` | 245 / 241 | faithful |
| `block/entity/LargeCompressorBlockEntity.java` | 53 / 67 | faithful |
| `block/entity/LargeTrommelBlockEntity.java` | 112 / 171 | partial |
| `block/entity/LargestHopperBlockEntity.java` | 86 / 169 | partial |
| `block/entity/LivingMachineControllerBlockEntity.java` | 19 / 112 | faithful |
| `block/entity/LuckyOneBlockEntity.java` | 56 / 117 | partial |
| `block/entity/MetaboliserSegmentBlockEntity.java` | 57 / 100 | faithful |
| `block/entity/MotorPortBlockEntity.java` | 19 / 62 | faithful |
| `block/entity/ReprocessorBlockEntity.java` | 41 / 41 | faithful |
| `block/entity/ServicePortBlockEntity.java` | 41 / 41 | faithful |
| `block/entity/SilkyExtractorBlockEntity.java` | 41 / 41 | faithful |
| `block/entity/StomachBlockEntity.java` | 95 / 141 | partial |
| `block/entity/StorageProvider.java` | 22 / 22 | faithful |
| `block/entity/TreeVacuumBlockEntity.java` | 42 / 82 | partial |
| `component/ChestComponent.java` | 53 / 53 | faithful |
| `component/FluidInputComponent.java` | 26 / 26 | faithful |
| `component/FluidOutputComponent.java` | 26 / 26 | faithful |
| `component/HopperComponent.java` | 53 / 53 | faithful |
| `component/ItemInputComponent.java` | 23 / 23 | faithful |
| `component/ItemOutputComponent.java` | 25 / 24 | faithful |
| `component/MetaboliserSegmentComponent.java` | 20 / 20 | faithful |
| `component/PoweredComponent.java` | 37 / 37 | faithful |
| `item/StomachBlockItem.java` | 23 / 44 | partial |
| `process/BallMillProcess.java` | 46 / 108 | partial |
| `process/BioreactorProcess.java` | 60 / 77 | faithful |
| `process/CharnelPumpProcess.java` | 63 / 103 | faithful |
| `process/CrusherProcess.java` | 95 / 169 | partial |
| `process/DisruptorProcess.java` | 109 / 120 | faithful |
| `process/DistributorPointProcess.java` | 45 / 195 | partial |
| `process/LargeCompressorProcess.java` | 51 / 66 | faithful |
| `process/LargeTrommelProcess.java` | 76 / 129 | faithful |
| `process/MetaboliserProcess.java` | 71 / 120 | faithful |
| `process/PhageRayProcess.java` | 65 / 119 | faithful |
| `process/PressurisedDigesterProcess.java` | 80 / 113 | faithful |
| `process/TreeVacuumProcess.java` | 129 / 227 | faithful |
After 0.30.0-neoforge.37 the five shell rows above are the registered implementations, so the count is 50 faithful, 19 partial, 0 shell. The line counts in this table were measured on 0.36.

Deviations in this batch: the phage beam does not render and silk touch is not applied. The charnel pump does not spawn spouts or grant enlightenment. The reaction core does not flood-fill a receiver organism. The digester does not build ore-fat fluids from smelting recipes; raw iron and blood become meat, and compressed air still halves the blood cost. The largest hopper does not damage mobs and is not the upstream cylinder. The tree vacuum has no client animation.

## Recipes, multiblocks, and distributor, jar 0.30.0-neoforge.35

`machine/live_machine` matches all 69 upstream paths. The package has more files than that because of the port's tests and helpers. `./gradlew runGameTestServer` passed 62 tests on this jar.

The eight recipes that were waiting on items are in `data/neepmeat/recipe/`. `active_waste` is a solid block (upstream it falls). `whisper_wheat_seeds` is a plain item. `asbestos` is the building-block family cube that was already registered, not `RawAsbestosBlock`. Ball-mill `data.ore_item` is the `neepmeat:ore_item` data component. `ItemVariant` keeps the component patch. The ball-mill test feeds `crushed_ore` with `minecraft:raw_gold` and checks the dust carries the same component. The iron and gold grinding and advanced-crushing crushed-ore recipes keep that field too.

`large_trommel` and `large_compressor` extend `BigBlock`. `ball_mill` extends `BigBlock2`, which is what upstream does. Game tests place each volume and break the origin. Collision shapes are the block grid, not the upstream model boxes. `BigBlock` still places from `setPlacedBy`. `BigBlock2` places from `onPlace`.

`DistributorPointBlockEntity` stores the address, mode, power flag, and parcel queue. `DistributorRouting` delivers the parcel after the animation total and spawns `SkywormEntity`. A game test checks both. The routing object is still a per-level map. The worm does not require a player within 60 blocks. NeepBus inputs and the altar spawn are not on these classes. The skyworm moves through descend, hold, and ascend and then discards.

`ItemPipeUtil.EjectOperation` inserts into an adjacent container, item handler, or item pipe, or drops the stack into air. A game test auto-ejects cobblestone into the chest the port faces. Pipe filters and the memory key are not consulted. `playerHoldingPipe` is true for a `FluidPipeBlock` item. `TankMessagePacket` is a `CustomPacketPayload`; the client shows the amount on the action bar. `BlockPlacementHelper` draws the volume outline while a multiblock item is held, and it spawns break particles while the attack key is held. There is no particle mixin.

The phage ray, charnel pump, and disruptor process classes are present and claim their components. They do not harvest, move fluid, or raise instability. `fat_trommel` is still the empty non-matching recipe.

## BigBlock and worm routing, jar 0.30.0-neoforge.34

`BigBlock` places its volume from `BigBlockPattern` and clears it when the origin breaks. The reprocessor is that multiblock. A game test places the volume and breaks the origin. Structure block entities are queued during the block register event and registered on the block-entity event. `BlockPlacementPreview` does not include the client particle preview. API lookups on the pattern are string ids, which is what the metaboliser already used.

`transport.worm` has routes, parcels, locks, and `DistributorRouting`. The routing object is a per-level map rather than a Cardinal Components attachment, and it does not spawn a skyworm. A game test acquires a route lock and releases it. `DistributorPointBlockEntity` is only the mode enum so far.

`large_trommel`, `ball_mill`, and the compressor are still one block. The compressor and those two machines were not switched onto `BigBlock` in this jar.

`fat_trommel`'s upstream file is only `{"type":"neepmeat:fat_trommel"}`. The serializer is an empty recipe that does not match.

These recipe files were left out because an item they name is not registered:

- `grinding/active_waste.json` needs `neepmeat:active_waste`
- `grinding/dye/yellow_dye_whisper_wheat.json` needs `neepmeat:whisper_wheat_seeds`
- `grinding/whisper_flour.json` needs `neepmeat:whisper_wheat` and `neepmeat:whisper_flour` (both items exist; the file was skipped before that was checked)
- `grinding/meat_steel_ball.json` needs `neepmeat:meat_steel_component`, `neepmeat:meat_steel_ball`, and `neepmeat:meat_scrap` (those items exist)
- `grinding/dye/brown_dye_packed_browne.json` needs `neepmeat:packed_browne` (that item exists)
- `advanced_crushing/whisper_flour.json` needs `neepmeat:whisper_wheat` and `neepmeat:whisper_flour`
- `ball_mill/asbestos_dust.json` needs `neepmeat:asbestos`
- `ball_mill/browne_ore_dust.json` needs `neepmeat:crushed_browne_ore` and `neepmeat:browne_ore_dust` (both items exist)

## Recipe registry, jar 0.30.0-neoforge.33

`MeatlibRecipes` reads the server recipe manager. `CrushingRecipe`, `NormalTrommelRecipe`, and `BallMillRecipe` use `MapCodec` and `StreamCodec` and are registered on the recipe `DeferredRegister`. Upstream JSON for grinding, advanced crushing, trommel, and the ball mill is in `data/neepmeat/recipe/`. Eight grinding files that name items this jar does not register were left out. `ResourceSpec` is the 1.21 form of the upstream resource-or-tag amount. Ball-mill `data` components are not decoded. The `fat_trommel` file is only `{"type":"neepmeat:fat_trommel"}`, so its serializer is an empty recipe that does not match.

The crusher tries advanced crushing, then grinding. A blaze rod becomes 4 or 5 blaze powder. Cobblestone falls back to gravel. The trommel turns 9000 droplets of tissue slurry into 9000 droplets of meat. The ball mill turns `crushed_ore` into `ore_dust`. `large_trommel` and `ball_mill` are one block each until `BigBlock` is ported. `tissue_slurry` is registered so the trommel recipe resolves.

## Taller blocks, jar 0.30.0-neoforge.32

`RegistrationContext.append` registers a child block. During static init it uses `DeferredRegister.register`. During the block `RegisterEvent` the deferred register is already frozen, which was confirmed by a failed boot (`Cannot register new entries to DeferredRegister after RegisterEvent has been fired`). The second path calls `RegisterEvent.register` while the parent supplier is running. `TallerBlock` places its structure above the origin. `lucky_one` is `LuckyOneBlock` and `ground_infuser` is `GroundInfuserBlock`. A game test checks the lucky-one upper block.

`MeatlibBlockSettings.copyOf(Properties)` returns the same properties object, which is what the structure constructor mutates.

## Item and fluid ports, jar 0.30.0-neoforge.31

`ItemInputPortBlock`, `ItemOutputPortBlock`, and `FluidPortBlock` are the upstream blocks. The item input opens `ItemInputScreenHandler` through `MenuType` (`IMenuTypeExtension`). The item output is the nine-slot upstream block entity. `FluidPortBlock` fills from a bucket and calls `TankBlockEntity.showContents`. `LivingMachineBlockEntity` is the controller base and implements `ComponentHolder`. `FluidPump.of` and `AcceptorModes` back the fluid output.

Deviations: `TankBlockEntity.showContents` writes the action bar instead of `TankMessagePacket`. `ItemPipeUtil.EjectOperation` returns zero because item pipes are not ported, so auto-eject keeps the stack. `ItemOutputScreenHandler.setEject` writes the property slot directly. `playerHoldingPipe` is always false. `ScrollableItem.swapStack` copies data components. The reprocessor and compressor are still one block. The crusher still turns cobblestone into gravel, and it now inserts that stack into the nine-slot output. The trommel, ball mill, and the other  processes are not in the jar.

## Leaf living-machine types, jar 0.30.0-neoforge.30

The 2,198-file Loom tree is no longer in git. `tools/loom-migrated/regenerate.sh` fetches commit `0d36776`, runs Loom 1.9.2 `migrateMappings`, and writes `tools/loom-migrated/neepmeat` and `meatlib`, both gitignored. `apply_121_drift.py` also rewrites `use` to `useWithoutItem` and makes `onRemove`, `rotate`, and `mirror` protected.

`machine/live_machine` now has the upstream extractor, silky extractor, service port, item input port, fluid input port, and reprocessor block entities, plus `PortBlock`, `ServicePortBlock`, `ExtractorBlock`, `SilkyExtractorBlock`, `ChestComponent`, and `HopperComponent`. The registrar class is `LivingMachines`, matching upstream. `getComponentType()` and `StorageProvider.getStorage` match the Loom names.

Deviations in this batch: block entities are constructed as `(BlockPos, BlockState)` because that is what NeoForge's block entity supplier is. `PortBlock` and `ServicePortBlock` take a `(BlockPos, BlockState)` factory because `DeferredHolder` has no `create`. The block item is not created inside `BaseFacingBlock`. The reprocessor block is still one block, not the upstream volume. The fluid input block is `PortBlock` until `FluidPortBlock` can call `TankBlockEntity`. The item input block is still `ComponentMachineBlock` until the screen handler exists. The crusher reads the input through `ItemInputComponent` and still writes gravel to the one-stack output port.

## Loom tree in the repo, jar 0.30.0-neoforge.29

The Loom output is checked in at `tools/loom-migrated/`. NeepMeat is `neepmeat/com` (1,888 files) and meatlib is `meatlib/com` (310 files). That tree stays on the 1.20.1 Mojmap API. `tools/loom-migrated/apply_121_drift.py` rewrites `saveAdditional`/`loadAdditional(CompoundTag, HolderLookup.Provider)` and `ResourceLocation` factories onto a copy.

Compiling all 69 living-machine files from that tree, after the transfer import rewrite and the drift script, is 323 error lines. Before the drift script the same 69 were 334. The regex scratch tree was 1,511. The 323 left are still missing project types (`LivingMachineBlockEntity`, `BigBlock`, `init`, `transport.worm`, screen handlers), not Yarn names. Those 69 files are not in the mod jar. The package in the jar is still 29 files.

## Loom migrateMappings, jar 0.30.0-neoforge.28

Path A worked. On a scratch copy of upstream commit `0d36776` (`1.20/dev`), `./gradlew migrateMappings --mappings "net.minecraft:mappings:1.20.1"` with Loom 1.9.2 finished in about three minutes. NeepMeat sources are in `remappedSrc` (1,888 files) and meatlib in `subprojects/meatlib/remappedSrc` (310 files). All 69 living-machine files are in that output. Overrides are renamed: `newBlockEntity`, `createBlockStateDefinition`, `getBlockPos`.

Error lines compiling those 69 against the NeoForge 1.21 classpath:

| Sources | Error lines |
| --- | --- |
| Regex scratch tree, the 59 files not already in the jar | 1,511 |
| Same files after a Fabric-transfer import rewrite | 1,209 |
| Loom output, all 69 | 534 |
| Loom output plus the transfer import rewrite | 334 |

The 334 that remain are missing project types (`LivingMachineBlockEntity`, `BigBlock`, `init`, `transport.worm`, screen handlers) and 1.21 signatures (`saveAdditional`/`load` still take one `CompoundTag`). Those 69 files are not copied into this jar. The package here is still 29 files.

## Remapper, jar 0.30.0-neoforge.27

`tools/yarn-to-mojmap` now builds an owner-aware tiny file (6,918 classes, 25,747 renamed methods) and runs Mercury 0.6.0 on a Yarn-named 1.20.1 client jar. Against the old regex scratch tree, Mercury renames resolved calls such as `getPos` to `getBlockPos` and `Block.Settings` to `Properties`. Compiling the 59 files that were not in the tree produced 1,511 error lines before this. Rewriting Fabric transfer imports, without Mercury, cut that to 1,209. Mercury does not yet rename every override (`createBlockEntity` stays, while `super.appendProperties` becomes `createBlockStateDefinition`), and a full-tree run stops on an Eclipse null binding. The living-machine package is still 29 files in the jar against 69 upstream. The 52-file gap is unchanged.
