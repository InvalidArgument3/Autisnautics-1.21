# Create andesite / brass split — audit and plan

Implemented on main after this audit. No release. The sections below are the audit; the chain that shipped is the one in "What shipped".

## What shipped

- Millstone recipe `kubejs:milling/netherrack_cinder_flour`: netherrack to 1 `create:cinder_flour` plus a 50% second, same yield as `create:crushing/netherrack`, 250 ticks. The millstone is andesite (cog + andesite casing + stone).
- `create:electron_tube` has one recipe: the chapter 2 spout fill of polished rose quartz with 10 mB `blazinghot:molten_iron`. `create:crafting/materials/electron_tube` is removed, and any other recipe that outputs the tube is removed before the fill is added. Create Addition 1.7.2 does not add a tube recipe. JAOPCA's jar does not mention `electron_tube`.
- Andesite alloy stays the vanilla craft. The four removes in `chapter_1.js` stay commented. The algal-brick mixer recipe is not registered, so it cannot be read as the required path.
- `kubejs:kinetic_mechanism_manual_only` stays (hammer, cog, alloy, stripped log).
- Brass machines listed in the edit list lose their vanilla crafts. `kubejs:precision_mechanism` is the precision recipe (`create:sequenced_assembly/precision_mechanism` is removed). Steam engine and steam whistle stay vanilla.
- Shortcuts that skipped a brass gate are gone: deployer and contraption controls via the andesite machine, the gold-sheet brass hand, the potato cannon via the copper machine, and `blazinghot:casting_depot` via the copper machine. The depot's own recipe is sturdy alloy + sturdy casing + an electron tube, so the copper-machine craft did not still make sense.
- Simulated and Offroad are not lockfile projects. They load from the Create Aeronautics bundled jar (`modId` `simulated` and `offroad`, jar-in-jar next to `aeronautics`). Their machine recipes stay, behind `Platform.isLoaded`. AE2, Thermal, and Tinkers are absent: AE2 calls stay inside `isLoaded("ae2")`, Thermal calls no-op through the stand-in in `metallurgy.js`, and `custom()` skips a recipe whose type namespace is not loaded. The override jar named `TinkersConstruct-Unofficial-Port-...` is Modern Foundry (`modId` `modernfoundry`), not Tinkers. Create Encased stays behind `isLoaded("createcasing")`.
- Rope pulley is wool again (`create.js` no longer replaces it with Supplementaries rope).
- Quest `667F4F686A45EF3E` (Gold Machine) moved from `stage_1.snbt` to `stage_2.snbt` with the same id. It depends on reinforced mechanism `76706BE282ABAC31` and brass machine `7FC4D1E69377271D`.

### Tube chain, no loop

Checked against Create 6.0.10 and Blazing Hot 0.7.4.

| Step | Needs | Tube, deployer, or crafter? |
|---|---|---|
| Andesite alloy | Crafting table, andesite, iron or zinc nuggets | No |
| Millstone, press, mixer, spout | Andesite casing, shafts, cogs. Spout is copper casing + dried kelp (`create:crafting/kinetics/spout`). Copper casing is a copper ingot on a stripped log. | No |
| Empty blaze burner | Iron sheets (press) + netherrack (`create:crafting/kinetics/empty_blaze_burner`) | No |
| Lit burner | Flint and steel on the empty burner. `BlazeBurnerBlock` has `FLINTANDSTEEL_USE`. Capturing a blaze also works and is not required. | No |
| Cinder flour | New millstone recipe | No |
| Blaze cake base | Compacting egg + sugar + cinder flour. That is a basin and a press, not a mixer and not a sequence. | No |
| Blaze cake | Spout fills the base with 250 mB lava (`create:filling/blaze_cake`) | No |
| Molten iron | Mechanical mixer, superheated, iron ingot (`blazinghot:mixing/melting/ingots/iron_mixer_only`, 90 mB). Superheat fuel is the blaze cake (or blaze roll, which mills cinder flour into netherrack dust and never uses a deployer). | No |
| Electron tube | Spout fill, polished rose quartz + 10 mB molten iron | The spout does not need a tube. This step produces the first one. |

Brass ingot is heated mixing (coal or lava in the burner), not superheated, so it does not need the cake or a tube. Brass hand, then deployer (tube + casing + brass hand), then the precision sequence. The deployer is after the tube, not before it.

Gold machine is railway casing (brass casing + an obsidian sheet) around a reinforced mechanism. The quest now waits on the brass machine quest, which is after precision mechanisms.

"A Source of Steel" (`68A8C2CB135ECA87`, stage 2) asks for `create:crushed_raw_iron`. That dust comes from a crushing wheel. The wheel quest (`7AAE71F57FB89BCC`) now sits in stage 2, after the brass machine, and steel depends on it. See "Quest alignment".

### Server check

Dedicated server, NeoForge 21.1.265, Java 25, this tree's `kubejs/`. KubeJS logged `Loaded 64/64 KubeJS server scripts in 0.363 s with 0 errors and 1 warnings`. The warning is the existing skip in `recipes/trading.js` because Thermal is not loaded; the script still loaded. Recipe pass: `Added 1135 recipes, removed 49 recipes, modified 148 recipes, with 0 failed recipes`. After `/kubejs export debug`: `Reloaded with no KubeJS errors!`

The export has one recipe whose result is `create:electron_tube`: `create:filling` of polished rose quartz with 10 mB `blazinghot:molten_iron` (`chapter_2.js`). `create:crafting/materials/electron_tube` and `create:sequenced_assembly/precision_mechanism` are in the removed set, with the twelve brass-machine crafts. `kubejs:milling/netherrack_cinder_flour` is 1 flour plus a 50% second from netherrack, 250 ticks. Vanilla alloy crafts, the unheated alloy mixes, deployer, spout, millstone, mixer, empty blaze burner, steam engine, steam whistle, heated brass ingot, blaze-cake compacting, and `kubejs:kinetic_mechanism_manual_only` are still present. `blazinghot:crafting/casting_depot` is the only casting-depot recipe and it takes an electron tube. Immersive Engineering's blueprint makes `immersiveengineering:electron_tube`, a different item.

