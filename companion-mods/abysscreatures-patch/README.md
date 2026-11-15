# Abyss Creatures 0.2.4

The pack ships a private NeoForge build of Abyss Creatures. There is no upstream source tree in this repo. `build.sh` compiles the AI in this directory against `abysscreatures-0.2.3-neoforge-1.21.1.jar` and writes `abysscreatures-0.2.4-neoforge-1.21.1.jar`. The 0.2.3 classes, assets, mod id, entity ids, and licence stay. New classes and `abysscreatures.mixins.json` are added. `mods.toml` becomes 0.2.4 and gains the mixin config. Attack goal classes are not replaced.

0.2.3's heading gate stays. `Course` still holds yaw when the horizontal steer is under `1e-4`, or under one block and shorter than the vertical gap, and `BoidMoveControl` / `SwimMoveControl` still do not write `yHeadRot`. `HeadingHarness` checks that and does not start Minecraft.

## What 0.2.4 changes

Hostile mobs were not closing on players. Most of them only had a short-range attack goal and a `NearestAttackableTargetGoal` whose search box grew by 4 blocks on Y. Idle movement was `WaterStrollGoal`, which stops choosing a new point once `noActionTime` passes 100. The in-progress swim node then stayed put, and the 0.2.3 heading hold kept them circling that node. Schooling boids sought a target and then cleared it, so with no player they cohered on a stationary centre.

0.2.4 adds a hunt that scans survival players in water, inside the mob's follow range in 3D, with line of sight, every 10 ticks. A pursue goal steers through the water until an existing attack is in range. That attack goal, its animation, its timing, and its damage are the ones already in the jar. Idle points are picked from the mob's current position, with no home. A school shares one of those points, and the point moves. The spine worm keeps its anchor and its reach; it is not given the pursue goal.

## Corpse angler lure

min01's Lovecraft: Beyond the Abyss, branch `1.20.1` (`com.min01.beyondtheabyss`):

- `CorpseAnglerEntity.finalizeSpawn` on a natural spawn moves the mob to the ground. It does not bury it there. Burial happens in `tick` once three full blocks are under it: animation state 3 (burrow), and `onAnimationEnd` then sets `IS_BURROW`.
- `CorpseAnglerEntity.hurt`: while animation state is 3, a player attack whose ray clips the model part `"Bait"` starts animation state 4 (unburrow) at tick 20. There is no `mobInteract`. The clip is a left-click / attack, not a right-click.
- `CorpseAnglerModel` builds `Bait` as the bulb at the end of the illicium. Its child `Gnasher` is the corpse hung on that bulb.
- `CorpseAnglerAmbushGoal.canUse`: once `isBurrow()` is true and the mob already has a target, a target within 2.5 blocks of the `Gnasher` model position starts the ambush (animation state 5).

The settled trigger is that 2.5-block lure distance. The `"Bait"` ray only runs during the burrow animation (state 3), which is the moment the illicium is the part still exposed. The mod page describes them buried on the ground with the angler sticking out. "Below the surface" here is the seabed under the ocean, not a lurk just under the waterline. The port has no `partBuilder`, and its animation ids differ (unburrow is state 3, the dash/ambush is state 1). The bait is a box in front of and above the body, where the straightened illicium holds the bulb. A player attack whose look ray hits that box is the Bait clip, and it counts for the whole time the mob is burrowed, because this port's buried pose is the `BURROW` flag rather than animation state 3. Standing within 2.5 blocks of the same box is the ambush. A hit that misses the box still deals damage and leaves the mob buried. While buried it does not take a target and does not roam. After either trigger the existing `DashGoal` does the dash.
