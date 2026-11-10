## Actuator, filters, drops, and the storage driver, jar 0.30.0-neoforge.66

Combined-tree errors: **2073** at the start of this run, **2045** at the end. That is 28, not 400, and not at 1,673. The PLC closure was **167** and is **164**. `exclude.txt` is **1277**. `tools/port-errors/symbols.txt` is the end histogram.

The pipe driver drops a stored stack from above the block, which is what upstream `spawnItem` does. `getStored`, `setStored`, `setTarget`, and `reachedTarget` raise "Actuator does not support action", which is the upstream default. The robotic arm still implements movement. `pipeDriverDropsItsStoredItem` checks the drop and that `setStored` is not silent.

`pipeToAny` refuses an item a filter pipe does not allow before inserting it. `filteredPipeAcceptsOnlyTheMatchingItem` sends cobblestone through a filter into a chest and checks dirt does not follow.

`simpleDrop` stores `Function<Block, ItemLike>`. `MeatlibBlockSettings.create` remembers it, and the block loot provider uses it. The wooden hopper is built that way. There is no mixin injecting the drop onto `Block`.

`IServerWorld.getEnlightenmentEventManager` is back. `EnlightenmentEventManager`, `EnlightenmentManager`, `PlayerEnlightenmentManager`, `EnlightenmentUtil`, and `EnlightenmentEventPacket` compile. Without the server-world mixin applied, `EnlightenmentEventManager.get` keeps one manager per level. The player manager's client tick throws `UnsupportedOperationException` naming `HUDOverlays` and `DosimeterItem`, which still pull excluded client types.

Ejector and item-pump constructors call `noOcclusion` and `isRedstoneConductor(InventoryDetectorBlock::never)` on the properties they pass upward.

The storage bus block entity is `StorageDriverBlockEntity`. Stock still reads four cobblestone through it. `RetrievalTarget` and `StorageDriver` compile with it.

`RWAIT` is the real wait-redstone provider. Opcodes that still execute as `Instruction.end()`: COMBINE, IMPLANT, NEEPBUS_READ, NEEPBUS_WRITE, REMOVE, SELITEM.

`ItemTransport` was included and failed on the block classes it constructs. Those classes still have their own errors, so it went back on `exclude.txt`. The pipe lookup stays on `ItemPipeLookup`.

## Transaction-aware capabilities and a live item network, jar 0.30.0-neoforge.65

Combined-tree errors: **2132** at the start of this run, **2073** at the end. That is 59, not 400, and not at 1,732. The PLC closure was **172** and is **167**. `exclude.txt` is **1286**. It lost the routing network, the pipe cache, and AREA3, EXEC, DYCALL, and DYLOC. `tools/port-errors/symbols.txt` is the end histogram.

Item and fluid capability adapters simulate inside the transaction and apply the move in `Transaction.addOuterCommit`. An abort drops the plan. Nested commit moves it to the parent. Fluid amounts convert at 81 droplets per millibucket, floored, and a remainder below 81 droplets moves nothing. Views report droplets.

`TransferGameTests.abortedItemInsertLeavesTheChestAlone` inserts four cobblestone into a chest, aborts, and checks the chest is empty, then commits the same insert and checks the count. `fluidRoundTripUsesWholeMillibuckets` checks that 80 droplets store nothing, an aborted 81-droplet insert stores nothing, and a committed 161-droplet insert stores one millibucket that extracts back as 81 droplets.

`PipeCacheImpl`, `PipePathfinding`, and `PipeGraphUtil` compile. `RoutingNetworkImpl` is included. `PipeDriverBlockEntity.getNetwork` builds that network and updates it. `stockReadsAChestThroughThePipeDriver` places a chest, a storage bus, and a pipe driver and checks `stock "minecraft:cobblestone"` pushes 4. The storage bus is the existing `ItemPipeBlockEntity` on `StorageBusBlock`; `StorageDriverBlockEntity` is still excluded. `BlockApiCache` copies its position so a reused mutable position does not retarget every cache. `BlockApiLookup.find` runs block-entity providers, block providers, and fallbacks.

`ItemTransport` still does not compile in the included set. Its block registrations name classes that are still excluded. The pipe lookup and BFS depth live on `ItemPipeLookup`, and `ItemTransport` points at those same fields. Including `ItemTransport` after the constructor fixes failed on the excluded block classes. Splitting the lookup was the second path, and that is what the network uses.

Opcodes that still execute as `Instruction.end()`: COMBINE, IMPLANT, NEEPBUS_READ, NEEPBUS_WRITE, REMOVE, SELITEM, WAIT_REDSTONE. AREA3, DYCALL, DYLOC, and EXEC are real providers. `END` is the real end opcode. A second include of the seven remaining instruction classes failed: NeepBus still needs `PLCBlockEntity.getSender`, select-item needs `BuilderBlockEntity.selectItem`, remove needs the recipe packages, and wait-redstone needs a `LazyBlockApiCache.of` for a `Void` context.

`IServerWorld` no longer exposes `getEnlightenmentEventManager`. That class pulls the excluded enlightenment package. `MeatlibBlockSettings.simpleDrop` accepts the drop supplier and does not change the drop. `EjectorBlock`, `ItemPumpBlock`, and `FilteredEjectorBlock` take `BlockBehaviour.Properties`. Their previous `noOcclusion` call on `FabricBlockSettings` is not applied by the constructor. `pipeToAny` moves the item amount with `stackToAny` and does not consult pipe filters. `PipeGraphUtil` no longer names the excluded fluid-pipe vertex in its unused particle helper.

## Cached block lookups and more opcodes, jar 0.30.0-neoforge.64

Combined-tree errors: **2141** at the start of this run, **2132** at the end. That is 9, not 400. The PLC closure was **175** and is **172**. `exclude.txt` is **1300**. `tools/port-errors/symbols.txt` is the end histogram.

`BlockApiCache` caches a position. `neepmeat:item_storage` and `neepmeat:fluid_storage` read `BlockCapabilityCache` for the item and fluid capabilities and adapt them to `Storage`. Other lookups still call `BlockApiLookup.find`. Capability insert and extract ignore the Fabric transaction. Fluid amounts are millibuckets.

`LazyBlockApiCache`, `DynamicLazyBlockApiCache`, `AutoInvalidatingLazyBlockApiCache`, `InterruptEmitter`, `InterruptReceiver`, and `InterruptManager` compile and are off `exclude.txt`. COUNT, FINDSLOT, IHANDLER, ICLEAR, IDISABLE, IENABLE, IWAIT, REQUEST, ROUTE, ROBOT, and STOCK are real providers, with the seven from the previous jar.

`PlcOpcodeGameTests.countCobblestoneInAChest` counts four cobblestone through the item capability. `disableInterruptsClearsTheFlag` runs `idisable` and checks the flag. All 77 tests passed on this tree before the version string was bumped, and again on `0.30.0-neoforge.64`.

`PipeDriverBlockEntity.getNetwork` returns null. `RoutingNetworkImpl` still needs `ItemTransport` (12 errors) and `PipeCacheImpl` (10 errors), so it stays excluded. Stock reads zero from a driver until that network is live.

Opcodes that still execute as `Instruction.end()`: AREA3, COMBINE, DYCALL, DYLOC, EXEC, IMPLANT, NEEPBUS_READ, NEEPBUS_WRITE, REMOVE, SELITEM, WAIT_REDSTONE. `END` is the real end opcode.

## Scaffold transforms and seven real opcodes, jar 0.30.0-neoforge.63

Combined-tree errors: **2148** at the start of this run, **2141** at the end. That is 7, not 400. The PLC closure was **181** and is **175**. `exclude.txt` is **1315**. Seven instruction classes left it because they already typechecked: `DelayInstruction`, `ExtFetchInstruction`, `ExtStoreInstruction`, `RisingEdgeInstruction`, `SayInstruction`, `SHLInstruction`, and `SHRInstruction`. `tools/port-errors/symbols.txt` is the end histogram.

`pushTransform` and `popTransform` keep a stack of quad transforms. Each mesh quad is copied, the stack runs, and a false return drops the quad. Edits to positions, UVs, colours, and normals are baked again. Emissive flags are still not stored. Vanilla baked parts only expose their face to the transform, so a transform cannot move those vertices.

With no world, a fluid pipe emits `pipe_centre` plus the north and south arms, which is the item model's core and two arms. A vascular conduit emits those two arms. An encased conduit emits its frame.

`PlcOpcodeGameTests.shiftLeftThenRightRestoresTheValue` compiles `push 3`, `shl 1`, `shr 1` and checks the stack is 3 again.

Opcodes that still execute as `Instruction.end()`: AREA3, COMBINE, COUNT, DYCALL, DYLOC, EXEC, FINDSLOT, ICLEAR, IDISABLE, IENABLE, IHANDLER, IMPLANT, IWAIT, NEEPBUS_READ, NEEPBUS_WRITE, REMOVE, REQUEST, ROBOT, ROUTE, SELITEM, STOCK, WAIT_REDSTONE. `END` is the real end opcode. DELAY, EXTFETCH, EXTSTORE, RISING_EDGE, SAY, SHL, and SHR are real providers.

Pulling COUNT, FINDSLOT, and IHANDLER into the included compile failed. A second try that also included `LazyBlockApiCache`, `AutoInvalidatingLazyBlockApiCache`, `InterruptEmitter`, and `InterruptReceiver` still failed, so those files went back on `exclude.txt`. `RoutingNetworkImpl` still typechecks and stays excluded with the rest of the closure. Stairs and slab fields are still reached by a cast.

## Copied item tags and baked meshes, jar 0.30.0-neoforge.62

Combined-tree errors: **2229** at the start of this run, **2148** at the end. That is 81, not 400. The PLC closure (`com/neep/neepmeat/plc/` plus `RoutingNetworkImpl`) was **223** errors and is **181**. `exclude.txt` is still **1322**. `RoutingNetworkImpl` itself has no errors left, and it stays excluded until the rest of that closure does too. `tools/port-errors/symbols.txt` is the end histogram.

