# Portal Linker

Survival players build two frames and link them into a pair of breakable Immersive Portals. The pack jar is not modified. This mod calls `PortalAPI`, `PortalManipulation`, `PortalExtension`, and `NetherPortalGeneration` on `GeneralBreakablePortal`.

Minecraft 1.21.1, NeoForge 21.1.265. Gradle wrapper 8.10.2, ModDevGradle 2.0.141. Compile against the pack jar `.pakku/overrides/mods/ip_aeronautics-2.8.17.jar` (Git LFS). Tested with Immersive Portals core `6.0.7+ipa.2.8.17`.

## Build

From this directory, with Java 21, after `git lfs pull` so the Immersive Portals jar is the real archive:

```
./gradlew build
./gradlew runGameTestServer
```

`build` writes `build/libs/portallinker-1.1.0.jar` and runs the size unit tests. `runGameTestServer` boots a headless server with this mod and the pack's Immersive Portals jar, including the exit-orientation game tests. The jar copied into `.pakku/overrides/mods/` is the one the pack loads. `build/` and `.gradle/` are not part of the pack.

## What a player does

1. Build a rectangle of crying obsidian. Corners are required. The inside is empty air, at least 1 by 2 blocks and at most 32 by 32. The long edge can run east-west, north-south, or flat on the ground.
2. Craft a Portal Linker: crying obsidian, eye of ender, crying obsidian, in a column. The recipe id is `portal_linker:portal_linker`.
3. Right-click one frame. Right-click the other. They can be in different dimensions, and one can be turned relative to the other. A new link uses the exit shown on the linker.
4. Walk through. The default exit is upright and out the front of the other frame: you turn around, and east-west frames do not roll you. Break any frame block and both openings, including the far one, close. If that chunk is not loaded, the close is finished when the chunk loads.
5. Sneak and scroll to choose the exit for the next link. Scroll up is the next mode, scroll down is the previous one, and the hotbar does not move. The action bar says which one, and the tooltip shows the same line. The modes, in order, are: out the front, upright; out the back, upright; rolled 90°; rolled 180°; rolled 270°; flipped. Roll and flip are about the destination opening. Back turns you out the other face of that opening.
6. Sneak-use a block of a linked frame to cycle that link's exit. The action bar names the new exit. Sneak-use in the air, or on a block that is not part of a link, forgets the stored frame and keeps the exit mode on the item. Right-click a frame that is already linked, without sneaking, still says it is already linked.

A 3×5 opening links to a 5×3. It does not link to a 3×6. The portal does not scale the player. Scaling would change collision and turn off Voxy's portal LODs.

## Config

`config/portal_linker-server.toml`

- `frames.maxInterior` — default 32, range 2 to 64. Either interior edge above this is refused.
- `frames.frameBlock` — a block id that counts as a frame, default `minecraft:crying_obsidian`. The tag `portal_linker:portal_frames` counts as well. Add ids with a datapack.
- `catalyst.enabled` — default false. When true, a successful link consumes `catalyst.count` of `catalyst.item` (default one ender pearl). The linker is not consumed.

## Quest gate

The craft is on. `kubejs/server_scripts/recipes/portal_linker.js` has `GATE = false`. Set that to true to remove `portal_linker:portal_linker`, then grant the item from the quest or re-add the recipe in that chapter.

## Claims and ships

If Open Parties and Claims is installed, both frames have to be chunks the player can access, and the link is refused when one end would affect the other claim. The check is skipped when that mod is absent. A lookup failure refuses the link.

A frame on a Sable ship, in Sable's plot coordinates, or in a ship sub-level is refused. The message says to build on the ground. Plot coordinates are only treated as a ship when Sable is installed. Mounting a portal to a moving ship is later work.

## What is stored

Pending selections (the first frame, remembered by the item) and finished links live in the overworld saved data `portal_linker`. Each link stores its exit mode. A link saved before 1.1.0 has no mode and comes back as "out the front, upright" the next time its portals are loaded. The linker item remembers the mode separately, including after the stored frame is cleared. A link that crashes after the glass is placed and before the entities spawn is deleted on the next load, so a restart does not leave a one-sided opening. Breaking a frame while the other chunk is unloaded keeps a pending break until that chunk loads.