Create's own recipes are from `create-1.21.1-6.0.10`. An item is **brass-tier** when every non-storage Create recipe for it needs a brass ingot, brass sheet, brass casing, electron tube, precision mechanism, or brass hand, or needs a machine that itself is brass-tier. Everything else Create crafts from wood, andesite, iron, zinc, copper, or gold is **andesite-tier**.

## How the scripts behave today

`kubejs/server_scripts/recipes/chapters.js` lines 34–448 are inside a block comment. The live Create progression is `kubejs/server_scripts/recipes/chapters/chapter_*.js` plus `kubejs/server_scripts/key_mods/create.js`.

A line marked `restored original:` is a commented `event.remove` or `event.replaceInput`. The vanilla recipe is still in the game. The KubeJS recipe under it is an **alternative**, not the only way.

`Platform.isLoaded("ae2")`, `thermal`, `tconstruct`, and `createcasing` are false in this pack (no project in `pakku-lock.json`). Those blocks do not register. `blazinghot` is true: Create: Blazing Hot `create_blazing_hot-0.7.4+neoforge-mc1.21.1.jar`, mod id `blazinghot`.

## Correction: Blazing Hot and the tube fill

The first pass said Blazing Hot was absent and that `blazinghot:molten_iron` was not a fluid. Both are wrong.

`blazinghot:molten_iron` is the fluid. `data/c/tags/fluid/molten_iron.json` in that jar lists `blazinghot:molten_iron` and `blazinghot:flowing_molten_iron`. The chapter 2 fill (`chapter_2.js:74`, 10 mB onto polished rose quartz) matches that id. One iron ingot melts into 90 mB, so the amount is enough.

How the fluid is made, from the 0.7.4 jar:

| Recipe | Machine | Heat | Inputs |
|---|---|---|---|
| `blazinghot:mixing/melting/ingots/iron_mixer_only` | `create:mechanical_mixer` on a basin (`create:mixing`) | superheated | iron ingot. 90 mB. Same shape for nuggets and iron plates. |
| `blazinghot:blaze_mixing/melting/ingots/iron` | Blaze mixer (`create:blaze_mixing`) | superheated | iron ingot + 50 mB `blazinghot:blaze_mixer_fuel` (lava or nether lava) |

The mixer is andesite. Superheat is not. Create's superheat map has only `create:blaze_cake`. Blazing Hot adds `blazinghot:blaze_roll`. Both of those fuels need `create:cinder_flour`.

Cinder flour's only production recipe in the Create jar is `create:crushing/netherrack` (the Ex Nihilo variant does not load; that mod is not in the lock). Crushing needs a crushing wheel. The wheel's only recipe is `create:mechanical_crafting/crushing_wheel`, and the mechanical crafter is electron tube + brass casing + a crafting table.

Blaze roll is not a way around that. `blazinghot:nether_dough` needs `blazinghot:nether_compound`, which needs `c:dusts/netherrack`. That tag contains only `blazinghot:netherrack_dust`, and every recipe for that dust starts from cinder flour. Soul dust can be milled from soul sand (millstone, andesite), but the nether-dust half cannot.

The blaze mixer is not a way around it either. `blazinghot:sequenced_assembly/blaze_mixer` deploys a blaze whisk onto a blaze casing. Deploying needs a deployer, and the deployer needs an electron tube. The whisk also takes brass ingots.

Without a millstone cinder-flour recipe, the fill cannot be the only electron-tube recipe. The loop would be: tube fill needs molten iron, molten iron needs superheat, superheat needs cinder flour, cinder flour needs a crushing wheel, the wheel needs a mechanical crafter, the crafter needs an electron tube. The millstone recipe in "What shipped" is that missing step, and the crafting recipe is now removed.

It is a legal later path if the vanilla craft stays (`create:crafting/materials/electron_tube`: polished rose quartz + an iron sheet). That craft does not use molten iron, the spout, or a deployer. Make one tube that way, build the crafter, then the fill can mint more tubes. Polished rose quartz is still sandpaper on rose quartz. The spout is copper casing + dried kelp (`create:crafting/kinetics/spout`). Neither is brass.

Wire cutters were checked in the diesel jar: `createdieselgenerators:crafting/wire_cutters` is andesite alloy + an iron plate + sticks. They are not part of this loop.

`blazinghot:sturdy_casing` and `blazinghot:casting_depot` also exist. `chapter_1a.js:71` is a real copper-machine craft of the casting depot, not a skipped empty item. Blazing Hot's own depot recipe (`blazinghot:crafting/casting_depot`) is sturdy alloy + sturdy casing + an electron tube, so the copper-machine line skips the tube. `chapter_2c.js` sturdy-casing and molten-iron uses are real ids, not missing items.

## Classification

Create 6 gates a few machines the request listed with the andesite examples. Those are called out in the last column.