Top: does not override 185, invalid method reference 25, `of` 14, `getId` 13, invalid method reference 12, `register` 12, `stonecutterResultFromBase` 12, `ImplantManager` capture 11, inference bounds 11, `Block` to `Properties` 11, `checkType` 11, variable `builder` 10.

`copyRootTag` and `getOrCreateRootTag` return a detached copy. `editRootTag` and the `put*` helpers write it back with `CustomData.update`. `StackNbt.read` is also a copy; `StackNbt.edit` writes back. A grep of the files that are not in `exclude.txt` found no caller that mutates one of those copies. `MeatgunComponentImpl.markDirty` and its dirty tick use `editRootTag`.

`QuadEmitter` stores the four vertex positions, UVs, colours, and normals, and `emit` bakes them. `FabricBakedModel` is an `IDynamicBakedModel`. Its `getQuads` runs `emitBlockQuads` into a `CollectingRenderContext`, and `meshConsumer` keeps the mesh's quads. Emissive lightmaps are not stored. `pushTransform` still does nothing, so scaffold face culling is not applied. Connection models return immediately when `getQuads` has no world.

`RoutingNetwork` is an interface again, with `store`, `route`, `request`, and the update methods the implementation overrides. `StoreRouter` has `findPathTo` and `addOperation`. `ScreenHandlerRegistry.registerExtended` is `IMenuTypeExtension.create`, whose factory reads a `RegistryFriendlyByteBuf`. Scaffold and building-block stairs and slabs are reached by a cast, because the `NMBlocks` fields stay `Block` while those classes are excluded.

Opcodes that still execute as `Instruction.end()`: AREA3, COMBINE, COUNT, DELAY, DYCALL, DYLOC, EXEC, EXTFETCH, EXTSTORE, FINDSLOT, ICLEAR, IDISABLE, IENABLE, IHANDLER, IMPLANT, IWAIT, NEEPBUS_READ, NEEPBUS_WRITE, REMOVE, REQUEST, RISING_EDGE, ROBOT, ROUTE, SAY, SELITEM, SHL, SHR, STOCK, WAIT_REDSTONE. `END` is the real end opcode. Eighteen menu types stay null.

## Storage hooks, item components, and tick casts, jar 0.30.0-neoforge.61

Combined-tree errors: **2418** at the start of this run, **2229** at the end. That is 189, not 400. `exclude.txt` is still **1322**. Nothing left it. `tools/port-errors/symbols.txt` is the end histogram.

Top: does not override 196, invalid method reference 25, `of` 14, `getId` 13, invalid method reference 12, `register` 12, `ImplantManager` capture 11, inference bounds 11, `Block` to `Properties` 11, `checkType` 11, variable `builder` 10. `serverTick`, `setVisualizer`, `floatValue`, `getCompound`, `getTag`, and `meshConsumer` left the top twenty.

The PLC subtree was not pulled out of `exclude.txt`. Including `com/neep/neepmeat/plc/` and `RoutingNetworkImpl` produced 626 errors in the included compile. Adding the eight recipe and manufacture types those files name, then `InterruptEmitter`, `MeatlibStorageUtil`, `AutoInvalidatingLazyBlockApiCache`, and `ItemImplantItem`, still left 408. Both sets were put back on `exclude.txt`. `RoutingNetworkImpl` is still excluded, so `RoutablePipe.store` still takes `RoutingNetwork.StoreRouter`.

Opcodes that still execute as `Instruction.end()`: AREA3, COMBINE, COUNT, DELAY, DYCALL, DYLOC, EXEC, EXTFETCH, EXTSTORE, FINDSLOT, ICLEAR, IDISABLE, IENABLE, IHANDLER, IMPLANT, IWAIT, NEEPBUS_READ, NEEPBUS_WRITE, REMOVE, REQUEST, RISING_EDGE, ROBOT, ROUTE, SAY, SELITEM, SHL, SHR, STOCK, WAIT_REDSTONE. `END` is the real end opcode. Override errors went from 207 to 196. That is not 150 signature fixes. Eighteen menu types stay null.

`SingleVariantStorage` again has `getCapacity(T)` and `getBlankVariant()`. Each defaults to the other hook (`capacity` / `blank`), so a subclass can override either name. `WritableStackStorage` and `WritableSingleFluidStorage` route `capacity()` through `getCapacity`, so an override of the Fabric name is the one `insert` uses.

`ItemComponent` is an abstract class again. Its tag is the stack's `minecraft:custom_data`. `getOrCreateRootTag` returns a copy. The `put*` helpers write that copy back with `CustomData.update`. A key with an id stores the component under that id.

`DataVariant.floatValue` is the upstream name. `intValue` rounds it. `NMMaths.log10` and `getRotationVector` match upstream. `ContainerItemContext.getAmount` is the stack count. Ticker lambdas that call `serverTick` cast the wildcard block entity. `saveToItem` receives the loot level's registry lookup. Step height is `Attributes.STEP_HEIGHT`.

Deviations that remain:

- `RenderContext.meshConsumer` discards the mesh. NeoForge does not run the Fabric renderer. Block and item models still draw through `getQuads`.
- `BlockItem.getBlockEntityData` is `BlockEntityDataUtil.get`, which reads `DataComponents.BLOCK_ENTITY_DATA` and returns null when it is absent.
- `ItemStack.getTag` and `setTag` go through `MiscUtil.itemTag` and `setItemTag`.
- `Item.isEdible` and `ItemStack.isEdible` are `DataComponents.FOOD` on a stack.
- `setMaxUpStep` is `Attributes.STEP_HEIGHT.setBaseValue`.
- `GeneralPanel` and `VStackPanel` `clearChildren` call `clearWidgets`. `LivingMachineScreen` calls `clearWidgets`.
- `NMVisuals.setBlockVisualizer` calls Flywheel with a raw `BlockEntityType`, because the fields are wildcards or `DeferredHolder`s.
- `getOrCreateRootTag` returns a copy, so a caller that mutates the returned tag without a `put*` helper does not write it back.
- Synced component keys still store a compound tag, not the live workpiece or implant. `QuadEmitter` still does not store edited vertices. Catalogue buckets are not `MeatlibItemExtension`s.

## Creative tabs and real particles, jar 0.30.0-neoforge.60

Combined-tree errors: **2428** at the start of this run, **2418** at the end. That is 10, not 400. `exclude.txt` is **1322**. The files that compile and left it include `NMParticles`, `ParticleSpawnS2C`, `ParticleRegistry`, `SwirlingParticleEffect`, `SwirlingParticle`, `PressureWashParticleType`, the drip and pressure-wash factories, `RoutablePipe`, `NodePos`, `RecipeMatching`, and `Craftable`. `tools/port-errors/symbols.txt` is the end histogram.

Top: does not override 294, invalid method reference 26, `serverTick` 17, invalid method reference 16, `getId` 14, `setVisualizer` 13, `register` 13, `extract` 12, `addListener` 12, `floatValue` 11, `ImplantManager` capture 11, inference bounds 11, `getStorage` 11, `Block` to `Properties` 11, `getCompound` 10.

`NeepMeat.TABS` already registers `neepmeat:general`. Its `displayItems` now also accepts every `MeatlibItemGroups` entry whose id is in the `neepmeat` namespace, which includes the catalogue buckets. `MeatlibTabs.TABS` is a `DeferredRegister<CreativeModeTab>` for `meatlib:main`, filled the same way with the `meatlib` namespace. `BuiltFluid` adds each bucket to that list directly.

`ContaminatedGrassBlock` calls `RegistrationContext.withItem` again. The context registers a block item from the `ItemSettings` factory under the block path.

`NMParticles` registers during the particle registry event, because constructing a particle while that registry is frozen crashes. Fluid bits are a `BlockParticleOption` type. Sludge wash and persistent-fluid breaks send `NMParticles.FLUID_BITS` through `ParticleSpawnS2C`, which writes the particle's `StreamCodec` and sends it on `neepmeat:particle_spawn`.

`RoutablePipe` compiles. `DeferredRequest.getPipe` returns the `RoutablePipe` block entity at the source. `STREAM_CODEC` writes the request NBT.

Opcodes that still execute as `Instruction.end()`: AREA3, COMBINE, COUNT, DELAY, DYCALL, DYLOC, EXEC, EXTFETCH, EXTSTORE, FINDSLOT, ICLEAR, IDISABLE, IENABLE, IHANDLER, IMPLANT, IWAIT, NEEPBUS_READ, NEEPBUS_WRITE, REMOVE, REQUEST, RISING_EDGE, ROBOT, ROUTE, SAY, SELITEM, SHL, SHR, STOCK, WAIT_REDSTONE. No instruction class left `exclude.txt` this run. `END` is the real end opcode. Eighteen menu types stay null.

Deviations that remain:

- Coloured-item particles use vanilla `BreakingItemParticle` instead of the ore-dust tint factory. `DynamicOreDustItem` and `OreDustRegistry` are still excluded.
- Pressure-wash and fluid-drip colours come from `FluidVariantAttributes.getColor`. `MeatlibFluidRendering` is still excluded.
- `DeferredRequest.STREAM_CODEC` writes the request NBT. The upstream `ParamCodec` builder is still excluded.
- `RoutablePipe.store` takes `RoutingNetwork.StoreRouter`. `RoutingNetworkImpl` is still excluded.
- `RecipeMatching` extracts only when the storage view is a `Storage`. The fabricator loop check looks for `RecipeMatching.FabricatorStorage`, which the block entity does not implement yet.
- Crafting remainders use `getCraftingRemainingItem`.
- `TransferVariant.BLANK.getObject()` is null. Synced component keys store a compound tag. `QuadEmitter` still does not store edited vertices. Catalogue buckets are not `MeatlibItemExtension`s; they are listed by `MeatlibItemGroups` instead.

