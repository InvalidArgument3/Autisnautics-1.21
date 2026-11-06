Autisnautics Molten Calderas (hemisphere-split, pure non-alloy fluids only)
===========================================================================
Worldborder: 50 km × 50 km (KubeJS kubejs/server_scripts/worldborder_50km.js sets
  Overworld WorldBorder center 0,0 size 50000 → X/Z ∈ [-25000, +25000]).
Natural Temperature LINEAR (mode 2): equatorial_distance = 25000.0 so
  Z=-25000 → t=-1.0 (polar cold), Z=0 → t=0, Z=+25000 → t=+1.0 (scorching).

Bands (wavy Z via lithostitched:axis + offset noise ±~220):
  B1 0.75–5 km : N tin/zinc | S copper/glass     rarity 1/8000 each
  B2 5–10 km   : N iron/aluminum/lead | S iron/clay/obsidian  1/10000
  B3 10–15 km  : N silver/nickel/amethyst | S gold/quartz     1/13000
  B4 15–20 km  : N cobalt/emerald | S uranium                 1/18000
  B5 20–25 km  : N diamond/ender | S debris                   1/25000

Placement: surface heightmap + Y∈[212,610] (rejects ocean Y≈188) + solid
footprint + replaceable air dome. Contained 13×8×13 NBT caldera basins.
Fluid blocks are modernfoundry:molten_*_fluid (1000 mB per source block;
90 mB = 1 ingot). NO alloys (no brass/steel/knightslime/netherite/etc).

Testing tips (new chunks only — already-generated chunks will not retrogen):
  /locate does not list these (placed features, not structures).
  Fly north/south to band Z and scout sky-island plateaus; or use a
  fresh superflat-like test world with the datapack for density checks.
