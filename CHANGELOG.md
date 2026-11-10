# Changelog
## [Unreleased]
- KubeJS recipe scripts no longer submit the ids that failed on the 9 Nov 2026 client log. `global.AutisnauticsRift` is published from `kubejs/startup_scripts/rift_api.js`, because a server script cannot assign `global` on KubeJS 2101. Postgen keeps the same span numbers, including the overworld water column and the deepsea ceiling
- Create Deco cast iron recipe ids point at industrial iron (`createdeco:industrial_iron_ingot`, `createdeco:industrial_iron_nugget`, `createdeco:industrial_iron_sheet`, block `create:industrial_iron_block`). Architect's Palette wither bone inputs use `modernfoundry:necrotic_bone`. The old Create Enchantment Industry disenchanter recipe is not registered. The color-substrate alchemy chain stays off because those ids collide with the live reagent grid
- `#forge:` item tags in the scripts now use the NeoForge `#c:` names (`slime_balls`, `foods/dough`, `glass_blocks`, `glass_panes`, `sands`, `glazed_terracottas`, `stripped_logs`, `rods/wooden`). `forge:chisels` stays, because that tag is defined by this pack. Lead casings stay on `immersiveengineering:plate_lead`. Project Red and Clockwork recipes stay behind item and mod checks

## [0.0.85]
- Overworld rift boundary 0_1 is 50 blocks taller. Density `max_exclusive` in `wrap_rift_0_1_minecraft_overworld.json` and `dry_active_minecraft_overworld.json` moves from -27 to **23**. `min_inclusive` stays -153. The clean column, placed at Y=-52, uses offsets -12 through 74, which is Y=-64 through Y=22 after the build-bottom clamp. The added band is Y=-27 through Y=22. The portal plane at Y=-49, the disc, and every other boundary stay as they are
- That overworld column is filled with water. Skylands sea level is 188, and the column top Y=22 is below it, so the whole opening is water. Generation places `rift_water_block`. Postgen and `/autisnautics_rift apply` do the same below `waterBelow` 188, and leave air at or above sea level. Other dimensions still clear to air. Already generated chunks are not swept; an existing air column needs `minecraft:air` in the apply allowlist. The 2048 cap stays, so a full column takes several preview and apply passes. Reverting these files does not put the old blocks back
- Deepsea ceiling of rift boundary 2_3 now reaches the top of that dimension. Density `max_exclusive` in `wrap_rift_2_3_oceanworld_deepsea.json` and the ceiling window of `dry_active_oceanworld_deepsea.json` moves from 302 to **320**. `min_inclusive` stays 275. The clean column covers Y=275 through Y=319. Deepsea `min_y` is -64 and height is 384, so Y=319 is the top block. That dimension has no bedrock roof. The deepsea floor at Y=-64, the portal plane at Y=300, and the disc are unchanged
- The other stack faces already include the floor or ceiling block of their dimension, and those windows stay as they are: Overworld Y=-64, Abyss Y=0 and Y=127, Otherside Y=0 and Y=127 (height 128), Caves Y=-48 and Y=127, Infernal Y=-48 and Y=63, Nether Y=0 and Y=255 (height 256), Undergarden Y=-64 and Y=127 (height 192), End Y=383 (Nullscape height 384)
- Already generated chunks do not change by themselves. `/autisnautics_rift` still caps one apply at 2048 blocks, so a full deepsea ceiling column may need another preview and apply. Reverting these files does not put removed blocks back

## [0.0.84]
- Overworld side of rift boundary 0_1 now reaches the build bottom. Density `min_inclusive` in `wrap_rift_0_1_minecraft_overworld.json` and `dry_active_minecraft_overworld.json` moves from -53 to **-153**. `max_exclusive` stays -27. The clean column for that row covers Y=-64 through -28. Nominal -153 is below the Overworld `min_y` of -64, and block placement stays clamped to -64. The Abyss slab, every other boundary, and the portal plane at Y=-49 are unchanged
- Postgen for that Overworld face uses `lo`/`clearLo` -153. Loops still clamp to the dimension minimum Y. The upper edge stays -27. The manual repair command is unchanged except a comment: the 2048 block cap stays, so a full column may need another preview and apply. Already generated columns do not change by themselves

## [0.0.83]
- Immersive Portals Overworld stack entry: `bottomY` -52 → **-49** in `config/immersive_portals.json` `dimStackPreset`. The Abyss entry is unchanged and still has no `topY` or `bottomY`. No carve, mask, radius, or roof rule was edited. This is the generation preset for a new stack link. Portals already saved in a world are not moved by this file