| Output | Tier | Why | Pack recipe today | Vanilla still works? |
|---|---|---|---|---|
| `create:andesite_alloy` | Andesite | Crafting: andesite + iron or zinc nuggets. Mixing the same pair does not need a built mixer if the crafting recipe exists. | `chapter_1.js:77` mixing algal brick + andesite, 2 alloy. No id. | Yes. Removes at `chapter_1.js:71-74` are commented. |
| `create:shaft`, `cogwheel`, `large_cogwheel` | Andesite | Alloy, or shaft + planks. | None that replace them. | Yes. |
| `create:andesite_casing` | Andesite | Alloy on a stripped log. | `create.js:12` shapeless 2x from alloy + any logs. Extra, vanilla item application stays. | Yes. |
| `create:water_wheel`, `large_water_wheel` | Andesite | Shaft + planks. | None. | Yes. |
| `create:millstone`, `mechanical_press`, `mechanical_mixer`, `mechanical_saw`, `mechanical_drill`, `encased_fan` | Andesite | Andesite casing plus an iron, shaft, whisk, or propeller part. No brass. | `chapter_1.js:116-120` `andesiteMachine(...)` item application. Saw and drill want `kubejs:saw_blade` / `kubejs:drill_head` instead of iron. | Yes. No remove. |
| `create:belt_connector` | Andesite | Dried kelp. | `chapter_1a.js:19` shaped 3x from `kubejs:rubber`. Extra. | Yes. |
| `create:whisk`, `propeller`, `gearbox`, `clutch`, `gearshift`, `chute`, `basin`, `depot`, `fluid_pipe`, `fluid_tank`, `mechanical_pump`, `spout`, `item_drain`, `hose_pulley`, `hand_crank`, `blaze_burner` | Andesite | No brass ingredient and no brass machine. | Copper-machine extras for spout, drain, hose pulley, smart fluid pipe, portable fluid interface (`chapter_1a.js:61-65`). | Yes. |
| `create:steam_engine` | Andesite | Alloy + copper block + gold plate. Not brass. | `chapter_2.js:97` `brassMachine`. Extra. | Yes. |
| `create:steam_whistle` | Andesite | Copper ingot + gold plate. | `chapter_2.js:107` `brassMachine`. Extra. | Yes. |
| `create:weighted_ejector` | Andesite | Gold plate + depot + cog. | None. | Yes. |
| `create:brass_ingot` | Brass | Heated mixing, copper + zinc. | No working custom. `metallurgy.js:121` remove is commented. Thermal smelter at `metallurgy.js:201` does not run (Thermal is absent). | Yes. This is the only brass-ingot recipe. |
| `create:brass_sheet`, `brass_casing` | Brass | Press the ingot. Casing is the ingot on a stripped log. | `create.js:15` shapeless 2x casing from brass sheet + logs. Extra. | Yes. |
| `create:electron_tube` | Brass | This is the brass-age component even though the craft is polished rose quartz + an iron sheet. | `chapter_2.js:74` spout-fill of polished rose quartz with 10 mB `blazinghot:molten_iron`. The fluid id exists. | Yes. Remove at `chapter_2.js:10` is commented. The fill is a second path, and it loops if it is the only path (see the correction above). |
| `create:precision_mechanism` | Brass | Vanilla sequence starts from a gold sheet and needs a deployer. | `chapter_2.js:81` id `kubejs:precision_mechanism`, from `kubejs:kinetic_mechanism` + two electron tubes + `createdieselgenerators:wire_cutters`. | Yes. Remove at `chapter_2.js:20` is commented. |
| `create:brass_hand` | Brass | Alloy + brass plates. | `chapter_1.js:189` id `kubejs:brass_hand_gold`: alloy + **gold** sheets. Extra. | Yes. |
| `create:deployer` | **Brass, not andesite** | Electron tube + andesite casing + brass hand. | `chapter_1.js:121` `andesiteMachine` + brass hand. That path skips the electron tube. | Yes. |
| `create:mechanical_crafter` | **Brass** | Electron tube + brass casing + crafting table. Not andesite-tier. | `chapter_2.js:95` `brassMachine`. Extra. | Yes. |
| `create:sequenced_gearshift`, `rotation_speed_controller`, `mechanical_arm`, `stockpile_switch`, `content_observer`, `brass_funnel`, `brass_tunnel`, `smart_chute`, `clockwork_bearing`, `elevator_pulley`, `display_link` | Brass | Electron tube and/or brass plates. | Matching `brassMachine` calls in `chapter_2.js:96-109`. Extras. | Yes. |
| `create:contraption_controls` | Brass | Electron tube + andesite casing + a button. | `chapter_1.js:133` `andesiteMachine` with no extra item, so the andesite machine alone cuts it. Skips the tube. | Yes. |
| `create:crushing_wheel` | Brass | Only recipe is mechanical crafting, and the crafter is brass. | `chapter_3.js:90` another mechanical craft (cobble + sticks). Still needs the crafter. | Yes. |
| `create:potato_cannon` | Brass | Mechanical crafting. | `chapter_1a.js:66` `copperMachine` + fluid pipe. Skips the crafter. | Yes. |
| `create:adjustable_chain_gearshift` | Brass | Electron tube in the vanilla craft. | The replace that swapped the tube for redstone is commented (`create.js:26`). | Yes, still the tube. |
| Trains: `track`, `controls`, `track_station`, `track_signal`, `railway_casing` | Brass | Track assembly is a deployer sequence. Railway casing is brass casing + an obsidian sheet. Controls are railway casing + a precision mechanism. Signals are railway casing + an electron tube. | `chapter_1b.js:117-121` `goldMachine` for controls, station, signal, schedule, observer. | Yes. |
| `create:schedule` | Andesite | Vanilla is paper, not brass. | `chapter_1b.js:120` `goldMachine`. Extra. | Yes. |
| `create:rope_pulley` | Andesite | Shaft + iron + a slab, plus wool in vanilla. | `create.js:27` **live** `replaceInput` of `#minecraft:wool` to `#supplementaries:ropes`. | No. This one is already replaced. |

Pack gates, not Create items:

| Item | Tier of the gate | Recipe | Status |
|---|---|---|---|
| `kubejs:kinetic_mechanism` | Andesite gate (stage 1) | Sequenced deploy of two alloy onto a wooden slab, then the diesel hammer (`chapter_1.js:100`, id `kubejs:kinetic_mechanism`). Also shapeless hammer + cog + alloy + stripped log (`chapter_1.js:110`, id `kubejs:kinetic_mechanism_manual_only`). | Active. No vanilla item to remove. |
| `kubejs:andesite_machine` | Andesite gate | Donut of andesite casing around a kinetic mechanism (`chapter_1.js:113`). | Active. |
| `kubejs:brass_machine` | Brass gate (stage 2) | Donut of brass casing around a precision mechanism (`chapter_2.js:92`). | Active. |
| `kubejs:sealed_mechanism`, `kubejs:copper_machine` | After rubber, still andesite materials | `chapter_1a.js`. | Active alternatives for copper fluid blocks. |
| `kubejs:reinforced_mechanism`, `kubejs:gold_machine` | Gold machine **uses railway casing**, so the donut is brass | `chapter_1b.js:96` and `:114`. | Active. |

## The alloy loop

It is written, and it is not the path the game forces.

