# Abyss Creatures 0.2.3 heading patch

The pack ships a private NeoForge build of Abyss Creatures. There is no source tree in this repo. `build.sh` reads `abysscreatures-0.2.2-neoforge-1.21.1.jar` (the working tree, or git `cc244d46` through Git LFS) and writes `abysscreatures-0.2.3-neoforge-1.21.1.jar`.

0.2.2 turns a mob whenever the steering vector's 3D length is at least `2.5e-7`, using `atan2` on x and z, then copies that yaw onto `yBodyRot` and `yHeadRot`. A mostly vertical vector, or two fish stacked closer than a ten-thousandth of a block, makes that angle noise. In a water column the same thing happens once the fish drifts off the node: the waypoint is still above them, and the yaw target swings as they creep. `LookAtTargetGoal` only calls `setLookAt`. The spin is the move control, which was also resetting `yHeadRot` every tick before the look control ran, so the head could move at most one look step off the body.

`Course` keeps the previous heading when the horizontal length is under `1e-4`, and when the horizontal gap is under one block and shorter than the vertical gap. Otherwise it turns toward an exponential average of the horizontal part. `yHeadRot` is left to the look control. Separation skips a neighbour at or below `1e-4` or beyond 1.5 blocks, and a neighbour closer than 0.35 blocks is pushed at a fixed length instead of by a gain times the tiny gap.

`HeadingHarness` runs the 0.2.2 formula and `Course` on a mostly-vertical vector, co-located fish, and a one-block column. It does not start Minecraft.