## [0.0.82]
- KubeJS server scripts load on this pack again. The item dump uses `JsonIO` instead of the missing `Utils.fileExists` and the blocked `java.nio.file.Files` API. Block loot is written with `ServerEvents.generateData` because `ServerEvents.blockLootTables` is not on this KubeJS build. Recipe callbacks stay bound to the recipe event, skip Thermal, Tinkers, and fluids that are not installed, and use `let` where Rhino would redeclare a `const` inside a loop
- Create 6 recipe JSON uses `id` and `count` results and `neoforge:single` fluid inputs. Tag counts are `3x #c:...` or repeated ingredients. The Create builder was turning some tags into fluid inputs with `amount: 1000`, so coke cake and the slab-to-plate cut are written as plain JSON. Lead casings use `immersiveengineering:plate_lead`. Chiseled copper and copper grates use the vanilla 1.21 items. The lapis printer uses `c:storage_blocks/lapis`. Sawdust uses `c:dusts/wood` (`immersiveengineering:dust_wood`)
- Project Red is not in the pack. The redstone relay and sequenced pulse generator overrides run only when `projectred_core:platformed_plate` exists, so Create Addition and Create Connected keep their own recipes. Platinum raw and ore processing is not registered: this pack has the lead-platinum alloy and `nuclearcraftneohaul:platinum_block`, not a raw platinum item or a platinum ore. The vanilla bundle is moved after the recovery compass instead of being added twice. No mod jars were added or removed
- Rift cleanup and the manual repair command sit on the floor/ceiling slab spans from `2be80e19` (one disc per cell, carve limited to the slab). Those `lo`/`hi` values are not widened back to the older full-height ranges, and fluid sealing stays inside that slab
- Rift exits are opened at generation for stack biomes that were missing from `#autisnautics:rift_all`: abyss `minecraft:deep_dark`, infernal and vanilla nether biomes, and the overworld and deepsea ocean, river, beach, and windswept biomes named in this repo's dimension files. The mask shape is unchanged. The late air feature still runs only where that boundary's sentinel is present, so bedrock and other solid caps inside the opening are removed during generation instead of waiting on a player visit. Nether and End build height are not defined in this repo, so their preset top planes are not retuned here
- Rift cleanup no longer drops a job when `getChunkNow` is briefly null. It retries, then parks the chunk and keeps the v4/v3 marker. The marker is removed only after a pass observes the exit band clear. The script still does not load or generate chunks
- Operators can preview one already loaded chunk with `/autisnautics_rift preview`. Applying a change requires a fresh token, an explicit block-id list, a cap, and the word `confirm`. Block entities are left in place. Nothing scans the world automatically. Reverting the scripts does not put removed blocks back

## [0.0.81]
- Add **No Height Temperature 1.0.0** (`noheighttemp-1.0.0-local.jar`, required on client and server). Disables height-based biome temperature cooling, fixing unwanted snow at the raised surface after the +125 ocean and sky-island shift. Biome temperatures and northern cold biomes remain unchanged; existing snow and ice are not removed
- Update shared client/server **Voxy** override from portal-LOD **ipa10h** to **ipa10i** (`0.2.17-alpha-sodium08-portal-lods-ipa10i`), matching the latest custom build installed in the client

## [0.0.80]
- Custom Voxy 0.2.17-alpha is now included on the server as well as the client. Distant Horizons server config removed (the mod is not in the pack)

## [0.0.79]
- Turn off the temporary origin-center rift disk. `config/paxi/datapacks/Autisnautics Abyssal Stack/data/autisnautics/worldgen/density_function/rift/center_trigger.json` is now constant `0`, so the dimension masks stay the seeded staggered boundaries. Already generated chunks are unchanged. New chunks no longer carve that 250 m center test (radius 125)
- Fix the live rift cleanup crash. KubeJS Rhino 2101.2.8 throws `TypeError: redeclaration of var previous` at `kubejs/server_scripts/rift_boundary_postgen.js:78` because a `const` inside a loop is initialized again on the next pass. The fresh-chunk neighbor scan died on the second column, so boundary cleanup never ran. The same `const` bindings in the recent-key trim, the overlapping-face check, and the server tick queue are now `let`. Cleanup behavior is unchanged. A running server needs a restart after the file is replaced so the old chunk listener is gone

## [0.0.78]
- KubeJS recipe scripts no longer crash the recipe callback when Thermal is absent (Proxy stub), when Create `.withChance()` is gone on 1.21, or when optional mods and items are missing (Tinkers, Create Deco cast iron nugget, Clockwork physics infuser, `substrate_red`, Create Enchantment Industry disenchanter, old NuclearCraft machine JSON skipped)
- Live silicon and Refined Storage recipes in `chapters.js` can register

## [0.0.77]
- add **Create Control Panels 0.2.3** (CurseForge `create-control-panels`, `create_controlling-neoforge-0.2.3-1.21.1.jar`, both sides). Needs Create 6.0.8 or newer; Create 6.0.10 is already in the pack