`chapter_1.js:77` mixes `architects_palette:algal_brick` + `minecraft:andesite` into 2 `create:andesite_alloy`. Mixing needs a `create:mechanical_mixer`. The mixer's vanilla recipe is andesite casing + cog + whisk, and the casing is alloy. That is a cycle **only if** the crafting-table alloy recipes are gone.

They are not gone. These removes are comments:

```
chapter_1.js:71  create:crafting/materials/andesite_alloy
chapter_1.js:72  create:crafting/materials/andesite_alloy_from_zinc
chapter_1.js:73  create:mixing/andesite_alloy
chapter_1.js:74  create:mixing/andesite_alloy_from_zinc
```

The crafting recipe is andesite + iron nuggets (or zinc nuggets). No mixer. The Andesite Alloy quest can be finished that way. The algal mix is a 2-for-1 alternative that is useless until a mixer already exists.

`chapters.js:77-85` still contains the old removes, inside the block comment that starts at line 34. They do not run.

**Fix:** leave `chapter_1.js:71-74` commented. Do not point the quest at the mixer. Keep or delete `chapter_1.js:77` as a bulk recipe that is legal only after the mixer; deleting it is the cleaner JEI. Either way the alloy stays the vanilla craft.

## Dependency graph under the split

Andesite outputs use Create's recipes. Brass outputs use the pack recipes, and the vanilla recipe for that output is removed. Brass ingot stays Create's heated mix, because nothing else produces it.

```
world: logs, andesite, iron, copper, zinc ore, kelp, redstone, quartz, netherrack
  furnace -> zinc ingot, iron ingot, iron nuggets, iron sheet (press, once the press exists)
  craft -> andesite alloy          (NOT the algal mixer recipe)
    -> shaft, cog, large cog, andesite casing (item application)
    -> water wheel, millstone, press, mixer, saw, drill, fan, belt (kelp), basin, depot
    -> blaze burner (netherrack + iron sheet) -> heated basin
  stage 1 pack, no brass:
    diesel hammer (chapter_1.js:82, iron + sticks; the diesel hammer remove is commented)
    kinetic mechanism MANUAL (chapter_1.js:110)   [see deployer trap below]
    andesite machine (casing + kinetic mechanism)
  brass ingot = heated mix of copper + zinc on that mixer. Mixer does not need brass.
    -> brass sheet (press), brass casing (ingot on stripped log)
  electron tube = polished rose quartz + iron sheet   (the path that does not loop)
    rose quartz = quartz + redstone (vanilla craft)
    polished = sandpaper on rose quartz (create:sandpaper_polishing/rose_quartz)
    the AE2 certus mix in chapter_2.js:67 does not register
    the molten-iron fill is a second recipe and loops if this craft is removed
      (superheat -> cinder flour -> crushing wheel -> crafter -> tube)
  brass hand = alloy + brass sheet
  deployer = electron tube + andesite casing + brass hand
  precision mechanism = kinetic mechanism + 2 electron tubes + wire cutters, on a deployer
    (kubejs:precision_mechanism). Remove create:sequenced_assembly/precision_mechanism.
  brass machine = brass casing around a precision mechanism
    -> crafter, sequenced gearshift, rotation controller, arm, stockpile switch,
       content observer, brass funnel, brass tunnel, smart chute, clockwork bearing,
       elevator pulley, display link
  railway casing = brass casing + obsidian sheet
    -> track station, track signal, train controls
  track = deployer sequence on a sleeper (materials are iron/zinc nuggets; the machine is the deployer)
  crushing wheel = mechanical crafting, so it waits on the crafter
```

No cycle on that graph:

- Alloy does not need the mixer.
- The mixer does not need brass.
- Brass ingot needs the mixer, which already exists.
- Electron tube's crafting recipe does not need the deployer or molten iron. The fill does need molten iron, and that fluid's superheat needs a crushing wheel, which needs a tube. Keep the crafting recipe.
- The deployer needs the tube and the brass hand, then the precision sequence needs the deployer. Order is tube, then hand, then deployer, then precision mechanism.
- Kinetic mechanism's **manual** recipe needs a cog and alloy, not a deployer.

The sequenced kinetic mechanism (`chapter_1.js:100`) deploys onto a slab. Deploying needs a deployer, and the deployer is brass. That path cannot be the stage 1 recipe. The shapeless `kubejs:kinetic_mechanism_manual_only` can. Keep it.

## Quest line under that graph

Stage 1 tasks that are the andesite line, in book order: log, kelp, sand, andesite, clay, algal blend, planks, slab, stripped log, **andesite alloy**, algal brick, **kinetic mechanism**, engineer's hammer, **andesite machine**, raw rubber, rubber, **belt connector**, deepslate, magma, obsidian, **gold machine**, **reinforced mechanism**.

| Quest task | Obtainable at that quest? |
|---|---|
| Andesite alloy | Yes, vanilla craft. Algal brick is a parallel quest, not an input. |
| Kinetic mechanism | Yes, via the hammer + cog + alloy + stripped log. The sequenced recipe is not available yet. Hammer recipe is the simplified one in `chapter_1.js:82`; its remove of the diesel recipe is commented, so both hammers work. |
| Andesite machine | Yes, after the kinetic mechanism and a casing. |
| Belt | Yes, dried kelp, or the rubber alternative after the rubber quest. |
| Overview "Andesite Workshop" (press, saw, fan, wrench, mixer, basin, millstone) | Yes. All of those are andesite-tier vanilla crafts. Wrench is gold plate + cog + stick (`create:crafting/kinetics/wrench`). |
| **Gold machine** (`chapter_1b.js`) | **Yes, in stage 2.** The donut is `create:railway_casing` + reinforced mechanism. Railway casing is brass casing plus an obsidian sheet. Quest `667F4F686A45EF3E` now lives in `stage_2.snbt` and depends on the reinforced mechanism and the brass machine. The track reward stays on that quest. |
| Reinforced mechanism | The mechanism itself is kinetic mechanism + obsidian (`chapter_1b.js:106`). Obsidian is a stage 1 quest. The **machine** wrapped around it is the problem above. |

