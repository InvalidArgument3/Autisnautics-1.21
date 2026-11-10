# Molten depths sea

The sea is one continuous stack. There are no separator blocks and no catwalks. Placement is one `minecraft:block_column` per column, 256 columns per chunk, anchored at Y=-39. Columns only replace air, so they do not schedule a fluid tick at generation. Surface rules are not used for the metals.

Modern Foundry 1.21.1-1.6.1 gives every one of these metals FluidType density 2000. The order is the stack order already used by the molten-depths feature: aluminum on top, osmium on the bottom.

| Metal | Y inclusive | Blocks |
|---|---:|---:|
| aluminum | 265..294 | 30 |
| zinc | 235..264 | 30 |
| tin | 205..234 | 30 |
| iron | 175..204 | 30 |
| nickel | 145..174 | 30 |
| copper | 115..144 | 30 |
| silver | 85..114 | 30 |
| lead | 54..84 | 31 |
| gold | 23..53 | 31 |
| platinum | -8..22 | 31 |
| osmium | -39..-9 | 31 |

The noise router keeps obsidian at Y<=-40 and Y>=295, and air between those. The gravel pillars are gone. `sea_level` stays -64. `NoiseBasedChunkGenerator.createFluidPicker` uses vanilla lava only when `y < min(-54, seaLevel)`. With sea level -64 that is `y < -64`, which this dimension does not contain. The default fluid (`modernfoundry:molten_iron_fluid`) is selected only for `y < seaLevel`, so it is not placed either. The floor cap is solid obsidian from the density, including Y=-64.

Rift postgen is unchanged. The floor clear is Y=-64 through Y=-40. The ceiling clear is Y=295 through Y=319. The sea is outside both. The bottom End rift is the floor disc. Osmium at Y=-39 sits on that opening. It is a liquid, so it has no collision. `holdRiftCaps` stops it from flowing down into the carved air. The ceiling rift stays air because metal does not flow upward and the sea stops at Y=294.

Fog for the eleven metals is overridden at `kubejs/assets/modernfoundry/hilt/fluid_texture/molten_<metal>.json`. Hilt's `FluidTextureManager` loads `hilt/fluid_texture` through the resource manager, and `ClientTextureFluidType.modifyFogRender` applies `fog.start` and `fog.end`. The override uses start 1 and end 32. Inside a sealed Deep Seas hull the world block is reported as air, so that molten fog is not applied. If the camera is sealed and the real fluid is still molten, the mod cancels fog out to 96 blocks.

The built mod jar is not installed. See `companion-mods/molten-sea/README.md`.

Any world that already generated `autisnautics:molten_depths` keeps the old pools. Reset that dimension, or start a new world, before expecting the stack.

## Fresh-world checklist

No in-game run was done for this change. On a new world, or after deleting `autisnautics:molten_depths`:

1. Seam stability. Stand at each of the ten boundaries in the table. Mine one block of the upper metal. The lower metal stays. The upper metal does not drill a column. Wait at least a minute, then check the neighbours along the seam.
2. Holes. Dig a 1-block hole in the middle of a layer, with the same metal above and beside it. With `selfHealInMoltenDepths` on, it should return to a source. Dig the bottom block of a layer, so the block under it is a different metal. That cell should not become a source of the upper metal.
3. Rift caps. Find the floor disc (boundary 8_9, the End rift) and the ceiling disc (boundary 7_8, the Nether rift). The carved columns are air from Y=-64 through Y=-40 and from Y=295 through Y=319. Osmium does not pour down the floor shaft. The caps outside the disc are still obsidian.
4. Swimming and fire. Enter a metal with no protection. You take lava damage and ignite. You do not drown. Fire Resistance, or the full Create netherite diving set, stops the burn. Movement is the modded-fluid walk, not sprint-swim (`canSwim` is false on these fluids).
5. Contraptions and ships. Move a Create contraption and a Sable ship through one layer and across a seam. Passing through should not start a cascade. Assembling or disassembling in the sea should leave a hole, not a collapsing seam, once the replacement mixin is on.
6. Oxygenator. In each of the eleven layers, submerge a sealed ship with a powered Oxygen Diffuser that has oxygen, and wait past the diffuser's 5 second bubble (100 ticks in 3.4.7). Occupants breathe, are not on fire, and can see across the cabin. The metal mesh inside the hull is hidden. Turn the diffuser off, or redstone it off, and the cabin should stop counting as sealed. Open a hull block below the metal line and confirm the breach. Repeat once with `deepSeasIntegration` false and expect the water-only Deep Seas behaviour.
7. Fog. In the open sea, visibility is on the order of 32 blocks, not 2. Inside the sealed ship it is the cabin, not the molten fog.
8. Chunk generation. Watch the log and a profiler while new chunks of the sea generate. Look for a lighting stall, a fluid-tick backlog (`/fluidlimiter status` if FluidLimiter is installed), and whether fresh chunks already show flowing metal before anything is mined.