## [0.0.76]
- Remove **LittleTiles 1.6.0-pre237** (`littletiles`, Modrinth `RCRxC1tD`, `LittleTiles_BETA_v1.6.0-pre237_mc1.21.1.jar`, both sides) and its pakku side override. Drop `config/littletiles.json`, which is only that mod's config. CreativeCore stays: AmbientSounds and ItemPhysic still require it, and nothing else in the lock depends on LittleTiles
- Existing worlds keep their other blocks. LittleTiles tiles, doors, and structures already placed will not load and those blocks are lost when the chunks load. No other mods were updated

## [0.0.75]
- Publish the **Create: Radars** performance build `create_radar-5.0.3-perf-1.21.1.jar` (mod id `create_radar`, both sides, Git LFS `.pakku/overrides/mods/`). It was added after 0.0.74 without a version bump, so the 0.0.74 release server pack does not contain it. Needs Create 6.0.10 and Create Big Cannons 5.11.x, already in the pack
- Pakku packaging overrides, not a license change: set `redistributable: true` on server-side mods whose Modrinth license is All Rights Reserved (`LicenseRef-All-Rights-Reserved`) and that did not already have the override. Pakku's server pack bundles a project that has a CurseForge file even when redistribution is disabled, and otherwise skips the jar when `redistributable` is false ([project overrides](https://juraj-hrivnak.github.io/Pakku/overriding-projects-properties.html)). Modrinth no longer uses the license id `ARR`, which is the id Pakku 1.5.0 checks, so these entries keep the jars in the server pack if that check starts matching the current id. Client-only sides are unchanged. Added or updated: Almost Unified, Aquaculture 2, Architect's Palette, Autochef's Delight, Balm, Better Combat, Better Compatibility Checker, Brewin' And Chewin', Cobblemarks+, Cobblemon: Create Industries, Collective, Corpse, Crafting Tweaks, Crawl on Demand, Create: Copycats+, Create: Curios Jetpack & Backtank, Epic Knights, Explorify, Fungal Infection: Spore, Galosphere, Immersive Petroleum, Jade Addons, MRU, Macaw's Bridges, Not Enough Recipe Book, Passive Shield, OctoLib, Simple Voice Chat, Sophisticated Storage Create Integration, Starter Structure, Storage Labels, The Beyond, The Undergarden, Waystones, YUNG's Cave Biomes

## [0.0.74]
- Abyssal Stack: align permanent staggered rift footprints across every floor/ceiling boundary; add late top-layer cleanup and shared-mask runtime safety passes to remove bedrock, obsidian/replacement blocks and other solid caps inside rifts without opening boundaries outside them
- Keep the temporary center test enabled at **250 m diameter** (radius 125); its trigger is modular in `rift/center_trigger.json` and can be disabled by replacing that density function with `0.0` without removing the permanent staggered-rift fixes
- KubeJS boundary cleanup: cooperative **2 ms/tick target**, resumable within chunks/columns, cached footprint evaluations and all three safety passes; add low-rate `[RiftPerf]` queue/pass/MSPT diagnostics. Native engine calls can overrun the budget; this is not a hard latency guarantee
- Immersive Portals: enable server teleport logging, retaining portal performance settings and the nine-layer stack
- Update custom LFS overrides: **Octolegs 0.7.5** and **Voxy portal-LOD ipa10h**; retain the other five custom override jars
- Bundle both Adventurer's Harness jars as **both-side Git LFS overrides**; keep the four resolved platform additions and CreativeCore update exactly version/hash locked through Modrinth
- Update **CreativeCore 2.13.53**; add **LittleTiles 1.6.0-pre237**, **Adventurer's Harness 1.1**, **Create: Adventurer's Harness 1.0**, **Nearby Crafting 1.3.0**, **Recipe Book Access 1.4.0** (both sides), and **Nowheel 2.1.1** (client); remove **WorldEdit 7.3.8**
- Include Cobblemon boss addon/load order, Natural Temperature equatorial distance 25000, new harness/nearby-crafting common/server defaults and Undergarden JAOPCA materials
- Worldgen changes require a **new world or ungenerated chunks**: already-generated 1 km holes do not shrink. Fluids remain, including water sections and Nether lava

## [0.0.73]
- remove Touhou Little Maid, its configs, the "Mass Bullying" fairy quest, and the leftover AttributeFix, Jade, and Astral amulet entries that pointed at it