Stage 2 tasks that are the brass line: rose quartz, electron tube, polished rose quartz, **precision mechanism**, **brass machine**, wrench, infernal mechanism, zinc machine, blaze burner, logistic mechanism, lead machine, crushed raw iron.

| Quest task | Obtainable at that quest? |
|---|---|
| Rose quartz, polished rose quartz | Yes, vanilla. The certus mix does not run. |
| Electron tube | Yes, vanilla craft (polished rose quartz + iron sheet). The fill at `chapter_2.js:74` is also a real recipe (`blazinghot:molten_iron` is in the Blazing Hot jar). Making that fill the only recipe loops through superheat and the crushing wheel, so the Electron Tube quest (`5351AF6CA73CB8E0`) would have no first tube. Tinkers and Modern Foundry are still absent; they are not the fluid source. |
| Precision mechanism | Yes, after the tube, a brass hand, a deployer, the stage 1 kinetic mechanism, and diesel wire cutters. Wire cutters ship with Create Diesel Generators, which is in the pack. |
| Brass machine | Yes, after precision mechanism + brass casing. Brass casing does not need the precision mechanism. |
| Blaze burner | Yes, netherrack + iron sheet. Stage 2 is late for it; it is not blocked. |
| Crushed raw iron ("A Source of Steel", depends on precision mechanism `19715EBCBBA1B4AD`) | The only Create source is `create:crushing/raw_iron`, which is a crushing wheel. The wheel is mechanical crafting, so it needs a mechanical crafter, which needs an electron tube and a brass casing. Those exist by this quest. The quest that **asks** for a crushing wheel is stage 3 (`7AAE71F57FB89BCC`), which is later than the item is required. The item is still craftable; the book is just late. |
| Infernal / zinc / logistic / lead machines | Pack mechanisms after precision mechanism. They are the stage 2 tail, not the andesite/brass split. `chapter_2c.js` `blazinghot:sturdy_casing` and `blazinghot:molten_iron` are real ids. Sturdy alloy still needs the same superheated melt, so that line is after crushing wheels, not a missing item. |

Stage 3 stockpile switch and mechanical harvester: switch is brass and belongs after stage 2; harvester is andesite and is just asked late. Both craftable.

Stage 4b "Bottling Machine" also tasks `create:mechanical_saw` and `create:mechanical_mixer`. Both stay vanilla andesite. No new gate.

Stage 5 blaze cake is `create:filling/blaze_cake` (cake base + lava) after `create:compacting/blaze_cake`. Spout and press are andesite. Obtainable long before stage 5.

## Blockers and the change for each

1. **Alloy cycle, latent.** File `chapter_1.js`, recipe at line 77, removes at 71–74. Uncommenting those removes makes alloy require a mixer that requires alloy. **Fix:** do not uncomment them. Delete line 77, or leave it as a post-mixer bulk recipe and keep the quest on the crafting table.

2. **Electron tube fill loops if it replaces the craft and cinder flour still needs a crushing wheel.** `chapter_2.js:74`, no recipe id. The fluid id is correct. **Fix, applied:** add the millstone cinder-flour recipe, then remove `create:crafting/materials/electron_tube` and keep the fill as the only tube recipe.

3. **Kinetic mechanism sequenced recipe needs a deployer.** `chapter_1.js:100`, id `kubejs:kinetic_mechanism`. Deployer is brass, and this quest is stage 1. **Fix:** keep `chapter_1.js:110` (`kubejs:kinetic_mechanism_manual_only`). Do not remove it when cleaning alternatives. The precision mechanism is allowed to depend on this item; the manual craft's inputs (alloy, cog, hammer, stripped log) do not change under the split.

4. **Gold machine was a stage 1 quest and a brass item.** `chapter_1b.js`, quest id `667F4F686A45EF3E`. **Fix, applied:** the quest object moved to `stage_2.snbt` with the same id, depending on reinforced mechanism `76706BE282ABAC31` and brass machine `7FC4D1E69377271D`. Reinforced mechanism stays in stage 1.

5. **Deployer and contraption controls are brass, and stage 1 gives andesite shortcuts.** `chapter_1.js:121` and `:133`. The gold hand at `chapter_1.js:189` (`kubejs:brass_hand_gold`) also skips brass plates. **Fix, matching Create's tiers:** delete those three so the vanilla recipes stand (`create:crafting/kinetics/deployer`, `create:crafting/kinetics/contraption_controls`, `create:crafting/kinetics/brass_hand`). Deployer then waits for an electron tube. If the deployer must be andesite instead, that is a deliberate exception: replace `create:crafting/kinetics/deployer` with a recipe that uses no electron tube and no brass plate, and do not build it through the mixer.

6. **Mechanical crafter is brass.** `chapter_2.js:95` is only an extra today. **Fix:** uncomment-style add `event.remove({ id: "create:crafting/kinetics/mechanical_crafter" })` so the brass machine is the recipe. Do not add an andesite craft for it.

7. **Potato cannon bypass.** `chapter_1a.js:66` `copperMachine` makes a brass mechanical-crafting item from the stage 1 copper machine. **Fix:** delete that call. Leave `create:mechanical_crafting/potato_cannon`.

8. **Rope pulley is an andesite recipe that is already not vanilla.** `create.js:27` replaces wool with `#supplementaries:ropes` on `create:crafting/kinetics/rope_pulley`. **Fix:** delete that `replaceInput` so the pulley goes back to wool.

9. **Steam engine and steam whistle are not brass.** `chapter_2.js:97` and `:107` add brass-machine crafts beside the vanilla ones. **Fix:** leave the vanilla ids in place (`create:crafting/kinetics/steam_engine`, `create:crafting/kinetics/steam_whistle`). Do not remove them. The brass-machine versions can stay as later extras or be deleted so JEI does not imply they are brass-gated.

10. **Crushed raw iron before the crushing-wheel quest.** Not a hard lock: the wheel is craftable once the crafter exists, and "A Source of Steel" already depends on the precision mechanism quest. **Fix:** optional, move the stage 3 crushing-wheel quest up next to that steel quest so the book shows the machine before it asks for the dust.

