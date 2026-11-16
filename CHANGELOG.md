# Changelog
## [0.1.23]
- Custom Voxy portal-LOD build **ipa10u** → **ipa10v** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10v.jar`, client and server). A column is held only after the client acks it, so a ring reset no longer skips columns the client never applied. A stationary player does not restart the ring, and a pause always logs a cause. The client prints a `[terrain-sync][client]` line every 5 seconds, and coverage scans sections still in memory. zstd protocol 3, the 1 MiB cap, and off-thread apply stay. The ipa10u jar is removed.

## [0.1.22]
- Custom Voxy portal-LOD build **ipa10t** → **ipa10u** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10u.jar`, client and server). A raw section starts with voxel protocol byte 2, which the client treated as a deflate frame and dropped, so `TerrainApplyQueue.offer` threw `Terrain count outside bounds` on every batch. Failed frames are kept, empty or oversized batches are rejected before the render thread, and a column that fails 32 sends is deferred for the next pass instead of abandoned. zstd protocol 3, the 1 MiB cap, and off-thread apply stay. The ipa10t jar is removed.

## [0.1.21]
- Custom Voxy portal-LOD build **ipa10s** → **ipa10t** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10t.jar`, client and server). Section decode and `WorldUpdater.insertUpdate` stay on `voxy-terrain-apply`, off the render thread. The client holds at most 48 batches or 8 MiB and acks a batch when it has been applied, so a slow client pushes back on the server window. zstd protocol 3 and the 1 MiB player byte cap stay. `acceptLevel` still returns a `ClientLevel`. The ipa10s jar is removed.
- Custom Voxy portal-LOD build **ipa10r** → **ipa10s** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10s.jar`, client and server). The reliable queue was sending two frames per tick and the client applied them on the render thread, so a standing player got about 2.3 columns per second and the ring sat on `waiting`. Applies now run on `voxy-terrain-apply` and write into the render engine. A server-thread stall longer than two seconds no longer resets the ring. zstd protocol 3 and the 1 MiB player byte cap stay. `acceptLevel` still returns a `ClientLevel`. The ipa10r jar is removed.

## [0.1.20]
- Custom Voxy portal-LOD build **ipa10q** → **ipa10r** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10r.jar`, client and server). The per-player terrain byte budget is `server_terrain_sync_player_bytes_per_tick` (default 1 MiB per tick, allowed 64 KiB..8 MiB). Hello protocol 3 frames each section with zstd, counted against that budget after compression and compressed on the worker pool. Protocol 2 clients still receive raw sections. `acceptLevel` still returns a `ClientLevel`. The ipa10q jar is removed.

## [0.1.19]
- Custom Voxy portal-LOD build **ipa10p1** → **ipa10q** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10q.jar`, client and server). Generated columns are read on the chunk IO worker and voxelized on a two-thread pool. The server tick only offers finished columns, nearest first, and still sends one column when the average tick is over 40 ms. `acceptLevel` still returns a `ClientLevel`. The ipa10p1 jar is removed.

## [0.1.18]
- Custom Voxy portal-LOD build **ipa10p** → **ipa10p1** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10p1.jar`, client and server). ipa10p crashed the client on startup with `java.lang.VerifyError` in `TerrainSyncClientHandler.tickDrainApplyQueue`: `player.level()` is a `Level`, and the apply path calls `ClientLevel.registryAccess`. The player's own level is now taken as a `ClientLevel`. A packet for that world is still applied while Immersive Portals has swapped the render level, and a packet for any other world is still dropped. The ipa10p jar is removed.

## [0.1.17]
- Custom Voxy portal-LOD build **ipa10o** → **ipa10p** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10p.jar`, client and server). A quiet server sends up to 32 columns per tick instead of one, and still sends one when the average tick is over 40 ms. Coverage keeps scanning until the local Voxy storage exists, and the client debug line `[terrain-sync-client]` follows `server_terrain_sync_debug`. A packet for the player's world is applied while Immersive Portals has swapped the render level. The ipa10o jar is removed.
- Custom Voxy portal-LOD build **ipa10n** → **ipa10o** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10o.jar`, client and server). A saved chunk that produces no LOD no longer parks the ring. The walker was stopping about three rings out, on the first column past the loaded chunks, and only continued when the player changed chunk. The ipa10n jar is removed.
- **The welcome chapter reads as a tutorial.** The opening quests are retitled Autisnautics, Exploration, The Magic Mods, Create and the Tech Mods, Multiplayer, and What Is In This Book. The same text is rewritten in plain tutorial voice in English, Spanish, French, Portuguese, Russian, and Chinese (`config/ftbquests/quests/lang/en_us.snbt` and `kubejs/assets/ftbquests/lang/`).

## [0.1.16]
- **End flashes stay in the End.** In the overworld, "End Flash rumbles" (`minecraft:weather.end_flash`) was playing every few seconds from a changing direction, with no portal in the world. Immersive Portals: Aeronautics 2.8.17 builds a background `ClientLevel` for every dimension at login, including `minecraft:the_end`, and `ClientWorldLoader.tickRemoteWorld` ticks it inside `withSwitchedWorld`, which points `Minecraft.level` at that hidden level for the tick. VanillaBackport 1.2.2's `ClientLevelMixin.vb$tick` then calls `EndFlashRenderer.tick` for every End client level, and that plays the sound on the shared sound manager. **End Flash Guard 1.0.0** (client only, `.pakku/client-overrides/mods/endflashguard-1.0.0.jar`) cancels that tick unless the level is the one remembered from a tick that was not switched, and it cancels `EndFlashRenderer.render` and `calculateLightIntensity` unless `Minecraft.level` is still that remembered level. A portal render swaps `Minecraft.level` without setting the switched flag, so the sky flash and the light change follow the same rule. The real End still flashes. The jar is not in the server mods folder. Remove it to revert. Source is `companion-mods/end-flash-guard`.
- Custom Voxy portal-LOD build **ipa10m** → **ipa10n** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10n.jar`, client and server). The client reports lod columns it already has, including ones it built locally, as `voxy:column_coverage`. Each packet is one 32×32 region, 128 bitmap bytes plus the two region coordinates. Hello is unchanged, and a peer without the new packet still streams. A chunk the server reloads is not put back by a later report. The ipa10m jar is removed.
- Custom Voxy portal-LOD build **ipa10l** → **ipa10m**. LOD columns start on the view-distance square instead of one chunk outside it. A noted chunk that is not on disk no longer holds the ring, so saved columns further out are still sent. Columns already given to that player are skipped before voxelizing, and those skips do not use the one-send tick budget. `server_terrain_sync_debug` in `config/voxy-config.json` is off by default; set it true for a summary every few seconds (ring, max radius, sent, skipped, client-reported columns, and why the walker is waiting). The ipa10l jar is removed.
- **A dropped levitite item no longer jitters in place.** `kubejs/startup_scripts/autisnautics_levitite.js` looks up `aeronautics:levitating` inside `ItemEvents.modification`, after registries exist, and removes it from `aeronautics:levitite` and `aeronautics:pearlescent_levitite` on both sides. Aeronautics' item-entity mixin floats a stack only when that component is present, on the client and the server. The 0.1.15 strip captured the component type while KubeJS was still starting. A client that had not registered it yet kept the default. The server sent an empty component patch, and the client put the component back from its own item prototype, so the client hovered while the server fell and position packets snapped the item back and forth. Levitite item entities also lose the component when they join a level, and on the client again before each tick. Motion and gravity are not written. End stone powder, music discs, and the cloud-skipper converter still have the component. A full game restart is needed for a startup script.

