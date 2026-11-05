# Changelog
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