11. **Mods that are actually absent, rechecked in the lock.** AE2 (`chapter_2.js:67` certus rose quartz), Create Encased (all of `createcasing.js`), Thermal (`chapters.js` `hasThermal` infernal sequence), Tinkers, Modern Foundry, Simulated, and Offroad. Simulated and Offroad calls in `chapter_1.js` / `chapter_1b.js` are not wrapped in `isLoaded`; `createMachine` skips them because the items are not in any jar. `chapter_1a.js:71` `blazinghot:casting_depot` does register: the item exists, and this copper-machine craft skips the electron tube in `blazinghot:crafting/casting_depot`. Delete that call if the depot should stay tube-gated. `chapter_2c.js` Blazing Hot ids are real.

## Brass recipes that consume an andesite item

Under this split the andesite **items** the brass line eats do not change recipe:

- `kubejs:precision_mechanism` eats `kubejs:kinetic_mechanism`. That mechanism's manual recipe keeps eating vanilla alloy and a vanilla cog. The mechanism is a pack item, not a Create recipe being reverted, so the precision sequence stays valid.
- Brass casing's extra shapeless eats a brass sheet. The sheet stays "press a brass ingot".
- Deployer's vanilla recipe eats an andesite casing. The casing stays "alloy on a stripped log".
- Nothing in the brass line eats the algal alloy mix. Deleting `chapter_1.js:77` does not break a brass recipe.

The shortcuts that **would** change meaning if left in place are the ones that replace a brass craft with an andesite machine: deployer, contraption controls, gold brass hand, potato cannon. Those are the lines in blocker 5 and 7, not silent input swaps.

## Exact edit list

Do this, and do not uncomment any other `restored original:` remove.

**Leave commented (andesite stays vanilla)**

- `kubejs/server_scripts/recipes/chapters/chapter_1.js` lines 71–74, the four alloy removes.
- `chapter_1.js` lines 58–64, the diorite/andesite/granite and flint-compacting removes.
- `kubejs/server_scripts/key_mods/create.js` line 26, the chain-gearshift tube swap.
- `create.js` line 29, the white-sail remove. The extra 2x sail at line 30 can stay or go; it does not replace the vanilla sail while the remove is commented.

**Delete or stop registering (shortcuts)**

- `chapter_1.js:77` algal alloy mix. Optional if the removes stay commented; delete it so the loop is not in JEI.
- `chapter_1.js:121` deployer `andesiteMachine`.
- `chapter_1.js:133` contraption controls `andesiteMachine`.
- `chapter_1.js:189-196` `kubejs:brass_hand_gold`.
- `chapter_1a.js:66` potato cannon `copperMachine`.
- `chapter_1a.js:71` casting depot `copperMachine` (the item exists; this skips the electron tube).
- `create.js:27` rope pulley `replaceInput`.
- Do not delete the molten-iron fill. It is the tube recipe. The millstone cinder-flour recipe is what makes that safe.

**Add these removes (brass becomes the pack recipe)**

In `chapter_2.js` next to the existing commented precision remove:

- `create:sequenced_assembly/precision_mechanism`
- `create:crafting/kinetics/mechanical_crafter`
- `create:crafting/kinetics/sequenced_gearshift`
- `create:crafting/kinetics/rotation_speed_controller`
- `create:crafting/kinetics/mechanical_arm`
- `create:crafting/kinetics/smart_chute`
- `create:crafting/kinetics/clockwork_bearing`
- `create:crafting/kinetics/elevator_pulley`
- `create:crafting/logistics/content_observer`
- `create:crafting/logistics/display_link`
- `create:crafting/logistics/brass_tunnel`
- `create:crafting/logistics/brass_funnel`
- `create:crafting/logistics/stockpile_switch`

**Do not remove**

- `create:mixing/brass_ingot` (`metallurgy.js:121` stays commented).
- `create:crafting/materials/electron_tube` was removed after the millstone cinder-flour recipe landed. It is not in this keep list anymore.
- `create:crafting/kinetics/steam_engine`
- `create:crafting/kinetics/steam_whistle`
- `create:crafting/kinetics/deployer` (vanilla is the brass recipe once the andesite shortcut is gone)
- `create:mechanical_crafting/crushing_wheel` and `create:mechanical_crafting/potato_cannon`
- `kubejs:kinetic_mechanism_manual_only`

**Quests**

- Move stage 1 quest `667F4F686A45EF3E` (gold machine, track icon) to stage 2, after brass casing.
- Crushing-wheel quest `7AAE71F57FB89BCC` now sits with "A Source of Steel" in stage 2. The move is in "Quest alignment".

**Keep as extras (vanilla recipe remains, pack craft is a second path)**

- `create.js` casing `tweak_casing` calls, including andesite and brass 2x shapeless.
- `chapter_1.js` `andesiteMachine` for fan, press, mixer, drill, saw, roller, portable interface, andesite funnel, andesite tunnel, harvester, plough, cart assembler, redstone link.
- `chapter_1a.js` rubber belts and the copper-machine fluid blocks (spout, drain, hose pulley, smart fluid pipe, portable fluid interface).
- `chapter_1.js:100` sequenced kinetic mechanism, as a later convenience once a deployer exists.

## Quest alignment

Quest ids were not changed. New quests use ids `A11E000000000001` through `A11E000000000005`. Reward ids `A11E00000000R001` through `A11E00000000R005` and task ids `A11E00000000T001` through `A11E00000000T005` are new. No quest was deleted. Display text is `config/ftbquests/quests/lang/en_us.snbt`. The cabin overlay in `kubejs/assets/ftbquests/lang/en_us.json` was updated for the five keys that still described the algal mix, the deployer assembly, nugget melting, or a kelp-less belt.