## [0.1.15]
- Custom Voxy portal-LOD build **ipa10k** → **ipa10l** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10l.jar`, client and server). Chunk loads no longer push LOD packets. The server records the chunk and the square-ring walker is the only sender, nearest ring first, including one chunk per tick while the server is over 40 ms. A failed send stays on that cell and is retried; after 32 failures the cell is skipped and logged once. A chunk that was cancelled after four nacks, four timeouts, or a status-2 ack is put back in the set. A retried packet goes to the front of that player's queue. The send caps are unchanged. The ipa10k jar is removed.
- **The spawn structure is editable in survival.** `config/serilum/starterstructure.json` sets `protectStructureBlocks` to `false`. Starter Structure placed the spawn island from `config/serilum/starterstructure/schematics/skyview_house.schem` and wrote every placed block to `world/data/starterstructure/protection/overworld/`. With protection on, a break in survival was cancelled on the server, so the block broke on the client and snapped back with no chat line, while `playersInCreativeModeIgnoreProtection = true` let creative players edit the same blocks. No claim is involved: Open Parties and Claims has no claims in the world (the `player-claims` files hold only empty per-player records for Server, Expiration, Phantomewzik, and validpoint, so the dark blue area on the map is not a claim either), FTB Chunks is not in the pack, and `spawn-protection` in `server.properties` is 0. `protectSpawnedEntities` stays true. The recorded block list stays on disk, and the mod does not read it while the option is off.
- **Levitite stacks fall again, including block drops.** `kubejs/startup_scripts/autisnautics_levitite.js` removes the `aeronautics:levitating` component from `aeronautics:levitite` and `aeronautics:pearlescent_levitite` in `ItemEvents.modification`, so a new stack has normal gravity and Aeronautics' item-entity mixin has nothing left to float. The 0.1.14 change left the component in place and relied on the mod's mixin. The pack's item-tick and item-join listeners stay removed, the block still drops `aeronautics:levitite`, and a full game restart is needed for a startup script.

## [0.1.14]
- **Levitite keeps Aeronautics' float.** 0.1.13 took the `aeronautics:levitating` component off levitite and pearlescent levitite. That part is undone. `kubejs/startup_scripts/autisnautics_levitite.js` still has no item-tick or item-join listener, so the pack does not cancel gravity or push the stack upward. The items keep the component Create Aeronautics registers, and that mod's item-entity mixin is the float. The block still drops `aeronautics:levitite`.
- **The grapple shoots 64 blocks.** In `config/adventurers_harness-server.toml`, `[grapple] range` is 64. That file is the pack `config` override, so the client and the server pack both get it. There is no second copy under server overrides or `defaultconfigs`. The fighting reach stays 4.5.

## [0.1.13]
- **Mined levitite drops as a normal item.** `kubejs/startup_scripts/autisnautics_levitite.js` no longer cancels gravity on the item entity or pushes the stack upward every tick, and levitite and pearlescent levitite lose the `aeronautics:levitating` component, so Aeronautics' item-entity mixin leaves gravity alone. The float had two sources and neither was a config switch. The levitite block still drops `aeronautics:levitite`, and a full game restart is needed for the startup script.
- **Analog Audio's two binaries ship with the pack.** AnalogLib downloads FFmpeg and yt-dlp into `.analoglib/bin` on the first cassette or radio, about 116 MB, and it rejects anything it finds there unless `ffmpeg --version` says 6.0 or newer and `yt-dlp --version` says 2026.07.04 or newer. `pakku.json` now lists `ffmpeg.exe` (9.0.1, gyan essentials build) and `yt-dlp.exe` (2026.08.19) under `client_overrides`, so they land in `overrides/.analoglib/bin/` in the CurseForge zip and the mod finds them before it looks at the network. Only the client zip carries them; the serverpack does not, because the client does the download and uploads the result to the server's file server. `.analoglib/` is still ignored apart from those two files, and both go to Git LFS. yt-dlp ages out when YouTube changes, so it needs a bump each release.
- **The Create: Deep Seas photosensitivity warning never shows.** The mod opens `DeepSeasSafetyScreen` over the title screen, the Photosensitivity Warning with its "I am photosensitive or epileptic" box, until `safetyScreenSeen` is set in `config/create_submarine_client_state.json`. The pack now ships that file with `safetyScreenSeen = true`. The warning is the only thing removed: `photosensitiveMode` and `alarmSteadyLight` stay false, so the Industrial Alarm keeps its coloured flashing light and still blinks while it sounds. `disableStartupScreens = true` already bypasses the same screen, and the state file covers the case where that is set back to false.

## [0.1.12]
- **Original vanilla and mod recipes work again.** KubeJS leaves them in place, and the pack's crafts stay as extra options, including `kubejs:brass_hand_gold`, `kubejs:stock_link_lead`, and `kubejs:portal_linker_machine`. Ore smelting and blasting, Create's andesite alloy and machines, Thermal alloys, Immersive Engineering hammer crushing, and Tinkers ore melting are back. Removals that fix dupes, pattern conflicts, and broken recipes stay: Quark's raw-ore iron and gold smelt, chainmail melted into steel, the Farmer's Delight knife clash with Rechiseled, slag glass and the slag brick, the JAOPCA wood and salt storage blocks, NuclearCraft grinding pearls into enderium dust, JAOPCA netherite scrap and molten coal, the duplicate steel nugget recipe, paper and bamboo rolling, and the duplicate autocannon copper sheet.
- **Spore infected spawn only on snowy land, and half as often.** `config/spore-startup.toml` is the pack `config` override, so the client and the server pack both get this file. There is no second copy under `defaultconfigs`. NeoForge 21.1.265 has the biome tag `c:is_snowy` (snowy plains, ice spikes, snowy taiga, grove, snowy slopes, jagged peaks, frozen peaks, and snowy beach, plus an optional `forge:is_snowy`). Spore matches each entry as a biome tag or a biome id, and the blacklist is checked first. The blacklist is `minecraft:deep_dark`, `c:is_ocean`, `c:is_deep_ocean`, `c:is_beach`, and `c:is_river`, so frozen oceans, frozen rivers, and snowy beaches stay empty. World-spawn weights are halved and rounded down, and the group sizes are the same: human 40 (2–5), drowned 5 (1–2), pillager 20 (1–3), villager 35 (1–3), player 10 (1–2), wanderer 12 (1–2), witch 12 (1–2), diseased villager 5 (1–2). Daytime spawning stays off. Laboratory structure spawns are unchanged.

## [0.1.11]
- **The client server list includes Invalid's Server.** Desired Servers 1.6.0 is client-only. On startup it appends `Invalid's Server` (`mc.invalserb.com`) when that name and address are not already in `servers.dat`. Other entries stay. Deleting that entry brings it back on the next launch. The list is `.pakku/client-overrides/config/desiredservers/desiredservers.json`, so the server pack does not get the mod or the file. A shipped `servers.dat` was not used: Default Options only copies one when the player has no list, and putting the file in client overrides would replace the whole list on update.
- **Pack server properties match the live server.** `.pakku/server-overrides/server.properties` is the copy the server pack installs. The 0.1.08 updater was writing that file over CT109, which put port 25569, `online-mode=true`, and `max-tick-time=60000` back. `server-port` and `query.port` are 25565, `online-mode` is false, and `max-tick-time` is 120000. `generate-structures` stays true.
- **No natural mobs within 128 blocks of overworld X=0, Z=0.** The old zone was 64 blocks around the shared world spawn. Live spawn is 62, 489, 41, about 74 blocks from the origin, so the starter island was outside that circle. Center and radius are the constants at the top of `kubejs/server_scripts/spawn_safezone.js`. Height is ignored. The other gap is the event. On NeoForge 21.1.265, `FinalizeSpawnEvent` (KubeJS `checkSpawn`) is posted for vanilla spawners and trial spawners. Natural spawns, chunk generation, phantoms, patrols, raids, and structure placement call `Mob.finalizeSpawn` and then `addFreshEntity` without posting it, and the natural spawner does not read `isSpawnCancelled()`. Those joins are now cancelled in the zone when the mob was not loaded from a chunk, is not Cobblemon, and its spawn type is not a spawner, trial spawner, spawn egg, command, dispenser, bucket, breeding, conversion, mob-summoned, or silverfish broken out of a block. A mob added with no spawn type is treated as natural. That covers mods that call `addFreshEntity` and skip `finalizeSpawn`. Cobblemon stays exempt, because wild spawns use `NATURAL` and cancelling that would stop pastures. Mobs already saved in the chunks are left in place.

## [0.1.10]
- **Create Confectionery is removed.** 1.1.3.b crashed the client at startup. Create's `RegisterClientExtensionsEvent` reported a duplicate client extension for `create:cardboard_helmet` from Registrate's ItemBuilder, and the only difference from a good launch was that jar. It is out of `pakku.json` and `pakku-lock.json`. `kubejs/server_scripts/recipes/create_confectionery.js` is deleted. The angel sugar tags no longer list `create_confectionery` items. The `c:milk` tag stays: `minecraft:milk` and `minecraft:flowing_milk` are still in it, so Create's chocolate and tea mixes can take vanilla milk.