## BuiltFluid and pipe vertices, jar 0.30.0-neoforge.59

Combined-tree errors: **2483** at the start of this run, **2428** at the end. That is 55, not 400. `exclude.txt` is **1335**. Fifteen files that compile left it: `BuiltFluid`, `StandardBuiltFluid`, `FluidBuilder`, `BuiltPersistentFluid`, `PersistentFluid`, `BuiltSludgeFluid`, `SludgeFluidBlock`, `PrecisePipeBlock`, `ItemPipeVertex`, `ItemPipeGraphHelper`, `MeatlibItemGroups`, `NMVec2f`, `ContaminationUtil`, `PressureWashable`, and `ContaminatedGrassBlock`. `tools/port-errors/symbols.txt` is the end histogram.

Top: does not override 293, invalid method reference 26, `serverTick` 17, invalid method reference 16, `extract` 14, `getId` 14, `setVisualizer` 13, `register` 13, `addListener` 12, `floatValue` 11, `ImplantManager` capture 11, inference bounds 11, `getStorage` 11, `Block` to `Properties` 11, `getCompound` 10. `RequestContext`, `DeferredRequest`, `RequestType`, and `PipeRouteFinder.Node` left the top twenty.

The nine fluids are `BuiltFluid` again, with still and flowing variants. Sludge is a `BuiltSludgeFluid`, so its block is `SludgeFluidBlock`: it dries, contaminates the block below, and washes down one level. Patina treatment, eldritch enzymes, and pasteurised milk use tick 5 and level decrease 2, 2, and 1. Animal feed uses tick 5, level decrease 4, and the sand place sound. Pinkdrink, fat, liquid clay, and oil of brome use the builder defaults. Buckets are registered. `Registry.register` during the mod constructor hit a frozen registry. Construction and `RegisterEvent.register` during the fluid, block, and item events is what loads. `BaseBucketItem` does not implement `MeatlibItemExtension`, so the creative-tab cast is skipped.

`ItemPipe.getPipeVertex` returns `ItemPipeVertex.NULL`. `getConnections` reads `PrecisePipeBlock` connection properties and is empty for every other block. `getShapeForState` uses the precise pipe's shape and a full cube otherwise. `ItemPipe` extends `ConnectablePipe`.

`RoutingNetwork` again has `RequestContext`, `RequestType`, and `DeferredRequest`. `PipeRouteFinder.Node` is public. Projectile renderers call `getTextureLocation`.

Opcodes that still execute as `Instruction.end()`: AREA3, COMBINE, COUNT, DELAY, DYCALL, DYLOC, EXEC, EXTFETCH, EXTSTORE, FINDSLOT, ICLEAR, IDISABLE, IENABLE, IHANDLER, IMPLANT, IWAIT, NEEPBUS_READ, NEEPBUS_WRITE, REMOVE, REQUEST, RISING_EDGE, ROBOT, ROUTE, SAY, SELITEM, SHL, SHR, STOCK, WAIT_REDSTONE. No instruction class left `exclude.txt` this run. `END` is the real end opcode.

Deviations that remain:

- Fluid bits and sludge wash particles are vanilla block particles. `NMParticles` and `ParticleSpawnS2C` still do not compile.
- Catalogue buckets are not added to a creative tab. `NMItemGroups` is excluded, and `BaseBucketItem` is not a `MeatlibItemExtension`.
- `MeatlibItemGroups.init` does not copy entries into tabs.
- Fire's pressure-wash fallback is `PressureWashable.Simple`, not `WashableFire`.
- `ContaminatedGrassBlock` does not call `RegistrationContext.withItem`. That method is not on the context.
- `PersistentFluid.dropletAmount` is the old long `getAmount`. Vanilla `FlowingFluid.getAmount` returns `int`.
- `RoutingNetwork.DeferredRequest` has no parameter codec, and `getPipe` is absent while `RoutablePipe` is excluded.
- A pipe that is not a `PrecisePipeBlock` still reports a full-cube `getShapeForState`.
- Eighteen menu types stay null. Their screen handlers still do not compile.
- `TransferVariant.BLANK.getObject()` is null. Synced component keys store a compound tag. `QuadEmitter` still does not store edited vertices.

## Real codecs, fluids, and menus, jar 0.30.0-neoforge.58

Combined-tree errors: **2671** at the start of this run, **2483** at the end. That is 188, not 400. `exclude.txt` is **1350**. Eight files that now compile left it: `ItemInPipe`, `PipeItemMemory`, and the screen handlers for the alloy kiln, buffer, guide, router, small compressor, and Stirling engine. `tools/port-errors/symbols.txt` is the end histogram.

Top: does not override 294, invalid method reference 26, `serverTick` 17, invalid method reference 16, `extract` 14, `getId` 14, `setVisualizer` 13, `register` 13, `addListener` 12, `floatValue` 11, `ImplantManager` capture 11, inference bounds 11, `getStorage` 11, `Block` to `Properties` 11. Constructor references, raw `FluidPipeBlockEntity`, `BuiltFluid`, `putBulkData`, `has(Item)`, loot `ResourceKey`, and `LOOKUP` left the top twenty.

`FluidPipeBlockEntity` is generic again. `NeepMachines` builds it with an explicit `FluidPipeBlockEntity<Fluid>` so the deferred-register inference has a concrete argument. The first attempt, an unbounded `FluidPipeBlockEntity<?>`, inferred `FluidPipeBlockEntity<Object>` and did not compile.

Particle types that carry data now have a `MapCodec` and a `StreamCodec` that write the fields. `SwirlingParticleEffect` writes the block state, radius, and speed. `MuzzleFlashParticleEffect` writes the player id, offset, scale, age, and hand. `PressureWashParticleType` writes through its existing parameter codec. `ItemParticleOption` uses the vanilla item stack codec. The 1.20 `ParticleOptions.Deserializer` type is gone, so `@Override` sits on `codec()` and `streamCodec()`.

`ItemVariant.getObject()` returns the item. `FluidVariant.getObject()` returns the fluid. The blank transfer variant still has no object. Food reads `ItemStack.getFoodProperties`, which uses the stack's food component.

`ItemPipe.insert` is abstract again. `PipeItemMemory` and `ItemInPipe` compile, so the memory is not a stub. Connection defaults match upstream: `canConnect` is true, items enter and leave only through a connected face, `singleOutput` and `endPoint` are true. `getPipeVertex` still returns null because `ItemPipeVertex` is excluded; upstream returns `ItemPipeVertex.NULL`. `getConnections` returns an empty set because `PrecisePipeBlock` is excluded; upstream walks that block's connection properties. A grep of the included sources found no class that implements `ItemPipe` without `insert`.

`BloodAcceptor.getMode()` is abstract, as upstream. `BalancingBloodAcceptor`, the lucky-one block, and the metaboliser segment implement it. `mode()` calls `getMode()`. `ImplementedInventory.items()` is abstract. `getItems()` returns that list. The included inventories that implement the interface override `items()`.

The nine fluids upstream built with `FluidBuilder` are registered as NeoForge fluids with the same ids, tick rates, and level decrease: patina treatment, eldritch enzymes, pasteurised milk, animal feed, pinkdrink, sludge, fat, liquid clay, and oil of brome. Animal feed uses the sand place and sand break sounds. Sludge has no bucket. They are `BaseFlowingFluid` values on `NMFluids`, not `BuiltFluid`, because `BuiltFluid` is still excluded. Sludge's block is a normal liquid block; the excluded `SludgeFluidBlock` is the one that dries and contaminates.