## [0.0.72]
- Immersive Portals: replace the 5-layer skyland stack with the **9-layer Abyssal Ascent** dim stack (top→bottom): overworld, `infinite_abyss:first_layer`, `deeperdarker:otherside`, `oceanworld:deepsea`, `dimension_of_caves:cave`, `infernalcross:dimension`, nether, `undergarden:undergarden`, end. Keep `maxPortalLayer` 1, `indirectLoadingRadiusCap` 3, `netherPortalMode` disabled, `endPortalMode` normal
- Paxi datapack **Autisnautics Abyssal Stack**: ported abyss/cave/infernal dims + Lithostitched boundary rifts; aquifers on for BC-carved layers; `fluid_level_floodedness: -1.0` on first_layer/cave/infernal; cave/infernal `min_y: -48` so ImmPtl floor bedrock matches `bottomY`
- add **Deeper and Darker 1.4.1** and **The Undergarden 0.9.6** (Modrinth, both sides) + first-launch common/Cristel configs
- `.gitignore`: allow `config/paxi/datapacks/**/data/` so Paxi datapack namespace folders stay trackable

## [0.0.71]
- replace the starter house with joshspaceboy's Skyview island (house and island only, airship left out). Empty cells are structure void so the paste does not carve a box out of the skylands. Spawn is on the island grass, and the island is lifted 64 blocks above the surface

## [0.0.70]
- force `redistributable: true` on the All Rights Reserved mods so the server pack actually contains them: Iron's Artifice, Iron's Lib, Create Aeronautics, Deep Seas, Walking Steel, Flight Control, Eco Stack Manager, KubeJS Diesel Generators, Natural Temperature, NeoOrigins, NuclearCraft Neohaul, Presence Footsteps

## [0.0.69]
- commit the leftover instance configs: C2ME drops the removed native-acceleration block, Jade plugin list, NuclearCraft overhaul coils, submarine hull block list (Mystcraft entries gone), JAOPCA blank lines, FTB quest formatting, plus first-launch AttributeFix, Hilt, Iron's Artifice, and Modern Foundry Cristel configs

## [0.0.68]
- Immersive Portals: default dimension stack for new worlds, top to bottom: skyland, overworld, `oceanworld:deepsea`, nether, end. `netherPortalMode` disabled so an 8:1 portal cannot skip the ocean. `maxPortalLayer` stays 1
- Paxi datapack **Autisnautics Ocean**: Abyssal Ascent deep sea (`oceanworld:deepsea`, water to y=300, floor portal at y=-64). Biomes: deep frozen ocean, warm ocean, deep ocean, Alex's Caves abyssal chasm
- add **The Beyond 1.0.1** (CurseForge/Modrinth, both sides) and **Isleweaver 0.0.3b** (Modrinth) so Nullscape can share the End


- track **Autisnautics Density Ores** Paxi datapack (folder was listed in `config/paxi/datapack_load_order.json` since 0.0.64 but never committed): density oregen + Cobblemon sky-island ores + **105 molten caldera** `placed_feature` / structure NBTs
- Skylands Over the Sea: shift all 8 `skylands_over_the_sea:y` windows in `density_function/islands/final.json` by **+125** (0.0.64 shipped the unshifted ranges); restore `"ore_veins_enabled": true` in Skylands `noise_settings/overworld.json`
- Autisnautics2: shift 23 Regions Unexplored Overworld cave-plant / crystal / redstone ceiling `placed_feature` heights by **+125** (`absolute` 256→381; `ore_redstone_large` 112→237)

## [0.0.66]
- use the **YUNG's Better Caves** fork `InvalidArgument3/YUNGs-Better-Caves` **3.1.7** (GitHub) instead of stock 3.1.6; restore `config/bettercaves/` and Autisnautics2 island cave carvers (`better_cave` / `surface_cave`)

## [0.0.65]
- drop stock **YUNG's Better Caves 3.1.6** and **Mystcraft Reborn** (plus their configs)
- Immersive Portals: cap `maxPortalLayer` 5→**1** and `indirectLoadingRadiusCap` 8→**3**

## [0.0.64]
- remove **World Preview NeoForged 1.3.1** (`world-preview-neoforged-(unofficial)`, client). Crashes Create World with C2ME when the Preview tab closes its dummy `ServerLevel` off-thread (`Async update chunks`)
- bump **Create Aeronautics: Octo Legs** 0.5.2 → **0.7.3** (both sides, `.pakku/overrides/mods/`, Git LFS); refresh `config/octolegs-server.toml` (`minFootDistance`)
- add **Modern Foundry 1.21.1-1.6.1** (TinkersConstruct Unofficial Port jar, both sides, `.pakku/overrides/mods/`, Git LFS). Hilt 1.13 is jar-in-jar. First-launch `config/modernfoundry-common.toml` / `modernfoundry-client.toml`
- add **EMF Compat: Better Combat 2.1.1** (Modrinth, client) + `config/emf_compat.json`
- add **Iron's Arms 'n Artifice 1.21.1-1.1.1** and **Iron's Lib 1.21.1-2.3.0** (Modrinth, both sides, not redistributed)
- KubeJS: levitite startup/tags (Rhino-safe ItemStack handling), oregen/`_helper` height + explicit-ore helpers, Simple Clouds init tweak, 50 km worldborder script
- track `config/cobbledlevels.json`; tune `config/cobblemon/main.json` spawn rates; update Skylands Over the Sea density/worldgen + Paxi datapack load order

