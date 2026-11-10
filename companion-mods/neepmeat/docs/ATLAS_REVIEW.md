# For Atlas review

Nothing in this section is implemented. No KubeJS script was added, and no Create recipe was changed. The building-block recipes that did ship are the upstream NeepMeat crafting and stonecutting recipes whose ingredients already exist (iron, stone, dyes, the blocks this phase registers). They are not a new Autisnautics progression.

Atlas owns balance. Please treat the following as proposals.

## Do not gate Create on NeepMeat

NeepMeat's machines are a parallel factory, not a replacement for Create crushing, mixing, or brass. Until the machines exist and Atlas signs off, NeepMeat should stay optional decoration plus, later, a side branch. Do not add NeepMeat items to chapter gates in `kubejs/server_scripts/recipes/chapters/`.

## When machines exist, these are the overlaps

| NeepMeat | Create in this pack (`create-1.21.1-6.0.10`) | Proposal |
| --- | --- | --- |
| Crusher / grinder, large crusher | Crushing wheels, millstone | Leave Create as the default ore path. If the NeepMeat crusher is enabled, its ore recipes should not be strictly better. A KubeJS script can remove NeepMeat ore-crushing recipes and keep mob/meat recipes. |
| Alloy kiln | Create basin + blaze burner, or the pack's existing alloy path | Do not add a second iron/brass alloy that skips the pack's chapter order. |
| Mixer, bottler | Create spout, mixer, hose | Fluid transfer should use NeoForge fluid handlers so Create pipes can connect, once those blocks are ported. No recipe change required for that. |
| Item pipes, chutes, barrels | Create funnels, chutes, vaults | Same idea for item handlers. Do not delete Create logistics. |
| Blood, ichor, and the vascular conduit | Create fluid pipes | Blood should be a normal NeoForge fluid, not a Fabric transfer-only fluid. Whether it belongs in the Create tank whitelist is an Atlas call. |
| PLC / NEEPSpeak | Create sequenced assembly, ponder, Computer-less logistics | The PLC is a programming puzzle. It should not be required to finish Create chapters. |

## KubeJS shape, if Atlas wants a bridge later

One server script, `kubejs/server_scripts/key_mods/neepmeat.js`, loaded only when `Platform.isLoaded('neepmeat')`. It would:

- remove NeepMeat recipes that duplicate a chapter's Create recipe
- add at most one optional "dirty" recipe that turns a Create byproduct into a NeepMeat building ingredient (for example polished metal already crafts from iron nuggets and ingots, so it needs no Create input)
- not touch `minecraft:` or `create:` recipe ids except by `event.remove` of a NeepMeat id

That file does not exist in this change.

## Building blocks that are already craftable

These are upstream recipes, called out so they are not a surprise:

- `neepmeat:polished_metal` is 32 from iron ingots and nuggets
- rough concrete dyes from vanilla concrete plus sand, then re-dyes inside `#neepmeat:rough_concrete`
- smooth tiles dye from smooth stone and from `#neepmeat:smooth_tile`
- `neepmeat:blood_bubble_planks` is in `#minecraft:planks`, so vanilla stick and wood recipes accept it. That matches the upstream block settings (`PLANKS`). Say if that tag should come off until the tree exists
- bloody bricks and bloody tiles reference `neepmeat:blood_bubble`, which is not registered, so those recipes were not copied

## Not in this phase

No meat, no weapons, no mob drops, no ore nodes, no worldgen, no enlightenment, no PLC programs. There is nothing to price yet.