| Id | Chapter | Change |
|---|---|---|
| `106C6DFDCB97327C` | stage 1 | Rewritten. Two andesite and two iron or zinc nuggets make one alloy. |
| `4CC420A6B673176A` | stage 1 | Rewritten. Sand leads to masonry blend, not the alloy. |
| `608BF7BE3CB666F3` | stage 1 | Rewritten. Kelp is masonry, not the alloy. |
| `6B7F0631060A9E62` | stage 1 | Rewritten. Clay blend is masonry. |
| `729FB5DD07C234E8` | stage 1 | Rewritten. Clay balls make masonry blend. |
| `72BE175C83834838` | stage 1 | Rewritten. Algal blend no longer "the brick the alloy is built on". |
| `32786E9D145FFE25` | stage 1 | Rewritten. No longer a dependency of the alloy quest. Side branch. |
| `73FCA432DEFC1DED` | stage 1 | Rewritten. Depends on andesite and the new nugget quest, not the algal brick. Reward `520000000000005E` is iron nuggets; the reward id is unchanged. |
| `A11E000000000001` | stage 1 | Added. Iron nugget. Depends on andesite. Reward: 2 iron ingots. |
| `A11E000000000002` | stage 1 | Added. Cogwheel. Depends on the alloy. Reward: 4 shafts. |
| `675C395B2074C24C` | stage 1 | Rewritten to the hand craft (hammer, cog, alloy, stripped log). Depends on the new cogwheel quest. The deployer sequence is described as later. |
| `3F45DFEF31838A00` | stage 1 | Rewritten. Press, mixer, saw, and drill. The deployer is not a cut of this machine. |
| `4064D448404CD639` | stage 1 | Rewritten. Reinforced mechanism is the hand craft first. |
| `76706BE282ABAC31` | stage 1 | Rewritten. Hand craft. Sequence is later. Gold machine is stage 2. |
| `174E3A810535CAAC` | stage 2 | Rewritten. Empty burner, flint and steel, heated brass on coal or lava, superheat waits on the cake. |
| `A11E000000000003` | stage 2 | Added. Cinder flour from the millstone. Depends on the early burner. Reward: 8 netherrack. |
| `A11E000000000004` | stage 2 | Added. Blaze cake base, pressed. Depends on flour. Reward: 4 sugar. |
| `A11E000000000005` | stage 2 | Added. Blaze cake, 250 mB lava from a spout. Depends on the base. Reward: 4 eggs. |
| `2167272204B6627B` | stage 2 | Retitled Molten Iron. Depends on the blaze cake quest, not iron nuggets. Checkmark task id unchanged. |
| `0E46C1FCE5AEB1DF` | stage 2 | Rewritten. Nuggets are not the tube's iron. |
| `1139F2E4E6FCFE29` | stage 2 | Rewritten. Same. |
| `53735DC4231AB566` | stage 2 | Rewritten. No longer gates the tube. Nothing depends on it. |
| `29D37095C73B3DEC` | stage 2 | Rewritten. Six dried kelp make one belt. Six rubber make three. The rubber dependency is unchanged. |
| `3EF5F89DBD6959E3` | stage 2 | Rewritten. Same belt fact. |
| `7FC4D1E69377271D` | stage 2 | Rewritten. The steam engine also has a crafting recipe. |
| `7AAE71F57FB89BCC` | stage 2 | Moved from stage 3. Depends on the brass machine, not the stage 3 log and stick. Both mechanical-crafting patterns are described. |
| `68A8C2CB135ECA87` | stage 2 | Depends on the crushing wheel as well as precision and the pre-existing `6EDA86E4BE0F096B` entry. Text names the wheel. |
| `5351AF6CA73CB8E0` | stage 2 | Left. The text already said 10 mB of molten iron on polished rose quartz. |
| `667F4F686A45EF3E` | stage 2 | Left where the recipe change put it. |
| `5C277D27606F64DA` | stage 3 | Rewritten. The log is planks for the vanilla wheel pattern. The wheel quest is no longer here. |
| `194B973EAF17809C` | stage 3 | Rewritten. The stick is the cobble-and-sticks pattern. Nothing in stage 3 depends on this quest. |
| `2C81D358594C4352` | stage 3 | Left. It still depends on the wheel id. `kubejs/rs_port/chromatic_singularity` consumes a crushing wheel. |
| `47965C310D9847DC` | stage 5 | Rewritten. This cake is the one the brass age used to melt iron. |
| `6F750A66D0A559DE` | stage 5 | Rewritten. Same. |
| `6401030000000000` | primer create | Rewritten. Depends on the primer start `6401000000000000`, not the algal brick. Reward `6301030000000000` is iron nuggets; the reward id is unchanged. |
| `6401090000000001` | primer create | Rewritten. Masonry, not the 2x2 alloy. |
| `6600000000000034` | primer create | Rewritten. The millstone mills netherrack into cinder flour. |
| `6600000000000035` | primer create | Rewritten. Dried kelp does make a belt. |
| `6600000000000039` | primer create | Rewritten. Heated brass, unheated alloy, superheated molten iron. |
| `660000000000003F` | primer create | Rewritten. Tube, casing, button. Depends on the casing and electron tube `5351AF6CA73CB8E0`, not the andesite machine. |
| `6600000000000042` | primer create | Rewritten. Copper casing and dried kelp, or a copper machine. Pours the cake lava and the tube's iron. |
| `660000000000004A` | primer create | Rewritten. Alloy and brass plates. Depends on the brass sheet, not the golden sheet. |
| `660000000000004B` | primer create | Rewritten. Tube, casing, brass hand. Depends on the tube quest, not the andesite machine. It does not build tubes. |
| `660000000000004C` | primer create | Unchanged. Still precision plus the brass machine. |
| `660000000000004D` | primer create | Rewritten. Alloy, copper block, gold plate. Depends on the alloy and the golden sheet. The brass-machine cut is named as optional. |
| `6600000000000101` | primer create | Rewritten. Goggles and the deepslate railway casing. Not the brass hand. |
| `660000000000010C` | primer create | Rewritten. Hand craft first. Depends on stage 1 `76706BE282ABAC31`, not the deployer and the press. |
| `6600000000000060` | primer diesel | Retargeted. `createdieselgenerators:distillation_tank` is not an item. Icon and task are `createdieselgenerators:distillation_controller`. The brass-machine dependency is dropped. The shaped craft is alloy, an iron plate, a clock, and fluid pipes. |
| `6600000000000063` | primer diesel | Rewritten. The well feeds the distillation controller. |
| `6600000000000065` | primer CEI | Task `6700000000000065` is a checkmark. The drain item exists and has no recipe. Icon is the mechanical grindstone. |
| `660000000000006B` | primer aeronautics | Retargeted. `aeronautics:levitite_blend` is a fluid. Icon and task are `aeronautics:levitite_blend_bucket`. Text is the heated mix: four end stone powder, two zinc nuggets, 500 mB water. |

