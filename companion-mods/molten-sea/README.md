# Molten Sea

Companion NeoForge mod for the Autisnautics pack. It keeps the eleven Modern Foundry metals in `autisnautics:molten_depths` from erasing each other, and it asks a running Create Deep Seas oxygen system to treat that sea the way it treats water.

Minecraft 1.21.1, NeoForge 21.1.265. The Gradle wrapper is pinned to 8.10.2 and ModDevGradle 2.0.141.

## Build

From this directory, with Java 21:

```
./gradlew build
```

The jar is `build/libs/moltensea-1.0.0.jar`. A sources jar is written beside it. `build/` and `.gradle/` are not part of the pack.

## Where the jar goes

This repository does not install the jar. Local pack mods that are not on Modrinth or CurseForge are stored with Git LFS at `.pakku/overrides/mods/` and marked both-sides, the same place as `fluidlimiter-1.0.0-local.jar` and `noheighttemp-1.0.0-local.jar`. Pakku copies that folder into the instance `mods/` directory for the client and the server. Drop `moltensea-1.0.0.jar` there when you are ready to ship it. Do not put it in the gitignored top-level `mods/` folder of a player instance as the way the pack is distributed.

The mod has to be on the server and the client. The replacement mixin runs with the world, and the hull render and fog hooks run on the client.

## Tag

`autisnautics:molten_sea` lives in this jar (`data/autisnautics/tags/fluid/molten_sea.json`), not in a Paxi datapack. The mixin is useless without the membership list, and removing the mod removes the tag with it. Each entry is `required: false`, so a missing fluid id is skipped instead of failing datapack load. A datapack can still append ids.

The ids are the fluids, not the blocks. On Modern Foundry 1.21.1-1.6.1 the blocks are `modernfoundry:molten_<metal>_fluid`. The fluids are `modernfoundry:molten_<metal>` and `modernfoundry:flowing_molten_<metal>`. The tag lists both forms for aluminum, zinc, tin, iron, nickel, copper, silver, lead, gold, platinum, and osmium.

## What it changes

Server config `config/moltensea-server.toml`, all default on:

- `refuseSeaReplacement` injects at the head of `BaseFlowingFluid.canBeReplacedWith` and returns false when both fluids are in the tag. Horizontal replacement was already false. Downward replacement is the overwrite.
- `selfHealInMoltenDepths` listens for `CreateFluidSourceEvent` and allows a tagged fluid in `autisnautics:molten_depths` to become a source. A cell on top of a different metal still cannot, because that neighbour is not the same fluid and not a solid block.
- `holdRiftCaps` refuses downward spread of a tagged fluid into Y<=-40 or Y>=295 in that dimension, so a tick cannot drain the sea into the carved rift.
- `deepSeasIntegration` makes tagged fluids pass `is(create_submarine:water)` while Create Deep Seas is loaded. It also skips molten contact damage for a sealed occupant, skips drawing those fluids inside an occluded hull, and pushes fog out to 96 blocks when a sealed camera is still in molten metal.

Create Deep Seas is not a compile dependency. Its compartment methods are called by reflection. The burning-block mixin is registered only when `modernfoundry` is loaded. Neither mod missing prevents this jar from loading.

## Create Deep Seas

The pack mod is CurseForge "Create Deep Seas [BETA]", file `create_submarine-3.4.7.jar` (sha1 `5aa320830166e195cf857cdc0432e499a5e237c2`). One jar registers `create_submarine` ("Create Deep Seas"), `create_abyss` ("The Abyss"), and `create_high_seas` ("Create High Seas"). The oxygen block is `oxygene_diffuser` (Oxygen Diffuser). There is also a creative oxygenator. Server config `oxygenMaxFillBlocks` is the scan cap for that diffuser.

A running diffuser scans the ship and `CompartmentTracker` then lies about world blocks inside the sealed volume, including every fluid, not only water. Entity fluid height is cleared for a sealed occupant whose plot cell is not water. This mod extends the water-tag checks, the burn hook, the liquid renderer, and fog so molten metal follows that sealed-hull path. It does not edit the Deep Seas jar.

Side effect while `deepSeasIntegration` is on: anything in Deep Seas that asks "is this `create_submarine:water`?" also says yes for these metals. That includes flood inlets, plugs, thrusters, ballast, mines, boats, and the electrolyzer tank filter. A breach still places vanilla water where Deep Seas hardcodes `Blocks.WATER`. Their `liquidDensities` table still defaults unknown fluids to 1.0; these metals have FluidType density 2000, so an operator can add `#autisnautics:molten_sea=2.0` to `create_submarine-server.toml` if hull pressure should match that. This repo does not change that config.