## [0.0.63]
- remove **Create: Atmospherics 2.0.0-oceans** (`aeronautics-wind-and-weather`, both sides). Too heavy. Also drop `config/aeronauticswinds-common.toml` and the `aeronauticswinds:*` entries in `config/submarine_hull.json`

## [0.0.62]
- bump **Create Aeronautics: Octo Legs** 0.3.6 → **0.5.2** (both sides, `.pakku/overrides/mods/`, Git LFS): wall climb and wall-descend foothold fixes. Aim rises/drops with vertical motion; the step trigger uses horizontal and vertical separation from that aim so sneak-descend walks down the same face. Notes in `docs/octolegs-0.5.0-notes.md`, `0.5.1`, `0.5.2` (plus `docs/limb-systems-analysis.md`)
- refresh `config/vista-common.toml` for newer Vista schema (television / picture tape / mirror / wave gate / view finder sections)
- track first-launch configs for Create Atmospherics, Create Deep Seas, Extra Disks, Let it Walk, and ExtraQuests: `aeronauticswinds-common.toml`, `create_high_seas-server.toml`, `create_submarine-common.toml`, `create_submarine-server.toml`, `create_submarine-vortex.toml`, `submarine_hull.json`, `extradisks-server.toml`, `let_it_walk-server.toml`, `config/extraquests/`

## [0.0.61]
- re-add **iasodiumfix 1.0.0-local** (client only, `.pakku/client-overrides/mods/`, Git LFS). **Test fix** for parts of the world de-rendering near portals: Sodium 0.8 caches draw batches per region, and portal views could reuse the main view's (or another portal's) cached batches. The fix clears a region's cached batches whenever a different render pass (main view vs a specific portal and layer) draws it. IP: Aeronautics 2.8.17 already clears them when the render list changes, but its portal lists are per layer, so two portals on the same layer could still share stale batches. Checked against Sodium 0.8.13 and IP: Aeronautics 2.8.17. Remove the jar to revert
- remove Spatial GUI 1.10.1 (client only; YACL stays as other mods need it)

## [0.0.60]
- add **Create: Atmospherics 2.0.0-oceans** (CurseForge `aeronautics-wind-and-weather`, both sides): wind, sails, waves, and weather for Create. Needs Create 6.0.10 and Sable 2.0.x, already in the pack. All Rights Reserved, not redistributed
- add **Create: Deep Seas 3.4.7** (CurseForge, beta, both sides): submarines and boats on Create Aeronautics. Needs Create, Create Aeronautics, and Sable, already in the pack. All Rights Reserved, not redistributed
- remove Grassier Grass 1.4.5
- add **Create Aeronautics: Octo Legs 0.3.6** (`octolegs`, both sides): placeable tentacle legs for Create Aeronautics ships. Our own mod, not on Modrinth/CF; shipped via Git LFS as a manual override (`.pakku/overrides/mods/octolegs-0.3.6+mc1.21.1.jar`). Server config `config/octolegs-server.toml` included (also `legworks-server.toml` and `tentaworks-server.toml` for Landing Legs / Octo Arms)
- add **Let it Walk! 0.1.1** (CurseForge, beta, both sides): Create/Sable walking machines. Requires Create and Sable (both already in the pack)
- add Fancy World Animations [FWA] 1.3.4 (Modrinth, client)
- re-add **iavistacompat 1.0.0-local** (both sides), our Vista ↔ Immersive Portals: Aeronautics bridge: IP:A cancels vanilla chunk tracking, so Vista viewfinders far away showed no live feed; this feeds Vista's view zones into IP's per-player chunk loaders and cancels Vista's conflicting entity-tracking mixin. Shipped via Git LFS (`.pakku/overrides/mods/iavistacompat-1.0.0-local.jar`)
- Voxy portal-LOD build `ipa10` → **`ipa10f`** (client only, `.pakku/client-overrides/mods/`). **Test build**: intended to fix LOD flicker with IP:A portals, not yet confirmed in game. Kill switch unchanged: `-Dvoxy.portalLods=false`