Checked and left alone: primer Create Addition, Let it Walk (sequenced parts already depend on the deployer, precision, or the tube), angel and primer aeronautics machine cuts (andesite, gold, and brass machine recipes are still registered behind `Platform.isLoaded`), the rope pulley (wool recipe, and there is no rope-pulley quest), and the electron tube and precision primer entries, which already described the fill and `kubejs:precision_mechanism`.

### Judgment calls

- Gold machine stays in stage 2, gated on the brass machine, even though `kubejs` shapeless golden sheet + deepslate still makes two railway casings without brass.
- The stage 2 cobble, gravel, and iron nugget quests, both algal brick quests, and the stage 3 log and stick were later deleted. See the note under the progression pass.
- The stage 1 belt quest still waits on rubber. The dried-kelp belt recipe was later removed.
- `6EDA86E4BE0F096B` and the other ids in the same shape (`21753CBB528EAADF`, `28C41C062843C2CA`, `5A8DF2D8553904BF`, `681DACF11F193771`, `28D3347E8FA277AD`, `74CFD83C501C4952`, `5DF206B1738D08A0`, `251A91F6C21AEAFB`, plus two origin task ids) are task ids on overview or origin quests, written into `dependencies`. They were already like that. Replacing them with the parent quest id would hide a large part of each stage behind an overview checkmark, so they were left.
- `aeronautics:pearlescent_levitite` and `create:refined_radiance` had no recipe when this split was written. Both have recipes now. See the progression pass below.
- Spanish, French, Portuguese, Russian, and Chinese cabin files described the algal mix and the deployer assembly under `cabin.quest.73FCA432DEFC1DED` and `cabin.quest.675C395B2074C24C` when this split was written. Those lines, and the other English lines that changed after the locales were last saved, are translated now. The quest body an English client shows is still the snbt literal.

Dedicated server boot after these edits: NeoForge 21.1.265, Java 25. KubeJS logged `Loaded 64/64 KubeJS server scripts in 1.644 s with 0 errors and 1 warnings` (the existing Thermal skip in `recipes/trading.js`). FTB Quests logged `Loaded 5 chapter groups, 43 chapters, 827 quests, 5 reward tables` and no quest error. The 827 count is the previous 822 plus the five new quests. A parser pass found no duplicate quest, task, or reward ids. Every new dependency resolves to a quest id. The pre-existing task-id dependency entries listed above are still not quest ids.

## Progression pass, 2026-11-21

Walked the quest graph and the recipe scripts again after the levitite and radiance recipes and the Transmission Linkage quests. 830 quests, 770 with dependencies, no dependency cycle. Every dependency string resolves to some id. New quest, task, and reward ids are unique.

**Fixed in this pass**

- The English quest file is what an English client shows. Pearlescent levitite no longer says the mod processes it by itself. It names the heated compact. Refined radiance names the lumisene spout. The three linkage quests have titles and descriptions in that file.
- The millstone netherrack to cinder flour recipe is still `kubejs:milling/netherrack_cinder_flour` in `chapter_1.js`. The only live electron tube recipe is the 10 mB `blazinghot:molten_iron` fill in `chapter_2.js`. The crafting-table tube and every other tube output are removed before that fill is added. The Tinkers fill in `chapters.js` sits inside the block comment that starts at line 34.

**Left as judgment calls**

- Regular levitite stays the in-world heat crystallization (torch, campfire, lit blaze burner). There is still no basin recipe for it. Pearlescent is the one that gained a basin recipe, because that quest had no written recipe at all.
- `create:shadow_steel` still has no recipe. Nothing in the quest line asks for it. The dark conversion is the other half of Create's dropped-item mechanic. A basin recipe for it would be a new path, not a repair of a quest.
- The gold machine stays in stage 2, gated on the brass machine, even though a golden sheet and deepslate still make two railway casings without brass.
- The dead-end branches were deleted after this pass: stage 2 Cobblestone `0E46C1FCE5AEB1DF`, Gravel `1139F2E4E6FCFE29`, and Iron Nugget `53735DC4231AB566`; stage 1 Algal Brick `32786E9D145FFE25`; Create primer Algal Brick `6401090000000001`; stage 3 Log `5C277D27606F64DA` and Stick `194B973EAF17809C`. Algal Blend's only child was the brick, so the blend quest remains and nothing else hung off it. Builders Tea still has its other children. Every other quest id was left in place.
- The stage 1 belt quest still waits on rubber. `chapter_1a.js` now removes `create:crafting/kinetics/belt_connector`, Create's six-dried-kelp craft. The shaped recipe of six rubber into three connectors stays. No other mod recipe outputs a belt from kelp. The stage 1 belt text, the rubber text, and the primer belt text say rubber.
- `6EDA86E4BE0F096B`, `21753CBB528EAADF`, `28C41C062843C2CA`, `5A8DF2D8553904BF`, `681DACF11F193771`, `28D3347E8FA277AD`, `74CFD83C501C4952`, `5DF206B1738D08A0`, `251A91F6C21AEAFB`, and the two origin task ids are still written into `dependencies`. They are task ids. Replacing them with the parent quest would hide a stage behind an overview checkmark.
- Quest `2809AE02EA018BFA` ("never") is a custom task that does not complete. The stage 2 nodes and the blobbian nodes that depend on it are hidden decoration. They are not on the machine line.
- Create Enchantment Industry's drain quest `6600000000000065` is still a checkmark. The drain item has no recipe. The icon is the mechanical grindstone.
- The pneumatic connection head and the giant hydraulic rod are described on Precision Linkage and are not their own tasks.
- Cabin translations apply where the snbt value is a `{cabin...}` placeholder. Rewritten quests that store an English literal keep that literal for every language, because there is no per-locale snbt. The cabin files were still brought up to the current English keys.