## [0.1.09]
- **Create Confectionery 1.1.3.b** (`create-confectionery1.21.1_v1.1.3b.jar`, client and server). Locked in pakku on Modrinth and CurseForge, pinned so it stays on this file. The jar requires Create `[0,)`, NeoForge `[21.1.65,)`, and Minecraft `1.21.1`, which covers Create `1.21.1-6.0.10` and NeoForge `21.1.265`.
- **Angel blood sugar includes Create Confectionery sweets.** Item ids were read from the jar. Each one below has a food component, so sugar is granted when the use finishes. Light (+150): candy cane, marshmallow, gingerbread man, honey candy, and the four full chocolate bars (each bite is nutrition 2, and the bar has 16 durability). Sweet (+280): gingerbread, hot chocolate, soothing hot chocolate, marshmallow on a stick, the four chocolate and caramel bars, and the glazed marshmallows and berries. Rich (+500): the sixteen chocolate candies and caramelized marshmallows on a stick. Hot chocolate and soothing hot chocolate are drinks with a food component, so finishing the drink counts. Chocolate, caramel, gingerbread, and candy-cane blocks, and the chocolate and caramel buckets, are not food. Candy cane tools are left out. Gingerdough, cocoa powder, cocoa butter, and crushed cocoa are ingredients.
- **Milk mixes can use vanilla milk.** `c:milk` had no fluids, so the dark, white, ruby, and hot chocolate mixes, and Create's own chocolate and tea mixes, could not take milk. `kubejs/server_scripts/tags.js` adds `minecraft:milk` and `minecraft:flowing_milk`.
- **The candy cane mix requires a mundane potion.** The jar's old fluid ingredient dropped the potion components, so any `create:potion` matched. `kubejs/server_scripts/recipes/create_confectionery.js` replaces that mix: 250 mB of regular mundane potion, 2 sugar, and a slimeball, for 4 candy canes.

## [0.1.08]
- **Three backups, and the updater keeps three override copies.** Simple Backups `backupsToKeep` in `config/simplebackups-common.toml` is 3. That file is the pack `config` override, so a serverpack deploy does not put 10 back. `UPDATEFROMGITHUB.sh` (shipped from `.pakku/server-overrides/`) still writes `overrides_backup_<timestamp>` before it replaces config, kubejs, and mods, then deletes older copies of those directories until three remain. `preserved_local_*` is left alone.
- **Angel sugar is granted when the item is used up.** Sugary food was listening for `food_eaten`. NeoOrigins 2.2.38 fires that on `LivingEntityUseItemEvent.Start`, so a right-click added blood sugar before the bite finished and letting go left the item in hand. `sugar_replenishment_food` now listens for `food_finished`, which fires when a food use completes and the item is consumed. Light, sweet, and rich amounts stay 150, 280, and 500. Raw sugar and the Supplementaries sugar cube are not foods: the use still consumes one item and the cooldown is 10 ticks. The `add_sugar` command is still the debug refill.
- **No natural mobs within 64 blocks of overworld world spawn.** `kubejs/server_scripts/spawn_safezone.js` cancels `FinalizeSpawnEvent` inside 64 blocks horizontally of the overworld shared spawn (`/setworldspawn`), ignoring height. That covers natural, chunk generation, structure, patrol, raid/event, jockey, and reinforcement. Spawners, trial spawners, spawn eggs, commands, dispensers, buckets, breeding, conversion, mob-summoned, and silverfish broken out of a block still spawn. Cobblemon entities are exempt, because wild Cobblemon calls `finalizeSpawn` with `NATURAL` and a blanket cancel would also stop pastures. Mobs already in the world are not removed. Other dimensions, including the Deepsea, are unchanged.

## [0.1.07]
- **Angel sugar withdrawal clears when blood sugar comes back.** Running out of blood sugar still applies withdrawal, and eating so sugar is above 0 now removes it on the next tick, including after a relog or a death. The old power was `origins:stacking_status_effect`. NeoOrigins 2.2.38 turns that into `neoorigins:persistent_effect` and moves the sugar check onto a power gate, so the effects are applied at infinite duration and the power stops ticking once sugar rises, which left the slow and the FOV zoom on. `sugar_withdrawal` is now a native persistent effect with `toggleable: false` and the `sugar <= 0` check as its own condition, so it keeps ticking and clears the effects as soon as sugar is above 0. The same stuck-effect path is fixed on Featherlight (Slowness II while wearing armor heavier than chainmail) and Empty Mask (weakness, fragility, and grievous while the head slot is filled). Flight levitation was already a native persistent effect and still drops when the toggle turns off.
- **Sugar withdrawal slow is Slowness II.** The slowness amplifier is 1. Weakness and mining fatigue stay at amplifier 4, and hunger stays at amplifier 0.