## [0.0.59]
- **pack now requires Java 25** (client and server): Cobblemon 1.7.3 → 1.8.4, patched to run on Java 25 (Java 21 lock lifted; bundled GraalJS `Unsafe.ensureClassInitialized` call replaced). Not on Modrinth/CF, shipped from the repo via Git LFS as a manual override (`.pakku/overrides/mods/Cobblemon-neoforge-1.8.4+1.21.1-j25.jar`)
- Cobblemon add-ons for 1.8.4: MoreCobblemonTweaks 1.3.6, CobbledLevels 2.1.0, Create Industries 1.5.0, cobblemarks+ 1.2.2, Lootrmon 0.0.0.3, summon orbs 1.0.0 (cobblemon_summon_orbs build); CCC Myths & Legends compat → 2.21 mod version (CF) replacing the 1.7.3 datapack zip
- add **Immersive Portals: Aeronautics 2.8.17** (`ip_aeronautics`, bundles Immersive Portals core 6.0.7+ipa.2.8.17), our modified fork of Immersive Aeronautics / Immersive Portals for NeoForge, shipped via Git LFS as a manual override (`.pakku/overrides/mods/ip_aeronautics-2.8.17.jar`). It replaces the old portal mods: Immersive Aeronautics 1.1.4 and Create: Aeroportals 1.3.6 are removed, and the earlier local iasodiumfix / iavistacompat jars are dropped (iavistacompat came back in 0.0.60, iasodiumfix in 0.0.61). Incompatible with Create Transferables and Dimensional Threading (neither is in the pack)
- add Voxy 0.2.17-alpha-sodium08 **experimental portal-LOD build** (`ipa10`, built for IP: Aeronautics; Voxy LODs render inside same-dimension portal views; client only, via Git LFS in `.pakku/client-overrides/mods/`). Kill switch: add `-Dvoxy.portalLods=false` to JVM args
- add Cobblemon: Cobble it 1.0.0 (Modrinth, client)
- fix Cobblemotion 1.5.1 and Winterglide's Extra Animations 1.1 placement: now resource packs (`resourcepacks/`, client) instead of zips in `mods/`, plus their data halves as Paxi datapacks (`config/paxi/datapacks/`, both sides), matching the dev instance.
- resource packs on by default: Fresh Animations 1.11.2, FA+ Player 1.1, FA+ All Extensions 1.10.1, Cobblemotion 1.5.1 and Winterglide's Extra Animations 1.1 now auto-load via Paxi (`config/paxi/resourcepack_load_order.json` points at them in `resourcepacks/`, bottom→top: FA, FA+ Player, FA+ All Extensions, Cobblemotion, Winterglide). They are still downloaded from Modrinth by pakku, not committed (FA/FA+/Cobblemotion forbid redistribution). If you already enabled them by hand, untick the manual copies in the resource pack menu
- **storage swap: Applied Energistics 2 → Refined Storage.** ⚠️ **Existing worlds lose every AE2 block, item and everything stored in ME drives/cells. Empty your AE2 network before updating, or keep a world backup.** Removed: Applied Energistics 2 19.2.18, AE2 Wireless Terminals 19.5.1, AE2 Network Analyser 2.1.5, plus AE2-only libraries GuideME 21.1.20 and Glodium 2.2. Added: Refined Storage 2.0.9, RS JEI Integration 1.0.0, RS EMI Integration 1.0.0 (client), RS Curios Integration 1.0.0, Extra Disks 4.0.15 (beta)
- KubeJS/quests for the swap: AE2-specific recipes are skipped when AE2 is absent (presses/printed circuits, processor assembly, certus/fluix/sky stone mixing, entropy manipulator, matter ball/plastics, singularity → paint ball → chromatic compound, ender dust); silicon recipes now give `refinedstorage:silicon`, calculation mechanism uses RS silicon, flash drive uses RS improved processor, controller-gated recipes use the RS controller; Create's default rose quartz polishing returns. Stage 4 silicon/controller/processor quests and the overview icon point at RS items.
- chromatic compound without AE2: heated mixing of crushing wheel + 2 quartz enriched iron + ender pearl + red/yellow/green/blue/magenta dye → Chromatic Singularity, then an Item Drain turns it into chromatic compound + 250 mB lumisene. Refined radiance (light) and shadow steel (void) are obtainable again. Matter plastics now come from 9 paraffin wax (superheated compacting, Immersive Petroleum). Both are only active when AE2 is absent
- calculation tier now gates Refined Storage the way it gated AE2. Quartz no longer smelts into silicon (silicon compound only). Fluix casing is 1 quartz enriched iron + 1 RS machine casing, and the controller is that casing around a calculation mechanism. Deploying the controller makes the disk drive (onto an advanced processor), grid (improved processor), construction cores (glowstone, 4), destruction cores (quartz, 4), and the wireless transmitter (ender pearl). The 1k storage part and the pattern take a calculation mechanism. Importers, exporters, autocrafters, and external storage stay their normal crafts, so they wait on those cores. Superheated ore-dust melting uses paraffin wax in place of matter balls
- quests: the remaining ~30 quests that showed AE2 items now use RS/vanilla/Create items (certus → nether/rose quartz, paint balls → dyes, singularity → quartz enriched iron, ender dust → ender pearl, printed circuits → processor binding/raw RS processors, fluix → amethyst shard (alchemy) or quartz enriched iron (reward), matter ball → paraffin wax). Quests whose step no longer exists (sky stone mill, volatile sky solution, certus recharger, singularity reactor, entropy manipulator) keep their checkmark so the chain still progresses. English quest text updated; other languages still show the old AE2 wording
- aero_ik_legs not included (removed from our build); Create Aeronautics Landing Legs (legworks) stays
- CI: build checkout pulls Git LFS files so the manual override jars (`.pakku/overrides/mods`, `.pakku/client-overrides/mods`) land in the exported packs