Menu types that compile are registered with the same ids as upstream and assigned on the menu registry event: buffer, guide, router, small compressor, and Stirling engine, plus the item input and output types that were already registered. `ALLOY_KILN` points at the existing `AlloyKilnMenu` registration, because that id is already taken by the machine that runs. The other eighteen menu fields stay null. Un-excluding every handler at once did not compile (`FilterList` and the PLC handler's missing types). Registering only the handlers that compile is what stayed. A second `alloy_kiln` menu crashed registry loading and was removed.

Opcodes that still execute as `Instruction.end()`: AREA3, COMBINE, COUNT, DELAY, DYCALL, DYLOC, EXEC, EXTFETCH, EXTSTORE, FINDSLOT, ICLEAR, IDISABLE, IENABLE, IHANDLER, IMPLANT, IWAIT, NEEPBUS_READ, NEEPBUS_WRITE, REMOVE, REQUEST, RISING_EDGE, ROBOT, ROUTE, SAY, SELITEM, SHL, SHR, STOCK, WAIT_REDSTONE. Their instruction classes are still on `exclude.txt`. `END` is the real end opcode.

Deviations that remain:

- `SwirlingParticleEffect`'s 1.20 writer stored only the block state. The stream codec also stores radius and speed, which the reader already consumed.
- `getPipeVertex` returns null until `ItemPipeVertex` compiles. `getConnections` is empty until `PrecisePipeBlock` compiles.
- `ItemPipe.getShapeForState` still returns a full cube. Upstream has no such method.
- The nine catalogue fluids are source fluids, not `BuiltFluid`. Sludge does not use `SludgeFluidBlock`.
- Eighteen menu types stay null until their screen handlers compile.
- `TransferVariant.BLANK.getObject()` is null.
- `RecipeContextMatch` on `MeatlibRecipe` catches `ClassCastException` when the input is not `C`.
- Synced component keys store a compound tag. `get` is not the original implant or workpiece object.
- `QuadEmitter` still does not store edited vertex positions.
- Static helpers that upstream does not annotate no longer carry `@Override`. The instance methods that do override keep it.

## Opcode debt and override bases, jar 0.30.0-neoforge.57

Combined-tree errors: **2788** at the start of this run, **2671** at the end. That is 117, not 500. `exclude.txt` stays **1358**. `tools/port-errors/symbols.txt` is the end histogram.

"Does not override" fell from 384 to 324. The compiler does not name the missing supertype method, so the breakdown is by the class that still carries the annotation, then by the method name. The largest classes are `RoutingNetworkImpl` 10, `TankBlock` 6, `InstructionBrowserWidget` 5, `PressureWashParticleType` 5, `MuzzleFlashParticleType` 5, `AdvancedEjectorBlock` 5, `OreDustSmeltingRecipe` 5, `OreDustBlastingRecipe` 5, and `DataVariantImpl` 5. The largest method names are `codec` 6, `streamCodec` 6, `saveAdditional` 6, `loadAdditional` 6, `saveWithoutMetadata` 6, and `register` 6. Before this pass the same errors clustered on `SyncableBlockEntity`, `BaseFacingBlock`, `ParticleType`, `BaseBlock`, `FluidPipeBlockEntity`, `ItemPipeBlock`, `EntityModel`, `MeatlibRecipe`, `ImplementedInventory`, `SmeltingRecipe`, and `BlastingRecipe`.

Top of the end histogram: does not override 324, invalid constructor reference 43, invalid method reference 26, `serverTick` 26, raw `FluidPipeBlockEntity` 15, invalid method reference 15, `Fluid` to `BuiltFluid` 15, `extract` 14, `getId` 14, `checkType` 14, `ResourceKey<LootTable>` to `ResourceLocation` 13, `setVisualizer` 13, `LOOKUP` 12, `addListener` 12, `addVertex` 12. `putBulkData` and `has(Item)` left the top twenty: the bulk-quad call now passes the 1.21 alpha float, and recipe unlocks call the public `FabricRecipeProvider.has`.

Making `FluidPipeBlockEntity` generic so the fifteen parameterized uses would compile broke inference in the included `NeepMachines` sources. That change was reverted. A second pass at the same cluster, default methods for the pipe and acceptor interfaces, is already in the compiling set (`ItemPipe` connection defaults, `BloodAcceptor.mode`/`getMode`, `ImplementedInventory.items`/`getItems`). It cleared the acceptor and inventory overrides that only differed by name. It cannot clear `insert` until `ItemInPipe` compiles, and `ItemInPipe` still needs the excluded `PipeItemMemory`.

Instruction opcodes that still call `Instruction.end()` instead of their real provider: AREA3, COMBINE, COUNT, DELAY, DYCALL, DYLOC, EXEC, EXTFETCH, EXTSTORE, FINDSLOT, ICLEAR, IDISABLE, IENABLE, IHANDLER, IMPLANT, IWAIT, NEEPBUS_READ, NEEPBUS_WRITE, REMOVE, REQUEST, RISING_EDGE, ROBOT, ROUTE, SAY, SELITEM, SHL, SHR, STOCK, WAIT_REDSTONE. `END` is the real end opcode. The matching instruction classes that exist (`Area3Instruction`, `CombineInstruction`, `CountInstruction`, `DelayInstruction`, `ExecInstruction`, `ExtFetchInstruction`, `FindSlotInstruction`, `ImplantInstruction`, `NeepBusReadInstruction`, `NeepBusWriteInstruction`, `RequestItemInstruction`, `RisingEdgeInstruction`, `RobotInstruction`, `RouteItemInstruction`, `SayInstruction`, `WaitRedstoneInstruction`) are all still on `exclude.txt`, so `Instructions.java` cannot name them while the included compile stays green. `SingletonInstructions` holds `ALLOT`, `DYCALL`, `DYLOC`, `FETCH`, `FILL`, and `STORE` as `Instruction.EMPTY` for the same reason.

`NMFluids.BLOOD`, `MEAT`, `WORK_FLUID`, `COMPRESSED_AIR`, and `TISSUE_SLURRY` are filled from their registered source fluids at the end of the fluid registry event, with `FOOD` and `ETHEREAL_FUEL`. The remaining `NMFluids` fields (animal feed, eldritch enzymes, fat, liquid clay, oil of brome, patina treatment, pinkdrink, p-milk, sludge) stay null. Screen-handler, block-entity, and living-machine fields added for missing names are also null until a registry event assigns them.

Deviations that remain:

- The opcodes listed above execute as `Instruction.end()`.
- `BloodAcceptor.getMode()` defaults to `SINK` when a type only overrides `mode()`.
- `ItemPipe` connection defaults return false, null, or empty connections.
- `TransferVariant.getObject()` returns null.
- `ParticleRegistry` stream codecs write nothing and decode null.
- `getFoodProperties(ItemStack.EMPTY, null)` ignores the stack's own food components.
- `RecipeContextMatch` on `MeatlibRecipe` catches `ClassCastException` when the input is not `C`.
- Synced component keys store a compound tag. `get` is not the original implant or workpiece object.
- `QuadEmitter` still does not store edited vertex positions.
- Menu types and the extra living-machine block fields are null until something assigns them.
- `NMFluids` fields are null until the fluid registry event, and the nine unregistered names stay null after it.

## Long-tail grind, jar 0.30.0-neoforge.56

`VertexConsumer.setNormal` calls that had dropped a `Matrix3f` now use `setNormal(PoseStack.Pose, x, y, z)`. The wrench outline builds a pose that includes the extra local rotation before that call.

Combined-tree errors: **2975** at the start of this run, **2788** at the end. That is 187, not 700. `exclude.txt` stays **1358**. `tools/port-errors/symbols.txt` is the end histogram. The top entry is still "does not override" at 384. The next names are invalid constructor references 43, invalid method references 26, `serverTick` 26, raw `FluidPipeBlockEntity` 15, `Fluid` to `BuiltFluid` 15, `extract` 14, `getId` 14, `putBulkData` 14, `checkType` 14, `has(Item)` 14.

Two attempts were made past that tail. Signature groups were aligned with 1.21 (`finalizeSpawn`, recipe `assemble` / `HolderLookup`, reload listeners, food properties, screen and block-entity renderer registration, `checkType`, lookup `getId`). Missing members were then added (screen handlers, block entities, fluids, instruction opcodes, living-machine fields). A third attempt, default methods on the interfaces those overrides implement, did not compile: the methods referenced types those files do not import, and one target was a class rather than an interface. That attempt was reverted so the included sources stay green. Fixing all 384 override failures would still leave the tree above 2,275.

Missing instruction opcodes are `Instruction.end()` until their real providers are wired. `NMFluids` fields for blood, meat, work fluid, compressed air, tissue slurry, food, and ethereal fuel are filled at the fluid registry event. The other fluid fields stay null.

## Override restore, jar 0.30.0-neoforge.55

The three run-47 shortcuts are gone.

`@Override` annotations removed in `659e1172` are restored. Where the declaration had already been renamed onto the 1.21 method (`getTextureLocation`, `setupAnim`, `renderWidget`, `getAddEntityPacket(ServerEntity)`, `appendHoverText(Item.TooltipContext, ...)`, `playerWillDestroy` returning `BlockState`, `getCloneItemStack(LevelReader, ...)`, three-argument `bake`), the annotation sits on that method. Entity `load`/`save` that were still the block-entity shape are `readAdditionalSaveData` and `addAdditionalSaveData`. Item reequip is `shouldCauseReequipAnimation(ItemStack, ItemStack, boolean)`. `Model.renderToBuffer` takes the extra packed colour. Food builders use `saturationModifier`. `NbtUtils.readBlockPos` takes the parent tag and the key and returns `Optional`.

Honest combined-tree count after that restore: **3074**. End of this run: **2975**. That is 99 errors, not 800. `exclude.txt` stays **1358**. `tools/port-errors/symbols.txt` is the end histogram. The top entry is still "does not override" at 408, and the missing symbols are 669 distinct names. Grouped signature replacement and adding the missing methods on our types both move the count by tens. Neither cleared a family of several hundred.

`ItemStack` custom data writes go through `CustomData.update` (`StackNbt.edit`). Reads use `copyTag()`.

`MeatlibRecipes.getFirstMatch` calls `RecipeContextMatch.matches(input, level)`. `MeatlibRecipe` implements that by calling its typed `matches(C)`. `CrushingRecipe`, `BallMillRecipe`, and `TrommelRecipe` implement it directly.

`ComponentKey.sync` writes the component NBT into a `CompoundTag` attachment registered with `AttachmentType.sync` and calls `IAttachmentHolder.syncData`. The attachment value is the tag, not the original component class. `NMComponents` keys are those synced attachments.

`NMFluids.FOOD` is a source fluid with a `FluidType`, flowing fluid, block, and bucket (`FoodFluid`). `NMFluids.ETHEREAL_FUEL` is the already registered ethereal-fuel source. Both fields are filled at the end of the fluid registry event.

Deviations that remain:

- `RecipeContextMatch` on `MeatlibRecipe` catches `ClassCastException` when the input is not `C`.
- Synced component keys store a compound tag. `get` is not the original implant or workpiece object.
- Some `setNormal` calls that passed a `Matrix3f` now pass the three floats, so that normal is not rotated by the matrix.
- `QuadEmitter` still does not store edited vertex positions.
- `NMFluids.ETHEREAL_FUEL` and `FOOD` are null until the fluid registry event.

## Symbol histogram, jar 0.30.0-neoforge.54

Combined-tree errors: **3773** before, **2769** after. `exclude.txt` stays **1358**. The histogram is `tools/port-errors/symbols.txt`.

Top of the before histogram: does not override 531, `register` 45, `getDeltaTracker` 37, `vertex` 32, `TransactionContext` to `Transaction` 30, `MeatlibBlockSettings` to `Properties` 28, invalid method reference 27, `toNbt` 26, `BlockEntityRenderers.register` 22, `intValue` 21, `renderLevel` 21, private `MenuScreens.register` 19, `uv` 19, `readNbt` 18, `vsUtil` 17, `getId` 17, `renderBorder` 17, `getFirstMatch` 16.

Top of the after histogram: does not override 122, invalid method reference 27, `BlockEntityRenderers.register` 22, `Matrix3f` to `Pose` 18, `getId` 17, inference bounds 16, `FluidPipeBlockEntity` raw type 15, invalid method reference 15, `LOOKUP` 15, `extract` 14, `getObject` 14, `putBulkData` 14, `serverTick` 14, `checkType` 14, `has(Item)` 14.

Fabric registry events are `FabricRegistryBuilder`, which builds a vanilla `MappedRegistry` or `DefaultedMappedRegistry`. Those registries are not inserted into the registry root. `net.fabricmc.api` is `ModInitializer`, `ClientModInitializer`, `EnvType`, and `Environment`. Object builders are `FabricBlockSettings`, `FabricItemSettings`, `FabricBlockEntityTypeBuilder`, and `FabricDefaultAttributeRegistry` on `EntityAttributeCreationEvent`. The last networking sites are `ServerPlayConnectionEvents`, fired from player login and logout on `neepmeat:raw` replies. `FuelRegistry` reads `ItemStack.getBurnTime`.

Deviations in this pass:

- Where a method still carries `@Override` but the 1.21 signature does not match, the annotation was removed. The method compiles as an ordinary method, so vanilla will not dispatch it until the signature is corrected.
- `MeatlibRecipes.getFirstMatch` looks for `matches` or `accepts` by reflection.
- `ComponentKey.sync` does nothing. Attachments are expected to sync themselves.
- `ItemStack.getOrCreateTag()` reads a copy of `CUSTOM_DATA` and does not write the copy back.
- `Fluid.still()` and `Fluid.flowing()` go through `FluidForms`.
- `QuadEmitter.pos`, `uv`, and `toBakedQuad` keep the sprite and face and do not store the edited vertex positions.
- `NMBlockEntities.register` builds a type with a null built-in type token.
- `NMFluids.ETHEREAL_FUEL` and `NMFluids.FOOD` are fields. This pass does not register fluids for them.
- `Player.inventory` reads go through `getInventory()`.

## Yarn client names, jar 0.30.0-neoforge.53

Combined-tree errors: **3854** before, **3773** after.

| Family | Before | After |
| --- | ---: | ---: |
| Yarn `net.minecraft.client.render` | 12 | 0 |
| Fabric resource API | 11 | 0 |
| Apache `AbstractReferenceMap` | 10 | 0 |
| meatlib transfer leftovers | 8 | 0 |
| Fabric networking leftovers | 10 | 2 |

The eighteen files that still imported `net.minecraft.client.render` had already been through a partial Mojmap rewrite. The leftover star imports were removed and the remaining Yarn names (`SpriteIdentifier`, `Identifier`, `RenderLayer`, `LightmapTextureManager`, `Vec3d`, `MathHelper`) were renamed on word boundaries. Fabric reload listeners are `com.neep.meatlib.resource.FabricResources` and are registered on `AddReloadListenerEvent` and `RegisterClientReloadListenersEvent`. Soft/hard `ReferenceMap`s are Guava caches with soft values. `FluidVariantAttributeHandler` and `CombinedSlottedStorage` cover the last transfer symbols. `PacketSender` replies on `neepmeat:raw`.

Taking ten whole packages whose files were clean in the full compile (52 files) failed: 36 files in those packages still named types outside the package, so the packages went back onto `exclude.txt`. `exclude.txt` stays **1358**.

## Transfer and renderer shims, jar 0.30.0-neoforge.52

Combined-tree errors: **4264** before, **3854** after. Fabric transfer, Fabric renderer, and Fabric fluid-rendering packages dropped out of `tools/port-errors/packages.txt`. The next missing packages are yarn `net.minecraft.client.render`, Fabric resource and networking, and `AbstractReferenceMap`.

`com.neep.meatlib.transfer` now has the Fabric transfer types the sources import (`TransferVariant`, `ItemStorage`, `FluidStorage`, `SnapshotParticipant`, `SingleSlotStorage`, `ContainerItemContext`, `PlayerInventoryStorage`, `FilteringStorage`, `InsertionOnlyStorage`, `SingleVariantItemStorage`, `StoragePreconditions`, `FluidVariantAttributes`). Imports were rewritten onto that package. `ItemVariant` and `FluidVariant` implement `TransferVariant`.

Fluid sprites and colours are `com.neep.meatlib.client.fluid.FluidClient`, and `FluidVariantRendering.registerClientExtensions` applies `IClientFluidTypeExtensions` on `RegisterClientExtensionsEvent`. The renderer types are `com.neep.meatlib.client.renderer`. `QuadEmitter.emit` bakes one axis-aligned quad through `QuadBakingVertexConsumer`. Emissive flags, colour indexes, and non-square geometry are dropped. `FabricBakedModel.emitBlockQuads` is not called by the vanilla baker.

`FabricDataGenerator.addProvider` stores the provider and `GatherDataEvent` adds it when it implements `DataProvider`. `NMDataGen` is loaded by name when that class is on the classpath.

The clean import closure was 147 files. Same-package references put most of them back. 24 stayed out, including `CrusherRecipeContext`, `FluidBuffer`, `ItemInterfaceBlock`, and several blocks and mixins. `exclude.txt` is **1358**.

## External families, jar 0.30.0-neoforge.51

Combined-tree errors before this run: **4403**. After the family shims: **4264**.

Missing-package counts before, then after:

| Package family | Before | After |
| --- | ---: | ---: |
| Fabric datagen (`api.datagen.v1` and `.provider`) | 23 | 0 |
| Architectury | 2 | 0 |
| Kotlin (`kotlin.jvm.functions`) | 2 | 0 |

The datagen entry points now live in `com.neep.meatlib.datagen` (`FabricDataOutput`, `FabricDataGenerator`, `FabricTagProvider`, `FabricRecipeProvider`, `FabricBlockLootTableProvider`, `FabricModelProvider`, `FabricAdvancementProvider`, `FabricDynamicRegistryProvider`). Block and item tag providers extend NeoForge `BlockTagsProvider` and vanilla `IntrinsicHolderTagsProvider`. `REIScreenSupport` listens to `ContainerScreenEvent` instead of Architectury. `ValkyrienSkiesUtil` is plain Java. Ship matrices return null because Valkyrien Skies is not on the compile classpath and was not added as a dependency.

`BlockTagProvider`, `MeatLibDataGen`, `MeatlibItemTagProvider`, `BlockLootTableProvider`, `BlockRegistry`, `REIScreenSupport`, and `ValkyrienSkiesUtil` left `exclude.txt`. The other datagen files still name excluded neighbours (`NMTags`, meatgun modules, `MWItems`) and went back. `exclude.txt` is **1382**. `FabricDataGenerator.addProvider` records nothing into `GatherDataEvent` yet.

## Leaf clusters, jar 0.30.0-neoforge.50

Excluded files form 837 strongly connected components. The largest has 402 files (entities, scutters, the surgery robot). The rest are mostly single files. Leaves are compiled first.

This batch is the meatlib building-block leaves: stairs, slab, wall, leaves, column, pane, glass, light, sapling, trapdoor, painted collection, seeds, plus the datapack tick events. `MeatlibBlock.blockItem()` and `MeatlibItem.meatlibTooltip()` now have defaults, so those classes compile. Slab `genLoot` returns null because `createSlabItemTable` is protected. Glass extends `TransparentBlock`. Saplings take `TreeGrower`. Trapdoors pass the block-set type first. Item renderers are registered for every neepmeat, meatweapons, and meatlib item, and the BEWLR looks up the Fabric renderer when the item is drawn.

`MeatLibRegistration`, `EntityVariant`, `ClientChannelReceiver`, `REIScreenSupport`, and `ImplementedRecipe` stayed excluded.

The next leaf cluster that compiled is the client input events (`KeyboardEvents`, `UseAttackCallback`, `ScrollEvents`, `AppendTooltipEvent`, `AppendPlayerFeatureRenderers`, `CrosshairRenderEvent`) and `BaseBucketItem`. The following thirty-file leaf batches all still error, mostly on Fabric datagen, Architectury, and Kotlin packages. The 402-file component that holds `WormEntity`, `FarmingScutter`, and `SurgicalRobot` cannot come out until every file in it compiles.

## File grind, jar 0.30.0-neoforge.48

`FinishedRecipe` is gone from the datagen signatures. Those methods take `net.minecraft.data.recipes.RecipeOutput`. No class was added under `net.minecraft`.

`TrackedValue` reads a NeoForge `ModConfigSpec` registered on the mod container (`meatlib.toml`, `neepmeat.toml`). Until the spec loads, `getRealValue()` returns the default.

`LootTableLoadEvent` builds a `LootTable.Builder`, passes it to the Fabric listener, and appends those pools onto the existing table through `LootTable.DIRECT_CODEC`. `NMBlocks.block()` returns `NMItemSettings`, and `retainsInv`, `neepbus`, and `plcActuator` append the upstream tooltip lines. `PLCBlockEntity.getSurgeryRobot()` returns a controller that stores the player and the aim position. The loom `SurgicalRobot` movement class is still excluded. `BuiltinItemRendererRegistry` registers an `IClientItemExtensions` BEWLR on `RegisterClientExtensionsEvent`.

`exclude.txt` went from 1437 to **1413** (24 files). Combined-tree errors are about **4520** after the recipe and frame-time rewrites, down from 4612. Two attempts to lift the files that were clean in the full compile failed: those files name excluded types in the same package, so they do not compile while their neighbours stay excluded. The next hubs (`WormEntity`, `FarmingScutter`) still fail their own signatures.

## Histogram, jar 0.30.0-neoforge.47

`tools/port-errors/symbols.txt` is the breakdown of the 3,401 missing symbols from the 5,633-error tree. The top of that list is not the Fabric packages. `vertex`, `getNullable`, `TrackedValue`, `setLeft`, and `block` each repeat dozens of times.

Combined-tree errors, measured with the exclude list moved aside:

| After | Errors |
| --- | ---: |
| Pipe move, start of this run | 5633 |
| Fabric rendering and the event adapter | 5394 |
| Transfer, config, lookup, and vertex rename | 4632 |
| `setColor` / `setNormal` | 4612 |

`FinishedRecipe` cannot live in `net.minecraft.data.recipes`. That splits the minecraft module and the game will not start. Those errors are back. The count above includes the class.

`exclude.txt` has 1437 files. The next ten kinds are in `tools/port-errors/next10.txt`. The largest remaining symbol occurs 43 times, so another shim of the same kind does not cross 2,500.

Deviations in this batch: Fabric `Event.register` forwards to the NeoForge bus, and the loot listener is called without a table builder. The Fabric item and fluid `Storage` types are shapes, separate from the meatlib records. `NMBlocks.block()` returns `ItemSettings` whose `retainsInv` and `neepbus` methods keep the call chain and do not add the tooltip lines. `PLCBlockEntity.getSurgeryRobot()` returns a handle whose controller is null. Kaleido `TrackedValue` stores the default and does not read a config file. `BuiltinItemRendererRegistry` records the renderer and does not draw it through `IClientItemExtensions` yet.

## Pipes collapsed, jar 0.30.0-neoforge.46

The hand-ported item pipe, fluid pipe, pump, filter pipe, merge pipe, pipe driver, route finder, and routing network now live at the upstream packages (`transport/block/...`, `transport/item_network`, `transport/api/item_network`). The `machine/transport` copies of those classes are gone. Tanks, the storage bus, and filter entries stay in `machine/transport` because upstream has no second copy of them. `SyncableBlockEntity` implements `BlockEntityClientSerializable`: `getUpdateTag` calls `toClientTag` and `handleUpdateTag` calls `fromClientTag`.

Combined-tree errors after that: **5633**. `exclude.txt` is 1452 files. The next ten kinds are in `tools/port-errors/next10.txt`.

Pairs that are both present upstream (mixins in neepmeat and meatweapons, the egg integrator and the neepbus integrator, vat and living-machine fluid ports) are different classes and were left in place.

## Overrides restored, jar 0.30.0-neoforge.45

The bulk deletion of `@Override` is reversed. `apply_121_drift.py` now rewrites signatures and keeps the annotation: `appendHoverText` takes `Item.TooltipContext`, `getUseDuration` takes a `LivingEntity`, `mouseScrolled` takes the extra scroll axis, `isPathfindable` drops the world and position, and `neighborChanged`, `onRemove`, `getShape`, `updateShape`, and `useWithoutItem` are protected. `SingleVariantStorage.canInsert` is the method Fabric storages overrode.

`fromClientTag` and `toClientTag` still have `@Override` and still do not match a 1.21 method. They were Fabric's client sync hooks. They are not deleted. Nothing calls them until a sync payload replaces them. Thirty-four `@Override` annotations that the restorer attached to static methods or to methods that only shared a name were taken off again, because those were never overrides.

Combined-tree error count with the annotations restored: **5533** (was 5622 immediately after the restore, 5044 before it). That is not under 2500. `exclude.txt` holds 1907 files so this commit still compiles.

Duplicate simple names are different packages (the hand-ported pipe classes under `machine/transport` and the Loom copies under `transport/block`). The hand-ported living machines, NEEPASM, and the integrator egg were not replaced by Loom copies. REI category slot positions (input at x=1, output at x=61, extra output at x=81 or 87) are on the JEI categories. Recipe contents are still empty.

# Port status

Upstream is about 1,889 Java files in NeepMeat, 310 in meatlib, 38 in NEEPSpeak, and 11 in Myron, plus 4,244 resource files. A line-by-line Fabric 1.20.1 Yarn port does not compile on NeoForge 1.21.1: registries, transfer APIs, menus, networking, data components, and rendering all moved.

## Data cables and integrators, jar 0.30.0-neoforge.40

`data_cable` connects to any neighbour that implements `DataCable`, including the integrator egg, the advanced-integrator disc, and the distributor controller and structure. A pedestal finds a mature integrator through that network and spends data on an enlightening recipe. The egg grows after one bucket of blood, then gains one data per tick. The advanced integrator is the radius-1 disc and gains four data on every fourth tick until eight gieb.

Deviations: no GeckoLib hatch animation, no cosmic beam particles, no encased data cable, and no whisper-flour feeding. Blood is stored in milli-buckets. The advanced integrator counts its own ticks.

## One-tree compile, jar 0.30.0-neoforge.44

Every staged file was moved into `src/main/java`, then every Loom file that was still absent was copied in. Flywheel `1.0.6` (Create `1.21.1-6.0.10`) is `compileOnly`. `compileJava` uses `-Xmaxerrs 100000`.

Error count on that combined tree:

| Pass | Errors | What changed |
| --- | ---: | --- |
| 0 | 5952 | Staged sources moved in, Flywheel on the classpath |
| 1 | 5198 | Removed 687 `@Override` annotations 1.21 does not match. `BlockRenderLayerMap` writes NeoForge render layers |
| 2 | 7119 | Copied the other 981 missing Loom files. A `super.use` rewrite was crossing newlines and was fixed |
| 3 | 6342 | Removed 788 more stale `@Override` annotations |
| 4 | 5419 | REI sources removed again. `@Environment` became `@OnlyIn`. `useWithoutItem` bodies that still say `hand` get a local |
| 5 | 5174 | `BlockApiLookup` shim. GeckoLib is `compileOnly` |
| 6 | 5044 | GeckoLib `core.animation` / `core.animatable` / `core.object` rewritten to the 4.9 packages |

Half of 5952 is 2976. The combined tree is still at 5044. `tools/port-errors/exclude.txt` keeps those files out of `compileJava`, so the committed build and the game tests pass. The exclude list is 1452 files. It is not a claim that those files compile.

## Registries, attachments, and no REI, jar 0.30.0-neoforge.43

`NMBlocks`, `NMBlockEntities`, `NMItems`, `NMFluids`, and `NMComponents` expose the upstream field names. They are not a second registration. `NMBlockEntities.bind` copies the holders that already exist (integrator, pedestal, advanced integrator, motor, alloy kiln). The other fields stay unset until that block class is in the main tree.

REI is not ported. The pack has no REI. Its staged sources were deleted. JEI and EMI already show alloy smelting and manufacture, and they now also register the other REI category ids (`advanced_crushing`, `block_crushing`, `compacting`, `heating`, `item_manufacture`, `mixing`, `pressing`, `entity_to_item`, `grinding`, `transforming_tool`, `vivisection`, `enlightening`, `trommel`). Those extra categories have no recipe widgets yet.

Cardinal Components call sites still implement the old interfaces. Those interfaces are a thin adapter. Player compressed air is a real `AttachmentType<Long>` with a codec and `copyOnDeath`. Fabric `ServerPlayNetworking`, `ClientPlayNetworking`, `PacketByteBufs`, and `PlayerLookup` are shims over one `CustomPacketPayload` (`neepmeat:raw`) registered in `RegisterPayloadHandlersEvent`.

Flywheel visuals stay staged. `getVisualPosition` and `PartialModel` do not match a drop-in compileOnly jar, and the classes are client-only.

## Staging source set, jar 0.30.0-neoforge.42

`src/staged/java` is a source set excluded from the mod compile. `tools/batch_port.py --stage-missing --promote` copies every missing Loom package there, rewrites it, and moves a package into `src/main` only when none of its files appear in the staged javac log. A failed compile that does not name a staged file promotes nothing.

Sounds are `DeferredRegister<SoundEvent>` on `NMSounds`, with `assets/neepmeat/sounds.json`. Meatweapons events are registered on `neepmeat`, and a repeated id gets the field name. Paths that contained a slash use an underscore. `VivisectionCriterion` is a codec `SimpleCriterionTrigger` on `DeferredRegister<CriterionTrigger>`. `BloodBubbleTreeGenerator` is a `TreeGrower` record. `TallBlock` and `BaseVertFacingBlock` are ported. Yarn star imports `net.minecraft.block.*`, `entity.*`, `item.*`, `recipe.*`, and `text.*` are rewritten to Mojmap.

161 packages and 830 files remain staged. Their errors are the init registries (`NMBlocks`, `NMBlockEntities`), Flywheel, REI, Cardinal Components, and Fabric networking. A package that compiled only because a staged neighbour was present was moved back.

## Batch driver, jar 0.30.0-neoforge.41

`tools/batch_port.py` copies a Loom package, runs `apply_121_drift.py` (transfer imports, `ResourceLocation`, save/load, `useWithoutItem`, `EnvType` to `OnlyIn`, entity `defineSynchedData`), and compiles. A package is deleted again if any of its files fail, and the javac lines stay in `tools/port-errors/`.

Two selections were tried. The closed set that only needed the transfer rewrite kept 11 packages. A second pass of every remaining leaf (no missing NeepMeat dependency, no mixin, renderer, or datagen) compiled none of them: the failures are per-file 1.21 API changes (`SimpleCriterionTrigger`, `AbstractTreeGrower`, `TallBlock`, yarn `net.minecraft.block.*`, missing sound constants), not one more repeated rewrite. Dropping a failed package inside a dependency chain then failed its dependents, so that wider batch was not kept.

## Package coverage, jar 0.30.0-neoforge.40

Each row is one upstream `com.neep.neepmeat` package. **Missing** means no file in that package is ported. **Partial** means some files are present. **Files present** means every upstream filename in that package exists here. A present file can still be a shorter port; the living-machine section below is the line-by-line check.

| Package | Ported | Upstream | State |
| --- | ---: | ---: | --- |
| `(root)` | 4 | 2 | files present |
| `advancement` | 2 | 1 | files present |
| `api` | 9 | 9 | files present |
| `api/big_block` | 6 | 5 | files present |
| `api/big_block_2` | 4 | 3 | files present |
| `api/block` | 2 | 2 | files present |
| `api/data` | 1 | 1 | files present |
| `api/enlightenment` | 3 | 3 | files present |
| `api/implant` | 1 | 1 | files present |
| `api/item` | 1 | 1 | files present |
| `api/live_machine` | 16 | 16 | files present |
| `api/live_machine/metrics` | 3 | 3 | files present |
| `api/machine` | 1 | 1 | files present |
| `api/multiblock` | 4 | 4 | files present |
| `api/multiblock2` | 5 | 5 | files present |
| `api/network` | 1 | 1 | files present |
| `api/plc` | 1 | 1 | files present |
| `api/plc/instruction` | 9 | 9 | files present |
| `api/plc/interrupt` | 2 | 2 | files present |
| `api/plc/memory` | 2 | 2 | files present |
| `api/plc/program` | 2 | 2 | files present |
| `api/plc/recipe` | 2 | 2 | files present |
| `api/plc/robot` | 5 | 5 | files present |
| `api/processing` | 9 | 9 | files present |
| `api/processing/ball_mill` | 3 | 3 | files present |
| `api/processing/random_ores` | 8 | 8 | files present |
| `api/storage` | 11 | 11 | files present |
| `api/theme` | 1 | 1 | files present |
| `block` | 35 | 34 | files present |
| `block/entity` | 12 | 12 | files present |
| `block/entity/machine` | 2 | 2 | files present |
| `block/fluid` | 2 | 2 | files present |
| `block/machine` | 1 | 1 | files present |
| `block/redstone` | 1 | 1 | files present |
| `block/sapling` | 1 | 1 | files present |
| `block/vat` | 6 | 6 | files present |
| `client` | 10 | 10 | files present |
| `client/datagen` | 1 | 1 | files present |
| `client/effect` | 1 | 1 | files present |
| `client/fluid` | 5 | 5 | files present |
| `client/hud` | 7 | 7 | files present |
| `client/implant` | 6 | 6 | files present |
| `client/input` | 1 | 1 | files present |
| `client/instance` | 13 | 13 | files present |
| `client/item` | 3 | 3 | files present |
| `client/meatgun` | 3 | 3 | files present |
| `client/meatgun/animation` | 1 | 1 | files present |
| `client/model` | 3 | 3 | files present |
| `client/model/block` | 5 | 5 | files present |
| `client/model/entity` | 5 | 5 | files present |
| `client/model/legs` | 1 | 1 | files present |
| `client/particle` | 6 | 6 | files present |
| `client/plc` | 3 | 3 | files present |
| `client/renderer` | 23 | 23 | files present |
| `client/renderer/block` | 4 | 4 | files present |
| `client/renderer/entity` | 12 | 12 | files present |
| `client/renderer/item` | 5 | 5 | files present |
| `client/screen` | 16 | 16 | files present |
| `client/screen/button` | 6 | 6 | files present |
| `client/screen/living_machine` | 8 | 8 | files present |
| `client/screen/living_machine/widget` | 2 | 2 | files present |
| `client/screen/plc` | 14 | 13 | files present |
| `client/screen/plc/edit` | 3 | 3 | files present |
| `client/screen/tablet` | 7 | 7 | files present |
| `client/screen/tablet/graph` | 3 | 3 | files present |
| `client/screen/util` | 8 | 8 | files present |
| `client/screen/widget` | 2 | 2 | files present |
| `client/sound` | 7 | 7 | files present |
| `client/world` | 2 | 2 | files present |
| `compat/emi` | 5 | 2 | files present |
| `compat/emi/helper` | 2 | 2 | files present |
| `compat/emi/recipe` | 19 | 19 | files present |
| `compat/forge` | 1 | 1 | files present |
| `compat/rei` | 0 | 4 | missing |
| `compat/rei/category` | 0 | 15 | missing |
| `compat/rei/display` | 0 | 14 | missing |
| `component` | 10 | 9 | files present |
| `config` | 1 | 1 | files present |
| `datagen` | 5 | 5 | files present |
| `datagen/tag` | 1 | 1 | files present |
| `documentation` | 2 | 2 | files present |
| `enlightenment` | 4 | 4 | files present |
| `enlightenment/event` | 6 | 6 | files present |
| `entity` | 11 | 10 | files present |
| `entity/bovine_horror` | 8 | 8 | files present |
| `entity/compressor_scutter` | 3 | 3 | files present |
| `entity/effect` | 2 | 2 | files present |
| `entity/follower` | 4 | 4 | files present |
| `entity/goal` | 2 | 2 | files present |
| `entity/hound` | 3 | 3 | files present |
| `entity/keeper` | 6 | 6 | files present |
| `entity/path_legion` | 1 | 1 | files present |
| `entity/repeater` | 2 | 2 | files present |
| `entity/scutter` | 7 | 7 | files present |
| `entity/skyworm` | 5 | 5 | files present |
| `entity/smudge` | 1 | 1 | files present |
| `entity/weapon_switching` | 7 | 7 | files present |
| `entity/worm` | 9 | 9 | files present |
| `fluid` | 14 | 7 | files present |
| `fluid/ore_fat` | 1 | 1 | files present |
| `guide` | 3 | 3 | files present |
| `guide/article` | 10 | 10 | files present |
| `guide/larkdown` | 4 | 4 | files present |
| `implant` | 1 | 1 | files present |
| `implant/config` | 5 | 5 | files present |
| `implant/entity` | 20 | 20 | files present |
| `implant/item` | 7 | 7 | files present |
| `init` | 21 | 21 | files present |
| `interfaces` | 3 | 3 | files present |
| `inventory` | 1 | 1 | files present |
| `item` | 50 | 49 | files present |
| `item/filter` | 5 | 5 | files present |
| `item/util` | 3 | 3 | files present |
| `machine` | 7 | 2 | files present |
| `machine/advanced_integrator` | 6 | 6 | files present |
| `machine/advanced_motor` | 4 | 4 | files present |
| `machine/alloy_kiln` | 3 | 3 | files present |
| `machine/bottler` | 4 | 4 | files present |
| `machine/breaker` | 2 | 2 | files present |
| `machine/casting_basin` | 4 | 4 | files present |
| `machine/charnel_compactor` | 2 | 2 | files present |
| `machine/charnel_pump` | 12 | 12 | files present |
| `machine/content_detector` | 3 | 3 | files present |
| `machine/crucible` | 8 | 8 | files present |
| `machine/death_blades` | 3 | 3 | files present |
| `machine/deployer` | 2 | 2 | files present |
| `machine/dumper` | 2 | 2 | files present |
| `machine/fabricator` | 6 | 6 | files present |
| `machine/fluid_exciter` | 3 | 3 | files present |
| `machine/fluid_rationer` | 3 | 3 | files present |
| `machine/flywheel` | 3 | 3 | files present |
| `machine/grinder` | 6 | 6 | files present |
| `machine/heater` | 2 | 2 | files present |
| `machine/homogeniser` | 4 | 4 | files present |
| `machine/hydraulic_press` | 4 | 4 | files present |
| `machine/integrator` | 7 | 5 | files present |
| `machine/item_mincer` | 4 | 4 | files present |
| `machine/large_crusher` | 1 | 1 | files present |
| `machine/large_motor` | 5 | 5 | files present |
| `machine/live_machine` | 11 | 5 | files present |
| `machine/live_machine/block` | 20 | 20 | files present |
| `machine/live_machine/block/entity` | 23 | 23 | files present |
| `machine/live_machine/component` | 8 | 8 | files present |
| `machine/live_machine/item` | 1 | 1 | files present |
| `machine/live_machine/process` | 12 | 12 | files present |
| `machine/living_rock` | 3 | 3 | files present |
| `machine/mincer` | 2 | 2 | files present |
| `machine/mixer` | 5 | 5 | files present |
| `machine/motor` | 5 | 5 | files present |
| `machine/multitank` | 3 | 3 | files present |
| `machine/pedestal` | 2 | 2 | files present |
| `machine/phage_ray` | 5 | 5 | files present |
| `machine/power_flower` | 6 | 6 | files present |
| `machine/pylon` | 4 | 4 | files present |
| `machine/reactor` | 7 | 7 | files present |
| `machine/reactor/block` | 4 | 3 | files present |
| `machine/reactor/block/entity` | 5 | 5 | files present |
| `machine/reactor/disruptor` | 5 | 4 | files present |
| `machine/reservoir` | 2 | 2 | files present |
| `machine/separator` | 5 | 5 | files present |
| `machine/sifter` | 1 | 1 | files present |
| `machine/small_compressor` | 6 | 6 | files present |
| `machine/small_trommel` | 6 | 6 | files present |
| `machine/solidity_detector` | 2 | 2 | files present |
| `machine/stirling_engine` | 4 | 4 | files present |
| `machine/surgery_platform` | 2 | 2 | files present |
| `machine/surgical_controller` | 6 | 6 | files present |
| `machine/synthesiser` | 5 | 5 | files present |
| `machine/transducer` | 2 | 2 | files present |
| `machine/trough` | 3 | 3 | files present |
| `machine/upgrade_manager` | 3 | 3 | files present |
| `machine/well_head` | 3 | 3 | files present |
| `meatgun` | 2 | 2 | files present |
| `meatgun/module` | 6 | 6 | files present |
| `mixin` | 19 | 19 | files present |
| `mixin/client` | 16 | 16 | files present |
| `mixin/control_drops` | 2 | 2 | files present |
| `mixin/feature` | 1 | 1 | files present |
| `mixin/inhibit_teleport` | 1 | 1 | files present |
| `mixin/living_tools` | 2 | 2 | files present |
| `mixin/loot` | 7 | 7 | files present |
| `mixin/villager_fear_player` | 1 | 1 | files present |
| `neepasm/compiler` | 8 | 8 | files present |
| `neepasm/compiler/alias` | 2 | 2 | files present |
| `neepasm/compiler/parser` | 8 | 8 | files present |
| `neepasm/compiler/variable` | 3 | 3 | files present |
| `neepasm/program` | 3 | 3 | files present |
| `neepasm/vm` | 6 | 6 | files present |
| `network` | 15 | 13 | files present |
| `network/plc` | 4 | 4 | files present |
| `particle` | 5 | 5 | files present |
| `plc` | 6 | 5 | files present |
| `plc/arm` | 2 | 2 | files present |
| `plc/block` | 13 | 11 | files present |
| `plc/block/entity` | 14 | 14 | files present |
| `plc/compiler` | 3 | 3 | files present |
| `plc/component` | 3 | 3 | files present |
| `plc/editor` | 3 | 3 | files present |
| `plc/instruction` | 48 | 48 | files present |
| `plc/instruction/block` | 6 | 6 | files present |
| `plc/instruction/gui` | 1 | 1 | files present |
| `plc/instruction/inventory` | 1 | 1 | files present |
| `plc/instruction/memory` | 6 | 6 | files present |
| `plc/instruction/stack` | 6 | 6 | files present |
| `plc/item` | 1 | 1 | files present |
| `plc/memory` | 3 | 3 | files present |
| `plc/processor` | 4 | 4 | files present |
| `plc/program` | 1 | 1 | files present |
| `plc/recipe` | 15 | 15 | files present |
| `plc/robot` | 4 | 4 | files present |
| `plc/screen` | 6 | 5 | files present |
| `potion` | 1 | 1 | files present |
| `recipe` | 30 | 21 | files present |
| `screen_handler` | 21 | 21 | files present |
| `screen_handler/slot` | 1 | 1 | files present |
| `screen_handler/widget` | 3 | 3 | files present |
| `thord` | 3 | 3 | files present |
| `thord/compiler` | 1 | 1 | files present |
| `thord/documentation` | 4 | 4 | files present |
| `thord/parser` | 9 | 9 | files present |
| `thord/parser/node` | 15 | 15 | files present |
| `thord/token` | 14 | 14 | files present |
| `thord/word` | 4 | 4 | files present |
| `transport` | 10 | 5 | files present |
| `transport/api` | 4 | 4 | files present |
| `transport/api/item_network` | 7 | 7 | files present |
| `transport/api/pipe` | 15 | 15 | files present |
| `transport/block` | 9 | 4 | files present |
| `transport/block/energy_transport` | 6 | 6 | files present |
| `transport/block/energy_transport/entity` | 5 | 5 | files present |
| `transport/block/energy_transport/entity/vsc` | 2 | 2 | files present |
| `transport/block/fluid_transport` | 20 | 20 | files present |
| `transport/block/fluid_transport/entity` | 8 | 8 | files present |
| `transport/block/fluid_transport/machine` | 7 | 7 | files present |
| `transport/block/item_transport` | 17 | 17 | files present |
| `transport/block/item_transport/entity` | 18 | 18 | files present |
| `transport/block/item_transport/machine` | 19 | 19 | files present |
| `transport/blood_network` | 9 | 9 | files present |
| `transport/client` | 1 | 1 | files present |
| `transport/client/model` | 1 | 1 | files present |
| `transport/client/renderer` | 2 | 2 | files present |
| `transport/client/screen` | 14 | 14 | files present |
| `transport/client/screen/filter` | 10 | 10 | files present |
| `transport/client/screen/widget` | 2 | 2 | files present |
| `transport/event` | 1 | 1 | files present |
| `transport/fluid_network` | 10 | 10 | files present |
| `transport/fluid_network/node` | 4 | 4 | files present |
| `transport/interfaces` | 1 | 1 | files present |
| `transport/item` | 2 | 2 | files present |
| `transport/item_network` | 13 | 13 | files present |
| `transport/screen` | 15 | 15 | files present |
| `transport/screen/widget` | 4 | 4 | files present |
| `transport/util` | 4 | 4 | files present |
| `transport/worm` | 8 | 8 | files present |
| `util` | 18 | 18 | files present |
| `world` | 1 | 1 | files present |
| `world/feature_configu` | 1 | 1 | files present |

Upstream packages: 257. Missing 3, partial 0, files present 254.

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

## Collision and the closed partials, jar 0.30.0-neoforge.39

The compressor collision is the upstream three boxes, rotated around Y. The trommel collision is the volume shape rotated the same way. The ball mill collision is its disc. A machine with more than four ball mills, or with no progress, does not mill. The hopper's killing blow uses the `neepmeat:crusher` damage type. Active waste falls. Altar lava becomes `neepmeat:contaminated_rubble`. The lucky one stores its animation offset. The stomach item places through `modifyHitPos`. The test machine block reports power, efficiency, and a remaining-life estimate.

The line-count table below is from 0.36. After 0.40 the distributor block, block entity, and process are faithful because the controller and the structure are data cables. The ball-mill block entity is the remaining living-machine partial. That is 68 faithful and 1 partial.

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
| `LargeCompressorBlock.java` | 104 / 204 | faithful |
| `LivingMachineComponents.java` | 54 / 110 | faithful |
| `LivingMachines.java` | 189 / 241 | faithful |
| `Processes.java` | 75 / 91 | faithful |
| `block/BallMillBlock.java` | 61 / 241 | faithful |
| `block/CrusherSegmentBlock.java` | 41 / 104 | faithful |
| `block/DistributorPointBlock.java` | 59 / 283 | faithful |
| `block/ExtractorBlock.java` | 38 / 38 | faithful |
| `block/FluidPortBlock.java` | 82 / 77 | faithful |
| `block/IntegrationPortBlock.java` | 56 / 62 | faithful |
| `block/ItemInputPortBlock.java` | 48 / 50 | faithful |
| `block/ItemOutputPortBlock.java` | 43 / 43 | faithful |
| `block/ItemPortBlockItem.java` | 54 / 54 | faithful |
| `block/LargeTrommelBlock.java` | 92 / 165 | faithful |
| `block/LargestHopperBlock.java` | 62 / 156 | faithful |
| `block/LuckyOneBlock.java` | 98 / 98 | faithful |
| `block/MetaboliserSegmentBlock.java` | 13 / 190 | faithful |
| `block/PortBlock.java` | 78 / 88 | faithful |
| `block/ReprocessorBlock.java` | 73 / 88 | faithful |
| `block/ServicePortBlock.java` | 63 / 69 | faithful |
| `block/SilkyExtractorBlock.java` | 38 / 38 | faithful |
| `block/StomachBlock.java` | 13 / 225 | faithful |
| `block/TestLivingMachineBlock.java` | 42 / 121 | faithful |
| `block/TreeVacuumBlock.java` | 65 / 177 | faithful |
| `block/entity/BallMillBlockEntity.java` | 85 / 180 | partial |
| `block/entity/CrusherSegmentBlockEntity.java` | 84 / 273 | faithful |
| `block/entity/DistributorPoint.java` | 32 / 50 | faithful |
| `block/entity/DistributorPointBlockEntity.java` | 172 / 310 | faithful |
| `block/entity/ExtractorBlockEntity.java` | 41 / 41 | faithful |
| `block/entity/FluidInputPortBlockEntity.java` | 71 / 70 | faithful |
| `block/entity/FluidOutputPortBlockEntity.java` | 80 / 80 | faithful |
| `block/entity/IntegrationPortBlockEntity.java` | 101 / 135 | faithful |
| `block/entity/ItemInputPortBlockEntity.java` | 86 / 85 | faithful |
| `block/entity/ItemOutputPortBlockEntity.java` | 245 / 241 | faithful |
| `block/entity/LargeCompressorBlockEntity.java` | 53 / 67 | faithful |
| `block/entity/LargeTrommelBlockEntity.java` | 112 / 171 | faithful |
| `block/entity/LargestHopperBlockEntity.java` | 86 / 169 | faithful |
| `block/entity/LivingMachineControllerBlockEntity.java` | 19 / 112 | faithful |
| `block/entity/LuckyOneBlockEntity.java` | 56 / 117 | faithful |
| `block/entity/MetaboliserSegmentBlockEntity.java` | 57 / 100 | faithful |
| `block/entity/MotorPortBlockEntity.java` | 19 / 62 | faithful |
| `block/entity/ReprocessorBlockEntity.java` | 41 / 41 | faithful |
| `block/entity/ServicePortBlockEntity.java` | 41 / 41 | faithful |
| `block/entity/SilkyExtractorBlockEntity.java` | 41 / 41 | faithful |
| `block/entity/StomachBlockEntity.java` | 95 / 141 | faithful |
| `block/entity/StorageProvider.java` | 22 / 22 | faithful |
| `block/entity/TreeVacuumBlockEntity.java` | 42 / 82 | faithful |
| `component/ChestComponent.java` | 53 / 53 | faithful |
| `component/FluidInputComponent.java` | 26 / 26 | faithful |
| `component/FluidOutputComponent.java` | 26 / 26 | faithful |
| `component/HopperComponent.java` | 53 / 53 | faithful |
| `component/ItemInputComponent.java` | 23 / 23 | faithful |
| `component/ItemOutputComponent.java` | 25 / 24 | faithful |
| `component/MetaboliserSegmentComponent.java` | 20 / 20 | faithful |
| `component/PoweredComponent.java` | 37 / 37 | faithful |
| `item/StomachBlockItem.java` | 23 / 44 | faithful |
| `process/BallMillProcess.java` | 46 / 108 | faithful |
| `process/BioreactorProcess.java` | 60 / 77 | faithful |
| `process/CharnelPumpProcess.java` | 63 / 103 | faithful |
| `process/CrusherProcess.java` | 95 / 169 | faithful |
| `process/DisruptorProcess.java` | 109 / 120 | faithful |
| `process/DistributorPointProcess.java` | 45 / 195 | faithful |
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