## [0.1.06]
- **Origins pass for the 13 AutisOrigins.** Bundled NeoOrigins origins and classes stay off in `config/neoorigins/content.toml`. Broken power references now point at things this pack loads: Forge reach, gravity, and step height are the 1.21.1 `minecraft:generic` and `minecraft:player` attributes; Modern Foundry (the Tinkers port) keeps bounce, magnetic, jump boost, extra jump, mining speed, ender slime balls, and cheese under `modernfoundry:`; the martian raptor is `stellaris:martian_raptor`; NuclearCraft sodium and the feral ghoul use `nuclearcraftneohaul`; thermal elementals, the rats plague effect, and the rats mask items are gone. Accursed is no longer only a quest hook: Spite (+20% damage dealt), Damned Endurance (+4 max health), and Grave Sight, with Cursed Hunger (1.4× exhaustion) and Brittle Soul (+25% magic damage taken). The hidden quest callback still completes `22107772A91372B0` and still does not give the ring. Duckborn lays an egg on waking in daytime (`autisorigins:lay_eggs`); webbed feet add swim speed and cost 10% land speed; undead hatred is +25% dealt and taken against undead. Khajiit pounce and scare creepers are real powers. Hedgehogs no longer spend hunger while sprinting, and not sprinting still burns extra. Blobbians gain no experience. The snail shell is +2 armor and some knockback resistance, and still only accepts a chestpiece. Snail salt is Galosphere pink salt plus real NuclearCraft NeoHaul sodium items and fluids. Ether disease reads the NeoOrigins origin attachment, so mutations can grant and revoke again. The alchemist icon is `createbigcannons:gas_mask`. Angel flight is unchanged: it still starts off, and NeoOrigins Skill 1 is still F6.
- **Angel blood sugar comes from sugary food.** Eating uses four disjoint item tags under `autisorigins:sugary_foods`. Raw sugar and the Supplementaries sugar cube, used from the hand, add 80. Cookies, sweet berries, Cobblemon berries, and small candy (`autisorigins:sugary_light`, 108 items) add 150. Candy, jam, jello, soda, pie slices, chocolate, honey, and mochi (`autisorigins:sugary_sweet`, 138 items) add 280. Whole cakes, pies, and the pancake (`autisorigins:sugary_rich`, 20 items) add 500. Flight still spends 10 sugar every 5 ticks out of a 1000 pool, so a cake is about 12 seconds and a cookie is about 4. Glow berries, Cobblemon EV and exp candies, savory pies, and blaze cakes are not in the tags.
- **Autist is the vanilla origin.** The five powers added in the origins pass (Brown Brick Affinity, Configurator's Eye, Pack Stamina, Sedentary, Analysis Paralysis) are removed. The only power is the canned-food start item. The description says this origin has no extra powers and no drawbacks, and plays like normal Minecraft.

## [0.1.05]
- **Portal render guard 1.0.1** (client only, `.pakku/client-overrides/mods/portalrenderguard-1.0.1.jar`, the 1.0.0 jar is removed). The 1.0.0 restore wrote raw GL after `drawPortalAreaWithFramebuffer` had already updated GlStateManager. That copy calls `_depthMask(true)`, `_colorMask`, `_enableDepthTest`, and `_viewport`, and those setters skip the driver call when the cache already matches. A translucent pass leaves the depth mask false. The copy sets the cache to true, the raw restore put the driver back to false, and the next pass that asked for a true depth mask did not call `glDepthMask`. 1.0.1 puts the saved state back through GlStateManager and flips each cached value first, so the cache and the driver both land on what was saved. The portal clip shift is gone. `activeClipEquation` is `FrontClipping.getActiveClipPlaneEquationBeforeModelView`, which is `n·(x - camera) + c`, and `blit_texture_depth_cutout.frag` unprojects with `inverse(projection * modelView)` where that modelView is Sodium's camera pose, so the point is already in that space. 1.0.0 looked up `Minecraft.gameRenderer()`; that member is a field (`public final GameRenderer gameRenderer`), so the shift never ran. Applying it would move the plane off the portal. `compatibilityRenderMode` stays true. The jar stays in client-overrides, not the server mods folder. Remove the jar to revert. Source is `companion-mods/portal-render-guard`.

## [0.1.04]
- **Portal Linker 1.2.0** (`.pakku/overrides/mods/portallinker-1.2.0.jar`, the 1.1.0 jar is removed). The linker craft is a column: crying obsidian, an enderium machine, an eye of ender. The machine is consumed. That is the chapter 3 ender-age gate, an ender casing around an abstruse mechanism (`kubejs/server_scripts/recipes/chapters/chapter_3.js`), and the abstruse mechanism is the heated mix of an inductive mechanism, an ender pearl, and chorus fruit just above it. `kubejs/server_scripts/recipes/portal_linker.js` has `GATE` true and re-adds that column with the machine item. The jar recipe uses the tag `portal_linker:linker_gate`, whose only entry is `kubejs:enderium_machine`. The old crying-obsidian / eye / crying-obsidian column no longer crafts. There is no Portal Linker quest, so no quest text changed.
- **Frames take four diamond blocks.** The corners are diamond blocks (`frames.cornerBlock`, tag `portal_linker:portal_corners`). The other frame blocks stay crying obsidian (`frames.frameBlock`). One frame is 4 diamond blocks at every size: 2×3 is 4 diamond and 10 crying obsidian, 5×5 is 4 and 20, 32×32 is 4 and 128. A full diamond ring at 32×32 would be 132 diamond blocks, so the corners are the diamond cost. A link already saved under 1.1.0 keeps working on crying obsidian until a frame block is broken. New crying-obsidian frames are refused. Saved data from 1.1.0 still loads.

## [0.1.03]
- **Portal Linker 1.1.0** (`.pakku/overrides/mods/portallinker-1.1.0.jar`, the 1.0.0 jar is removed). Two standing frames were coming out upside down. Immersive Portals' positive-X basis puts world-up on the portal's width axis, and `adjustRotationToConnect` then turns 180° around that horizontal axis, so an east-west frame rolls the view and the controls. South-facing frames happened to use world-up and only yawed. Vertical frames now keep world-up as height, flat frames use a fixed basis whose front is up, and the link uses Immersive Portals' own `computeDeltaTransformation` (a yaw when both frames stand). A standing frame linked to a flat one also turns 180° about world up, so walking off the floor into the standing frame stays upright. The default exit is upright, out the front of the destination, and the turn matches the two facings. A 3×5 still matches a 5×3, and nothing is scaled. **The 1.1.0 jar is required on clients.** The upright-link fix and the exit picker are in that jar; a client still on 1.0.0 will not match the server.
- **The linker picks the exit.** Sneak-scroll (scroll up next, scroll down previous) sets the mode for the next link and does not move the hotbar. The action bar and the tooltip say which: out the front, upright; out the back, upright; rolled 90°; rolled 180°; rolled 270°; flipped. Sneak-use on a linked frame cycles that link. Sneak-use in the air, or on a block that is not a link, still forgets the stored frame and keeps the mode on the item. The mode is stored on the link in the overworld saved data. Links from 1.0.0 come back as out the front once their portals load. Ships stay refused, and claims are unchanged.
- **Terrain next to a portal stays drawn.** `compatibilityRenderMode` in `config/immersive_portals.json` is false → true. The stencil portal renderer paints a full-screen fog triangle while drawing the other side, and it only stays inside the portal if the stencil test is still on. Voxy's LOD pass (`AbstractRenderPipeline.finish` and `transformBlitDepth`) turns that test off during the solid chunk pass, which is earlier in the frame, and Immersive Portals never turns it back on before the triangle. With the test off, the triangle covers the whole screen, including blocks next to the player, in any look direction that actually renders a portal. Compatibility mode draws the portal view into its own framebuffer and copies that onto the portal. `maxPortalLayer` is already 1, so this does not give up a nested view the pack was rendering. Sodium occlusion culling, fog occlusion, block-face culling, and the no-error GL context are unchanged. Set `compatibilityRenderMode` back to false if a portal view itself is wrong.
- **Angel flight turns off when the sugar runs out.** NeoOrigins 2.2.38 treats a missing `active` field as on, so Envy of Flight stayed in levitation after the sugar hit zero. The toggle now starts inactive. Sugar hitting zero, or sneaking, turns it off and drops the effect. **Bind NeoOrigins Skill 1 to F6.** That is the pack default for a new client (`key.neoorigins.skill_1` in `config/defaultoptions/keybindings.txt`), and it is the key Envy of Flight uses. An existing client keeps its old `options.txt`, so that bind has to be set by hand.
- **Overworld structures no longer generate, except the stronghold that reaches the End.** Structurify disables the vanilla overworld sets (villages, outposts, mansions, mineshafts, ancient cities, trial chambers, trail ruins, temples, monuments, shipwrecks, ruined portals, and the rest) and the modded ones that place in overworld biomes (Terralith, Modern Foundry's overworld islands, Cobblemon habitats and ruins, Explorify, Alex's Caves, Spore, and the others). YUNG's Better Strongholds stays on: that mod already blocks the vanilla stronghold type, and eyes of ender locate `betterstrongholds:stronghold`. Nether and End structures are unchanged, including the nether ruined portal and Modern Foundry's nether and end islands. `autisnautics:wafer_maze` and the molten calderas are unchanged. `generate-structures` stays true. Stellaris meteors are limited to moon biomes.

## [0.1.02]
- **First join gives a small kit.** `kubejs/server_scripts/startingitems.js` still gives the quest book once, under `received_starting_items`, and now also 10 Poke Balls, 16 coal blocks, and an Iron Adventurer's Harness.

## [0.1.01]
- **Abyss Creatures 0.2.4** (`.pakku/overrides/mods/abysscreatures-0.2.4-neoforge-1.21.1.jar`, both sides). The 0.2.3 jar is removed. Corpse anglers, ghidruth, and siamserpents were circling one point and not attacking. Most hostiles had no pursuit goal, only a short-range attack, and `NearestAttackableTargetGoal` only grew the search box by 4 blocks on Y, so a player above or below was missed. Idle `WaterStrollGoal` stops picking a new point once `noActionTime` passes 100, the last swim node stays, and the 0.2.3 heading hold circles that node. Schooling fish with no target cohered on a stationary centre and did the same. 0.2.4 rebuilds the decision and the swim. A hunt scans survival players who are in the water, inside the mob's follow range in 3D, with line of sight, every 10 ticks, and drops them when they leave the water, go creative, or leave range. A pursue goal steers until an existing attack is in range. Those attack goals, their animations, timings, damage, and effects are the ones already in the jar: gnasher bite, corpse angler dash, necroshell claw, fulgastra split, splitted fulgastra shock, mutavore tongue, ghidruth bite, tail, and charge, siamserpent slash, beam, and shot, and the spine worm grab. Gloomfish and lithoshrimp stay ambient. Splitted fulgastra still shocks and returns instead of chasing. The spine worm keeps its anchor and its reach chain and is not given the free pursue. Siamserpent and spine worm chains still step one block apart. Ghidruth is still one entity. The 0.2.3 heading gate stays in `Course`. `config/servercore/config.yml` and `config/doespotatotick-common.toml` are not edited.
- **Corpse angler lure.** In min01's Lovecraft: Beyond the Abyss (`1.20.1`, `CorpseAnglerEntity`), a natural spawn walks to the ground and buries once three solid blocks are under it, with the angler left out. There is no right-click. `hurt` starts the unburrow only while the burrow animation (state 3) is playing and the player's attack ray clips the model part `Bait` (the bulb on the illicium, `CorpseAnglerModel`). `CorpseAnglerAmbushGoal` then fires when a target is already set and within 2.5 blocks of the `Gnasher` model point. This port has no part builder, and the buried pose is the `BURROW` flag rather than animation state 3 (here unburrow is state 3 and the dash is state 1). The lure is a box in front of and above the body. A player attack whose look hits that box unburrows for the whole time the mob is buried. Standing within 2.5 blocks of the same box is the ambush. A body hit that misses the box still hurts and leaves it buried. While buried it holds position and does not roam. The dash after either trigger is the existing `DashGoal`.

## [0.1.00]
- **ServerCore activation range loads again.** `config/servercore/config.yml` named `minecraft:oak_boat` and the other per-wood boats and rafts. On 1.21.1 those are items. The boat entities are `minecraft:boat` and `minecraft:chest_boat`. ServerCore 1.5.19 rejects the file on an unknown id and falls back to its defaults, so the activation-range settings never applied. The list now uses the two real boat entities. The other entity ids and the Spore and Friends & Foes mob categories were checked against 1.21.1 and the pinned jars.
- **FTB Quests: machine quests are titled for the machine.** The Factory Guide node that asked for a Brass Machine is titled Brass Machine. The same pass retitles the Gold Machine, Zinc Machine, Lead Machines, Light Engineering, the controller, the enderium machine, the harvester, the stockpile switch, and the blaze cake, and drops the descriptions that talked about the quest instead of the item.
- **FTB Quests: descriptions say what the thing is for.** The repeated JEI, Ponder, and Akashic lines are gone from the primers and the Factory Guide. Each quest now says what the item or machine is and where it sits in the pack. The Create primer is laid out in lanes: materials and shafts at the start, then the press and the sheets, the andesite machines, copper and the electron tube, precision and the brass machines, trains, and logistics off on their own.
- **Mob AI throttling is loosened, and water mobs keep swimming.** ServerCore activation range stays on in `config/servercore/config.yml`. Full-speed range is animals and villagers 16→32 blocks, zombies 16→48, other monsters 32→48, the modded catch-all 24→40, and raiders and flying monsters 48→64. Outside that, inactive mobs tick every 10 ticks instead of 20, and animals wake every 30 seconds instead of 60. The default type, which is mostly items, moves 16→24 and still ticks every 20. Dynamic scaling still caps at 10 chunks and 100% mobcap, but it waits until 45 MSPT (was 35) and its floors are 6 chunks and 60% mobcap (were 2 chunks and 30%). Breeding caps stay at 32 within 64 blocks. Vertical activation range stays off, because this pack is tall. DoesPotatoTick stays on in `config/doespotatotick-common.toml`: horizontal tick distance 4→6 chunks and vertical 2→4. The ServerCore `water` activation type is excluded (`activation-range` -1, `tick-interval` 1), which is how 1.5.19 always-ticks every `WaterAnimal`: vanilla fish, squid, glow squid, dolphins, tadpoles, Aquaculture fish, Undergarden gwib and gwibling, the Deeper and Darker angler fish, and the Alex's Mobs and Alex's Caves fish that extend `WaterAnimal`. Abyss Creatures are `PathfinderMob` and `isPushedByFluid` is false, so the thirteen `abysscreatures` entity ids (gloomfish, lithoshrimp, gnasher, corpse angler, necroshell, fulgastra, splitted fulgastra, mutavore, spine worm head and body, siamserpent head and bone, ghidruth) are on the activation-range exclusion list, and the mod id `abysscreatures` is on DoesPotatoTick's always-tick mod list, with `aquaculture`. The same lists name the other swimmers that are not `WaterAnimal`: axolotl, turtle, guardian, elder guardian, Alex's Mobs whales, crocs, seals, anacondas and their parts, bone serpents, laviathans, skelewags, and the rest of that fluid set, Alex's Caves deep ones, mine guardian and its anchor, submarine and boats, Friends & Foes mauler, crab, and barnacle, the Quark crab and boats, and the Undergarden gloomper. Lithium's only set flag is `mixin.ai.poi=false`, which is not a distance throttle. Entity Culling is client-only in this pack. Let Me Despawn only changes whether a mob stays persistent.
- **Cobblemon wild density is halved again.** `config/cobblemon/main.json` `pokemonPerChunk` goes from 0.5 to 0.25. Commit `4ae2cdbb` already took it from 1.0 to 0.5 and `maximumSpawnsPerPass` from 8 to 4. The per-chunk cap is the population limit, so only that knob moves. Attempt interval, zone size, spacing, and spawns per pass stay where they are, so the rate is not halved a second time on top of the cap.

## [0.0.99]
- **Abyss Creatures 0.2.3** (`.pakku/overrides/mods/abysscreatures-0.2.3-neoforge-1.21.1.jar`, both sides). Schooling and swimming mobs were spinning in place. `BoidMoveControl.tick` and `SwimMoveControl.tick` took yaw from `atan2` whenever the steering vector was longer than `2.5e-7` in 3D, then copied that yaw onto the body and the head. A mostly vertical vector, fish stacked on the same spot, and a fish held in a water column all make the horizontal part noise, so the heading walked around at the turn rate. Yaw now comes from a smoothed horizontal direction and is left alone when that direction is shorter than `1e-4` or is a gap of under a block that is shorter than the vertical gap. The head is no longer overwritten by the move control. Neighbours closer than 0.35 blocks separate with a fixed push instead of a nudge scaled by the tiny gap. The movement runs on the logical server; the same jar still has to be on the client.
- Custom Voxy portal-LOD build **ipa10j** → **ipa10k** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10k.jar`, client and server). Dedicated-server LOD streaming starts just outside each player's own view distance (clamped to the server view distance) and walks outward in a square ring, recentring when they move. Elytra, fast travel, teleports, and dimension changes pause that walk and drop queued packets that no longer apply. The shared send cap is 32 sections and 1 MiB per tick, with the same section cap per player. `server_terrain_sync_broadcast` is on, so the server streams those LODs. The near-ring dedup set is large enough that a dozen players do not immediately resend columns
- **Portal Linker** (`portal_linker` 1.0.0, `.pakku/overrides/mods/portallinker-1.0.0.jar`). A crying-obsidian frame (tag `portal_linker:portal_frames`, or `frameBlock` in `config/portal_linker-server.toml`) from 1×2 up to 32×32 interior, corners included, on any axis including flat. The Portal Linker item stores the first frame and links the second into a bi-way, bi-faced pair of breakable Immersive Portals. The two openings have to be the same size (a 3×5 matches a 5×3); nothing is scaled. Breaking any frame block removes both ends, including after a restart. Ship frames are refused. Open Parties and Claims is checked when that mod is loaded. The craft is ungated; set `GATE` in `kubejs/server_scripts/recipes/portal_linker.js` to take `portal_linker:portal_linker` out of the book. Catalyst cost is off.

## [0.0.98]
- **FTB Quests: Let it Walk has a primer.** Nineteen quests run from Klaxium, mixed in a Mechanical Mixer, out to the Walker Core. Sheets need the press, spikes need the saw, articulation and jet nozzles need the deployer, and the stabilization module needs a precision mechanism. Pilot controls need an electron tube. The chapter sits with the other Create primers. The mod's own recipes are unchanged by KubeJS.
- **FTB Quests: the Create primer follows the pack's recipes.** Sheets come from the press. Whisks, propellers, drill heads, and saw blades come from iron sheets. The fan, mixer, drill, and saw are Andesite Machines applied to those parts. The brass hand is a golden sheet, not a brass plate. Railway casings are a golden sheet and deepslate. Train controls, stations, signals, and schedules are Gold Machines and also wait on a reinforced mechanism, which is obsidian deployed and pressed onto a kinetic mechanism. Electron tubes are a spout of molten iron onto polished rose quartz. Precision mechanisms deploy two of those tubes. Brass machines wait on that mechanism and the Factory Guide brass machine. Logistics waits on the lead machine. Sturdy sheets stay out, because that recipe is removed. Crafts and Additions connectors depend on the zinc machine, and only the electric motor also needs the spool. The rolling mill needs a shaft and an Andesite Machine.

## [0.0.97]
- **FTB Quests: primer chapters fan out instead of stacking in one line.** Dependencies follow the real branches (Create kinetics, machines, trains, and logistics; Refined Storage disks, autocrafting, and wireless; and the same idea in the other primers). Entry quests sit at the centre. Branched chapters are radial. Astral Sorcery, NuclearCraft, and the Undergarden, which stay mostly one path, spiral outward. Nodes stay at least a slot and a half apart.

## [0.0.96]
- **FTB Quests: primer capstones were checked against the mod jars.** Create gains train track and the lead-machine logistics line, ending at the redstone requester. Refined Storage gains the disk ladder through 64k, importers and exporters, external storage, the pattern grid, and the wireless grid. Tide gains the smithing rods through netherite and the Midas rod. Cobblemon gains great, ultra, and master balls. Sophisticated Backpacks and Storage gain the stack, feeding, magnet, void, and tank upgrades the pack does not hide. Psi gains the psimetal ingot, the top CAD parts, and the exosuit. Immersive Engineering gains the steel component and the electronic components. Modern Foundry's tool station is the Tinker's Anvil, then the scorched anvil.
- **FTB Quests: the remaining primers now run to each mod's last machine.** Immersive Engineering ends at the arc furnace and the excavator, with the dynamo, water wheel, windmill, and thermoelectric generator left out and wires pressed rather than sawn. Immersive Petroleum ends at the distillation tower. The gas generator stays removed. Refined Storage ends at the disk drive: fluix casings, a controller that needs a calculation mechanism, and processors only from the wafer line. NuclearCraft ends at the solid-fuel fission reactor and the turbine. The voltaic pile stays removed. Modern Foundry, Ars Nouveau, Iron's Spells, Theurgy, Astral Sorcery, both Alex mods, Cobblemon, and the Undergarden each end on their own capstone.
- **FTB Quests: the Create primers now follow this pack's machines.** Create runs from the cogwheel out to the mechanical arm. Fans, press, mixer, drill, and saw are Andesite Machine applications. Copper, gold, and brass machines wait on those Factory Guide quests. The deployer waits on a brass hand. Crafts and Additions ends at the modular accumulator. The alternator and capacitor stay removed, and the tesla coil is a Zinc Machine on a lightning rod. Diesel ends at the huge engine. The lava ferment and the commented diesel script are not the live path. Enchantment Industry ends at the blaze enchanter, and the printer stays a Zinc Machine on a lapis block. Aeronautics ends at the smart propeller. Levitite mines with iron.
- **FTB Quests: the first primer questlines now run to each mod's last craft.** Farmer's Delight ends at the honey glazed ham, with bark, straw, canvas, compost, rich soil, the cabinet, the basket, and the rice bag along the way. Sophisticated Backpacks ends at the netherite backpack and Sophisticated Storage at the netherite chest, the controller, and the storage link. Copper storage stays hidden. Tide ends at the obsidian crate. ComputerCraft ends at the advanced turtle, and the digital adapter waits on the Factory Guide brass machine. Psi ends at ebony psimetal, built through the CAD assembler.
- **FTB Quests: rewards now follow the task.** The flat per-chapter stacks (16 andesite, 8 copper, 16 torches, and the rest) are replaced. Each primer and Factory Guide quest gives an ingredient, consumable, or complementary item for that task, with XP scaled by stage and whether the node is a hex. Books, tools, and builder's tea stay. No two of those quests share the same set of reward items. New rewards are not mechanisms, tier machines, processors, the Refined Storage controller, or a tesla coil.
- **FTB Quests: Factory Guide rewards stay on the quest, and the brass shortcut is gone.** Overview's brass-machinery node pays crushed raw zinc, so the alloy step stays in front of you. Brass ingots are off the precision-mechanism nodes and the numismatics cog. The Stage 2 brass-machine bonus that paid quartz-enriched iron now pays iron. Nether Quartz pays redstone and a little rose quartz. Gunpowder pays sulfur dust and saltpeter. Colored wool, carpet, and dye that were only there to make two rewards differ are replaced with an ingredient from that quest. Across the primers and the Factory Guide, 427 quests have 427 distinct reward item sets. Tesla coils, processors, rockets, and launch pads were not among the rewards.

## [0.0.95]
- **FTB Quests: "Crafts & Additions" was showing as invalid formatting.** Quest text treats `&` as a format code, and `&` followed by a space throws `Invalid formatting! You must escape whitespace after & with \&!`. The chapter title and the spool, small-connector, and rolling-mill lines now store the ampersand as `\&`. The other primer and Factory Guide strings were checked for the same `&`, `{substitute}`, and JSON-looking pitfalls. Barbed wire's own text was already fine.

## [0.0.94]
- **FTB Quests: Mod Primer text was written, but the game never showed it.** FTB Quests 2101.1.38 reads quest ids with `Long.parseLong(hex)`. Every primer id started with `E`, which is above `Long.MAX_VALUE`, so the loader threw, assigned a random id, and the `lang/en_us.snbt` keys no longer matched the nodes. Those ids are remapped by subtracting 8 from the first hex digit (`E` → `6`), including the chapter group. Titles, subtitles, and directions now load. Pack-specific steps are corrected where the old copy described a removed craft: andesite alloy is the 2x2 of algal bricks and andesite (the mixer is not the first alloy), Crafts & Additions connectors are Zinc Machine stonecuts, the steam vent is a Brass Machine cut, the propeller bearing is a Gold Machine cut, the spool is `createaddition:copper_spool`, and Blaze's Enchanting Handbook is a template, not a book you read. Two Create leaves were added: Algal Brick and Andesite Casing.
- **Quest rewards on the primers and the Factory Guide.** The 8-iron-nugget placeholder on primer quests is now a small stack of a relevant material (andesite, wool, copper, charcoal, clay, quartz, bread, torches, and so on) plus XP. Book and tool rewards were kept, with XP added. Factory Guide quests that had no reward (Overview, Stages 1–5, Stage 4b, Taking Flight) now give a stage-scaled item and XP. Gate items are not handed out: no mechanisms, no tier machines, no Refined Storage controller or processors, no tesla coil. Quests on that line that had no title or description (the wafer line, the reagent jars, the flight page, the orbit markers) now have both. Decorative `shape: none` nodes with no tasks were left alone.
- Custom Voxy portal-LOD build **ipa10i** → **ipa10j** (`.pakku/overrides/mods/voxy-0.2.17-alpha-sodium08-portal-lods-ipa10j.jar`, client and server). Model baking no longer crashes when a Regions Unexplored grass tint asks Voxy's fake world for a colour and the biome or colour resolver is missing: the tint falls back to plains, and a throwing colour provider is logged once per block and captured as no tint. An interrupted frame no longer leaves a timing sampler open, so the follow-up crash in `TimingStatistics` does not fire. Portal LOD behaviour is unchanged

## [0.0.93]
- **Akashic Tome no longer saves a book that contains itself.** `kubejs/server_scripts/universal_tome.js` drops a self-reference in `tool_content` and strips nested `tool_content` from stored books, on the player tick after a morph and when a tome is dropped. A cyclic stack was overflowing `ToolContentComponent.hashCode` and the player/slot encoder. A flat crafted or prefilled tome is left as it is.

## [0.0.92]
- FTB Quests: Mod Primers chapter group, one beginner chapter per major content mod. The first quest of each chapter gives that mod's guidebook, or the Ponder/JEI stand-in when the mod has no book. Starter-book configs stay off (Tide journal, Alex's Mobs giveBookOnStartup, Ars Nouveau spawnBook, NuclearCraft give_guidebook).
- **ServerCore mob scaling is on.** `dynamic`, `activation-range`, and `breeding-cap` are enabled in `config/servercore/config.yml`. Dynamic keeps the existing target of 35 MSPT and the existing ranges (chunk-tick and view/simulation 10 down to 2, mobcap percent 100 down to 30). Breeding caps stay at 32 villagers and 32 same-type animals within 64 blocks. Activation range stays horizontal (`use-vertical-range` remains false). Its exclusion list now names item frames, armor stands, boats, rafts, minecarts, rideable mobs, Create contraptions and seats, Aeronautics/Offroad/Simulated movers, Immersive Aircraft, Create Big Cannons carriages and pitch contraptions, and Cobblemon pokemon, boats, bobbers, and NPCs. Players were already exempt in ServerCore 1.5.19
- **DoesPotatoTick always-ticks the mods whose entities must keep moving.** `EntitiesOfTheModsThatWillAlwaysTick` adds `cobblemon`, `spore`, `aeronautics`, `simulated`, `offroad`, `sable`, `octolegs`, and `immersive_aircraft`. Sable ships are not entity types, and the Octo Legs override jar does not publish entity ids this config can name; the mod-id list is what keeps those namespaces ticking
- **Every loaded dimension gets the 50 km border.** `kubejs/server_scripts/worldborder_50km.js` walks all server levels, including ones loaded later, and sets diameter 50000 centered at 0,0 in that dimension's own block coordinates. Vanilla 1.21.1 only scales the border center, not the size, so the Nether stays ±25000 without multiplying by 8. One log line per dimension
- **Repeating data errors that live in pack data are fixed.** Blueprint's `modid:example` dimension data map and Create Enchantment Industry's `reliquary:xp_juice_still` fluid entry are filtered in Autisnautics Compat Filters. The experience map is replaced with Reliquary 2.0.85's fluid id `reliquary:xp_still` (20 units, still gated on Reliquary being loaded). Cobblemon 1.8.4 has a `water` spawn preset and no `underwater` or `illager_structures` presets: tympole and the submerged palpitoad now use `water`, and Audit Fixes adds the 1.8.0 illager-structure preset (pillager outpost, swamp hut, mansion, min Y 62) so Galarian Koffing and Weezing resolve. Accessories compat `addPocketSlot` is on, which is the switch that registers the generic `curio` slot as Accessories' pocket. NuclearCraft's Geiger counter and radiation badge are the items that asked for that slot
- **Paxi loads Autisnautics2 and Autisnautics Wafer.** Autisnautics2 (Regions Unexplored / Better Caves height shifts and the other misc fixes) loads after Skylands Over the Sea and before Autisnautics Density Ores. Autisnautics Wafer (the chapter-4 `autisnautics:wafer` dimension, coordinate scale 1) loads after Autisnautics Abyssal Stack. Compat Filters and Audit Fixes stay last. None of these packs share a data path
- **Almost Unified no longer prefers AE2.** `ae2` is removed from `mod_priorities` in `config/almostunified/unification/materials.json`

## [0.0.91]
- **Abyss Creatures 0.2.2** (private Deepsea adaptation of min01's Lovecraft: Beyond the Abyss creatures, GPL-3.0) restored to the pack via overrides (`.pakku/overrides/mods/abysscreatures-0.2.2-neoforge-1.21.1.jar`). Natural Ghidruth now despawn and free the dimension cap when no players are in Deepsea; summoned, egg, and name-tagged ones persist

## [0.0.90]
- **Adventurer's Harness 1.2 and Create: Adventurer's Harness 1.1** are now Pakku-managed CurseForge projects (`adventurers-harness` 1780287, `create-adventurers-harness` 1780307) instead of hand-shipped LFS override jars, so `pakku update` keeps them current. The old override jars (`adventurers_harness-1.1-NeoForge-1.21.1.jar`, `create_adventurers_harness-1.0-NeoForge-1.21.1.jar`) are removed from `.pakku/overrides/mods/`; the lockfile carries both new projects (the Create addon pulls in the base mod as a dependency) and the serverpack ships `adventurers_harness-1.2-Sable-NeoForge-1.21.1.jar` plus `create_adventurers_harness-1.1-NeoForge-1.21.1.jar`
- **Pakku updates (16)**: Collective 8.86, Corpse x Curios API Compat 4.2.1, Create Control Panels 0.2.4, CreativeCore 2.13.54, EMF Compat: Iron's Arms 'n Artifice 1.1.0, FancyMenu 3.10.0, JAOPCA 5.0.15.24, Let it Walk 0.1.5, Not Enough Recipe Book 0.6.2, Reliquary Reincarnations 2.0.85.1599, Sophisticated Core 1.5.33 / Storage 1.6.17, Starter Structure 6.0, TIS-3D 2.0.2, Titanium, Vista 5.6.8
- **Advanced Hook Launchers removed**: the project is cut from `pakku-lock.json` so Pakku cannot reinstall it, along with `config/adhooks-common.toml`, its keybinding lines in `config/defaultoptions/keybindings.txt`, the `starter_hooks` reward table, and the quest reference in `generic.snbt`. Nothing else required the launchers; ForgeEndertech stays
- **Create Deep Seas pressure disabled**: since the mod has no pressure-off switch, implosion, diver crush, and crack chance are all off and the depth cap is maxed in `config/create_submarine-server.toml`
- **Quest cleanup**: the Tips and Tricks branch is gone — the `lifesavers` and `metallurgy` chapters and their chapter group are deleted, with their lang keys in `config/ftbquests/quests/lang/en_us.snbt` and `kubejs/assets/ftbquests/lang/en_us.json`. The Calculation wafer line is linked from `stage_4.snbt`. Starter books are disabled (Tide, Alex's Mobs `giveBookOnStartup`, Ars Nouveau `spawnBook`, NuclearCraft guide)

## [0.0.89]
- Server-side `llmchat` mod: type `@bot <question>` in chat and the answer is broadcast to everyone. Any OpenAI-compatible endpoint works; the stack currently points at DeepSeek `deepseek-flash`. Hand-shipped jar `.pakku/overrides/mods/llmchat-1.0.0.jar`, config `config/llmchat-common.toml`, notes in `docs/llmchat.md`. The API key is deliberately not in the pack: `config/llmchat-common.toml` has no key field at all. The mod reads `LLMCHAT_API_KEY` from the server environment (systemd `EnvironmentFile=/etc/llmchat/env`, mode 600) or a key file outside the server dir, warns if that file is world-readable or inside the server folder, and only logs the last four characters. Nothing key-shaped ships in this repo or the server pack
- **Skin Restorer** (Suiranoil, MIT) applies player skins from a Minecraft username, Ely.by username, or any image URL at runtime (Steve/Alex models), on both sides — so skins work on the offline-mode server. Added via Pakku (`skinrestorer`, NeoForge 1.21.1)
- **Create: Lightworks 1.0.1** (`createlightworks-neoforge-1.21.1-1.0.1.jar`, CurseForge 1785136) adds functional Create lights that keep working on moving contraptions and vehicles: Searchlight and Floodlight with pan/tilt and Pan-Tilt Head mounting, Rotating Beacon, Warning Lights, Edge Lights, and vehicle Headlights plus Tail/Brake Lights. Dyeable, redstone-dimmable, Clipboard-copyable, with a Ponder scene per block
- The Remote Trading branch is gone from FTB Quests: the `market`, `professions` (Bulletin Board) and `shipments` chapters and the `Remote Trading` chapter group are deleted, along with their quest/task lang keys in `config/ftbquests/quests/lang/en_us.snbt` and the `cabin.*` keys in the six `kubejs/assets/ftbquests/lang/*.json` files. The dead commented gold-coin fortress-locator event at the top of `kubejs/server_scripts/quests.js` is removed too. No other chapter depended on the removed quests
- Local quest/config refresh merged against 0.0.88: generated configs and quest chapters were identical to upstream's refresh except for regenerated quest UUIDs (upstream's kept) and the Remote Trading deletion (kept)

## [0.0.88]
- **Octo Legs 0.7.5 hybrid** is the pack override (`.pakku/overrides/mods/octolegs-0.7.5+mc1.21.1.jar`). Terrain attitude is fitted from the plane through planted feet, ordinary hull support is contact-gated, and the gravity support polygon is disabled. Wall and steep-grade climbing keep grip through the crest; the step trigger and reach tolerance are 15% longer. The hybrid build also fixes stale leadership, empty-mount control clearing, swing completion, and thin-block footholds.
- **Octo Legs goggles** are split by block: a leg mount shows only that leg's tentacle, reach, mass, speed, efficiency, stress, mode, and stride; the controller shows the combined lift pool, ship mass, powered legs, planted feet, load, and pool stress. The tooltip builder no longer accumulates lines.
- **Octo Legs stress** is pool-based: every fitted leg contributes its mass capacity and stress impact; each mount reports `base stress × (ship mass / total lift capacity)` (capped at 4×), and the SU requirements are 8× the previous values.
- **Pack config refresh**: new/updated Ars Nouveau, Iron's Spellbooks, JAOPCA, Create Radar, Create Controlling, Create Ultimine, FTB Ultimine, NERB, Molten Sea, NoLijium, Nowheel, Voxy seedgen, Chunky, and Cobblemon dimension datapack configs; quest, Immersive Portals, submarine hull, NuclearCraft, and client config updates.
- **Repo hygiene**: runtime backups, heap dumps, and client state are ignored; the pack repo tracks only the pack overrides and configs

## [0.0.87]
- New client compat mods: `emf-compat-irons-artifice-neoforge-1.21.1-1.0.0.jar` (+ EMF Compat Core) fixes third-person gun poses from Iron's Arms 'n Artifice; `veil_bonk-1.21.1-0.3.2-neoforge.jar` applies the open Veil 4.5.2 bugfix PRs as mixins; `iris_bonk-1.21.1-0.1.3-neoforge.jar` keeps custom core shaders visible under Iris shaderpacks. All three are client-side
- Truly Modular (miapi + arsenal + archery + armory) now ships from our forks (`InvalidArgument3/Modular-Item-API`, `Arsenal`, `Archery`, `Armory`) through GitHub-release Pakku sources instead of Modrinth/CurseForge. The fork builds strip the pride skin sets and the supporter-only wizzard skin; summer skins stay. Fork builds are miapi 2.4.4, arsenal 2.4.2, archery/armory 2.2.2
- Dependency pins so `pakku update` cannot re-break them: `emf_compat_core_neoforge_1.21.1_2.3.2.jar` (artifice pins it exactly), `OctoLib-NEOFORGE-0.6.3+1.21.jar` (nerb needs `>=0.6.2`, and the old `octo-lib` slug is stale), `touch_n_motion_neoforge_1.21.1_1.0.4.jar` (1.0.5 needs core `>=2.4.0`, which fights the artifice pin; 1.0.4's floor is 2.3.0). All three carry `update_strategy: NONE`
- Pakku: ARR-licensed mods are marked `redistributable: true` in pakku.json so exports keep their jars (ars-nouveau, irons-spells-n-spellbooks, xaeros-minimap/world-map, cobblemotion, fresh-animations-extensions, fa-player-extension, yeetus-experimentus, client-tweaks, fusion-connected-textures, certain-questing-additions, sound, default-options among them)
- Bulk mod updates across the pack (MiAPI 2.4.2, JEI 19.70, KubeJS 2101.7.2-build.381, ExtraQuests 2.0, Sophisticated Storage 1.6.16, Quark 488, Create-family fixes, Cobblemon extras, Dungeons Delight 1.5.2, Reliquary 2.0.84 and ~40 more)

## [0.0.86]
- ServerCore mob categories use the Friends & Foes enum names `FRIENDSANDFOES_GLARES` and `FRIENDSANDFOES_RASCALS`. The Spore categories in that file already match `SPORE_INFECTED`, `SPORE_ORGANOIDS`, and `SPORE_EXPERIMENTS`
- Paxi datapack `Autisnautics Compat Filters` hides Copycats+ `physics_block_properties/copycat_catwalk.json` (the catwalk item places `copycats:copycat_board`, which already has the same Sable mass and volume) and Truly Modular Armory's debug `miapi/material_extensions/test.json` (no `key`, so it cannot resolve a material)
- Recipe-viewer hide scripts skip Ad Astra, Thermal, Ender IO, and Create Casing unless that mod is loaded. JAOPCA hides run only for items JAOPCA actually registered. Hides for mods that are installed, including Create Addition's own items, stay as they are
- Dimension stack order, top to bottom, is now overworld, deepsea, caves, abyss, otherside, undergarden, infernal, nether, molten depths, the end. Commit 795d2fd had already reordered `config/immersive_portals.json` and left the rift carves on the old pairing. The lithostitched masks, postgen spans, wall ids, and `rift_api.js` now follow the preset. Each boundary is still one disc on the upper floor and the same disc on the lower ceiling. Face windows from 0.0.85 stay, including the overworld water column and the deepsea ceiling. Abyss, Otherside, Infernal, and the Nether gained the portal Y that their slabs already used. Already generated chunks do not regenerate
- `autisnautics:molten_depths` is a continuous eleven-layer Modern Foundry sea from Y=-39 through Y=294, aluminum on top and osmium on the bottom, with no obsidian walls, basalt catwalks, or other separators. The obsidian caps and the rift clears stay Y=295..319 and Y=-64..-40. Companion mod `moltensea` 1.0.1 stops those metals from replacing each other; the 1.0.1 jar ships in `.pakku/overrides/mods/` (the IllegalClassLoadError fix moves its helpers out of the mixin package). Fog for the eleven metals is lengthened under `kubejs/assets`. Already generated molten depths chunks do not regenerate
- KubeJS recipe scripts no longer submit the ids that failed on the 9 Nov 2026 client log. `global.AutisnauticsRift` is published from `kubejs/startup_scripts/rift_api.js`, because a server script cannot assign `global` on KubeJS 2101. Postgen keeps the same span numbers, including the overworld water column and the deepsea ceiling
- Create Deco cast iron recipe ids point at industrial iron (`createdeco:industrial_iron_ingot`, `createdeco:industrial_iron_nugget`, `createdeco:industrial_iron_sheet`, block `create:industrial_iron_block`). Architect's Palette wither bone inputs use `modernfoundry:necrotic_bone`. The old Create Enchantment Industry disenchanter recipe is not registered. The color-substrate alchemy chain stays off because those ids collide with the live reagent grid
- `#forge:` item tags in the scripts now use the NeoForge `#c:` names (`slime_balls`, `foods/dough`, `glass_blocks`, `glass_panes`, `sands`, `glazed_terracottas`, `stripped_logs`, `rods/wooden`). `forge:chisels` stays, because that tag is defined by this pack. Lead casings stay on `immersiveengineering:plate_lead`. Project Red and Clockwork recipes stay behind item and mod checks
- Wafer Gate holds one ruined wafer in the new `autisnautics:wafer` dimension (Paxi datapack `Autisnautics Wafer`: 'Void wafer dimension for the chip-fab exorcism. Not part of the rift stack.'). Right-click the gate to enter that cell; death sends you back with your items, and the same wafer stays seated for another try. `kubejs/server_scripts/wafer_maze.js` carves a 17x17 maze per cell from a per-seed LCG. The gate item is a `wafer_gate` block with a 1-slot wafer inventory, `mineable/pickaxe`, `create:wrench_pickup`, light level 6
- The wafer lattice is now one persistent worldgen structure instead of per-visit carved cells: `wafer/maze.nbt` via structure `wafer_maze` / structure set `wafer_maze` / template pool `wafer_maze`. Chapter 4 chip-fab recipes switch the processor dice to follow whichever storage mod is loaded
- Plain rose quartz stays obtainable and abstruse has a live recipe again. The rose-quartz-to-polished output swap is limited to JAOPCA ids (`/^jaopca:/`), which an empty filter used to rewrite Create's own `create:rose_quartz` too, so quest `1861E583C9DAA0BA` could never see the plain item. The laser accelerators are spelled right, the abstruse mechanism is crafted without Thermal, and the stage-3 electrum quest points at Create Addition's ingot
- New `kubejs/server_scripts/rift_fresh_entity_guard.js` stops Immersive Portals looping on brand-new entities below a rift portal. Cause: the portal test runs over `level.getAllEntities()`, and before an entity's first tick its old position is still 0, so the eye-to-eye segment spans from (0, eye, 0) to the entity and crosses the portal plane (e.g. an item below the overworld rift water under `bottomY` -49). The guard seeds old position to the current position on spawn when it is unset, which is what vanilla does on first tick anyway. Rift crossing for items and mobs is unchanged
- Prism-style textures land for the machines and treasure pots across the pack (andesite, boron, brass, constantan, copper, enderium, gold, steel, zinc machines; lander deployer; navigation computer; small, tall, and quartz treasure pots; Cabin boron and constantan casings; refined and incomplete refined mechanism), with matching block and item models so they render placed and in hand
- Pack extras and pinned mods: `pakku-lock.json` is refreshed and the new mods pinned. Paxi datapack `Autisnautics Audit Fixes` holds the Cobblemon legendary orbs Eevee recipe (`eevee_cr` -> `cobblemon_legendary_orbs:eevee_summon_orb`), the Create Deco placard dye recipe (`dye_placard`), an `azure_ball` disable stub, and the Dungeons Delight 1.21 GLM conditions. Loot-modifier drops for brined flesh, gritty flesh, and sniffer shank, plus scavenging loot for gunk, rotten tripe, and slime noodles, ride along in the same datapack. Paxi datapack load order is updated

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