## [0.0.58]
- Cobblemon 1.7.3 + addons: CobbledLevels 2.0.0, Legendary Birds 1.2 (2.0 needs Cobblemon ≥1.8.1), MoreCobblemonTweaks 1.3.3, cobblemarks+ 1.1, Create Industries 1.3.2, summon orbs, cobblemore lib, CobGyms 3.0.1, CCC Myths & Legends compat, Extra Move Anims, Lootrmon
- EMF 3.3.12 + ETF 7.2.7 + emf_compat_core 2.3.2
- Fresh Animations 1.11.2 + FA+ Player 1.1 + FA+ All Extensions 1.10.1; Cobblemotion 1.5.1; Winterglide Extra Animations
- Create Aeronautics Landing Legs (legworks) 1.0.0, Octo Arms 1.6.0, Spatial GUI 1.10.1, Touch'n Motion 1.0.4
- skipped: cobbleit (CF modpack, not a mod); voxy (Fabric-only, not on 1.21.1 NeoForge)

## [0.0.57]
- fix serverpack crashloop: forge-auto-install NeoForge pin tracks pakku-lock again (was stuck on 21.1.227)
- Aeronautics Camera Sync → BOTH so Player Tilt's required dep ships in the serverpack

## [0.0.56]
- add Vista
- drop Embers Questing (NPE on null recipe results at world load)
- FancyMenu: align title-screen side icons; enable modpack mode to lock layout editing

## [0.0.55]
- add Astral Sorcery 2.0.3 (alpha) + ObserverLib
- drop Infinite Railways / Railways Untold / Steam 'n' Rails (Skylands worldgen mismatch)
- pakku lock refresh: mod file bumps from sync; restore Yeetus Narratus + Piglin Proliferation after collateral rm

## [0.0.54]
- quest remap: Create / Taking Flight / Stellaris spine; hide dead CABIN economy + origin chapters; Thermal leftovers gated in KubeJS
- scrub leftover Tinkers IDs from hidden quests, market, reward tables, and matrix prose
- add Enigmatic Legacy Plus for Accursed One (cursed ring start + quest IDs); remove Researchd
- disable Seven Curses starter gear / Accursed origin ring give
- shift Ender Chest inventory button left onto Create goggles slot
- starter kit: Sophisticated Backpack + pocket lantern
- creative_tabs.js: stop KubeJS errors from missing cb_microblock / Supplementaries globe
- ignore runtime item/recipe dumps under docs/dumps/
- testing/: Prism CLI smoke-test scripts (launch-sp, ensure-test-world, log watch)

## [0.0.53]
- NeoForge 21.1.248 → 21.1.265 (Propellers+ needs ≥249)
- mass mod updates via pakku
- Create Aeronautics companions: avionics, propellers+, portals, logistics, flight control, camera sync, player tilt, toolgun, curios/jetpack bridges
- owo-lib 0.12.15.6: fix Accessories custom_payload kick on NeoForge ≥262
- pin JEI to 19.64.0.455 (19.70 beta VerifyError with EMI)
- fzzy_config: use real jar (Modrinth marked sources as primary)
- Regions Unexplored 0.6.3: drop Autisnautics2 placed features for removed blackstone_cluster / steppe_grass
- refresh generated configs after updates (serilum/almanac/letmedespawn migrations, aero defaults, etc.)

## [0.0.52]
- updates

## [0.0.51]
- fix AutisOrigins missing from NeoOrigins picker: add origin layer merge for all custom origins
- disable remaining NeoOrigins built-ins (golden_body, sword_immortal) so only AutisOrigins show

## [0.0.50]
- bunch of mod updates, i can increment thev ersion number by 8 because I feel like it

## [0.0.42]
- bunch of mod updates and cramming the kube scripts back together


## [0.0.41]
laser alchemy part 2
- laser alchemy: implement reagent manufacturing + blend analysis using oritech centrifuge
- _a2_generate.js: add intermediate lidless jar block, register legally distinct alchemical waste fluid
- add convenient automated recipe for supplementaries:jar
- generate.js: replace some old stuff, remove tags for reagent ingredients (broken?)

## [0.0.40]
laser alchemy part 1
- ported bonkers_chemistry using simulated:laser_pointer, implemented chaos transmutation
- add laser shifting, transmuting reagents in a limited, lossy way using colored lasers
- register items
- tooltips.js: reagent/catalyst tooltips

## [0.0.39]
- generate.js: fix all kubejs block models
- autisnautics2textures: temporary replacement for thermal models used in machines
- Unzip CABIN resourcepack & fix broken copper machine texture regression
- port tooltips.js
- create_hypertube.js: use kubejs rubber

## [0.0.38]
- the secret sixth level of parkour civilization

## [0.0.37]
- downgrade youkai's homecoming to match downgraded farmers delight (fixes emi crash)

## [0.0.36]
- worldgen: vanilla, terralith, regions_unexplored placed features now spawn on all island levels

## [0.0.35]
- beware of farmers delight 1.3, a bunch of tag shit changed so it was downgraded


## [0.0.34]
- update pakku jar
- add createslop resource packs to see how they look
- add neat photography mod
- orthinopter glider mod
- starter structure mod
- rm bettermodsbutton
- rm youkais delight due to farmers delight 1.3 causing a crash

## [0.0.33]
origins port part 1
- unzip originpack
- ported layer
- starting items are callback commands (neoorigins:starting_equipment doesnt work lol)
- replace lost icons, starting_equipment items
- attributeslib -> apothic_attributes
- rat: neoorigins substitute for origins:climbing
- alchemist: Port alchemical allocation, half-finished bombs port

- chapters: deepslate gen, register liquid soul

## [0.0.32]
- regexsearch release id thing

## [0.0.31]
- regexsearch patch

## [0.0.30]
- chapter 1: add cobblegen mod for andesite generation

## [0.0.29]
- chapter 1: kubejs water straining for sand/clay with farmersdelight:basket

## [0.0.28]
- updoot

## [0.0.27]
- version boomp

## [0.0.26]
- worldgen fixes, add origins mod, preparing for chapters
- oregen: remove oritech resource nodes, stellaris:steel_ore
- add neoorigins, disable base origins & classes in config, whitelist originpacks folder to build
- mod updates and config inits
- unzip a2 datapack and resourcepack; re-enable cave gen in skylands via datapack
- register kubejs:rubber replacement for thermal:rubber
- kubejs tree tapping for resin (thermal arboreal extractor replacement)
- add create: blazing hot

## [0.0.25]
- replace """""advanced"""" rocketry with stellaris (no relation)
- add dependencies
- include curseforge source for some mods

## [0.0.24]
- Add Advanced Rocketry 3 and Mystcraft Reborn for testing purposes

## [0.0.23]
- try to fix earlyload screen

## [0.0.22]
- custom iris .jar to fix absurd shader screen issue (I even submitted a PR wow)
- custom loading screen
- PIP tv camera

## [0.0.21]
- what's left of ores should now gen again

## [0.0.20]
- teach git to not ignore its responsibilities

## [0.0.19]
- Actually add Terralith + SOTS this time
- Add Citadel (Alex's dependency)

## [0.0.18]
- Update Terralith to 2.5.8 and port custom changes from 1.20.1
- Enable Skylands + Terralith
- A2_hide.js: comment/delete some dead mods
- oregen.js: prepare for oregen update

## [0.0.17]
- Add Alex's Caves and Alex's Mobs unofficial ports

## [0.0.16]
- really trying to avoid connector #2

## [0.0.15]
- really trying to avoid connector

## [0.0.14]
- rm sizechangepotions

## [0.0.13]
- rm ksyxis

## [0.0.12]
- rm teams offliner from server, client only mod

## [0.0.11]
- remove useless extraguages mod stopping me from launching

## [0.0.10]
- rip webdisplays you were annoying

## [0.0.9]
- script tweak

## [0.0.8]
- webdisplays trouble

## [0.0.7]
- linux server starter
- server startup scripts (`start_server.bat` and `start_server.sh`) now use a basic auto-restart loop
- server updates no longer overwrite host-local Java path/runtime config (`server_starter.conf` removed from managed `server_overrides`)

## [0.0.6]
- release test 6

## [0.0.5]
- release test 4

## [0.0.4]
- release test 3

## [0.0.3]
- release test 2

## [0.0.2]
- release test

## [0.0.1]
- rebase on to 1.21 for create aeronautics, many have died
