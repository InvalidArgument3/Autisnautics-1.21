# Pack compatibility

Checked against `pakku-lock.json` (Minecraft 1.21.1, NeoForge 21.1.265, 402 projects) and the Git LFS names in `.pakku/overrides/mods/`. The override jars in this checkout are LFS pointers, so their `neoforge.mods.toml` files were not readable here.

## Mod ids

This jar registers one mod id: `neepmeat`.

Upstream's Loom build also nested two other mods:

- `meatlib` (the library subproject)
- `myron-neepmeat` (the OBJ loader fork)

Neither id is registered in this phase, so a later phase can still claim them. `meatweapons` and `neepbus` are package and data namespaces inside the NeepMeat jar, not separate Fabric mod ids.

A search of `pakku-lock.json` slugs, names, and filenames, plus a workspace text search, found no `neepmeat`, `meatlib`, `neepbus`, `meatweapons`, or `myron`. That does not cover mod ids hidden inside jars that were not extracted.

## Mixins

This phase ships no mixins. Upstream had 92 mixin classes (`neepmeat.mixins.json`, `meatweapons.mixins.json`, `meatlib.mixins.json`, `myron_neepmeat.mixins.json`) aimed at 1.20.1 Yarn names. They were not copied, because those targets do not exist on NeoForge 1.21.1 and several of them sit on classes the pack already patches:

| Upstream mixin area | Pack mod that also touches it |
| --- | --- |
| Entity, living entity, mob, player, server world | Lithium `lithium-neoforge-0.15.4+mc1.21.1`, C2ME `c2me-neoforge-mc1.21.1-0.4.0-alpha.0.123` |
| Chunk | C2ME, ModernFix `modernfix-neoforge-5.27.27+mc1.21.1` |
| Rendering, camera, held item, HUD, particles | Sodium `sodium-neoforge-0.8.13+mc1.21.1`, Iris `iris-neoforge-1.8.14-beta.1+mc1.21.1` |
| Enderman teleport (`inhibit_teleport`) | Immersive Portals inside `ip_aeronautics-2.8.17.jar` (the kubejs guard in `rift_fresh_entity_guard.js` names `imm_ptl`) |
| Model / memory | FerriteCore `ferritecore-7.0.3-neoforge` |
| Ship / physics | Sable `sable-neoforge-1.21.1-2.0.6`, Create Aeronautics `create-aeronautics-bundled-1.21.1-1.3.2` |

Voxy is not a pakku-lock project. It is the LFS override `voxy-0.2.17-alpha-sodium08-portal-lods-ipa10i.jar`. Its mixins were not read.

Any future mixin has to be re-targeted at Mojmap 1.21.1 and tried with Sodium, Iris, Lithium, C2ME, Immersive Portals, Voxy, and Sable before it ships.

## Flywheel

Upstream `build.gradle` depends on `dev.engine-room.flywheel:flywheel-fabric-1.20.1:1.0.6-278`. `settings.gradle` has a commented `include 'subprojects:Flywheel'`. This checkout has no Flywheel jar and no Flywheel submodule. The "vendored jar" is not in the pinned commit.

This port does not bundle Flywheel.

Create `create-1.21.1-6.0.10.jar` (sha1 `0e97e49837bed766e6f28a4c95b04885d6acc353` from the lock file) declares a required dependency `flywheel` version `[1.0.0,2.0)` in its `neoforge.mods.toml`. The lock file and the override filenames contain no Flywheel jar. Client jars that are easy to confuse with it: `nowheel-2.1.1+1.21.1neoforge.jar`, `ponderjs-neoforge-1.21.1-2.4.0.jar`, `colorwheel-neoforge-1.3.0+mc1.21.1.jar`, `Ping-Wheel-1.12.2-neoforge-1.21.1.jar`. None of those filenames is Flywheel.

NeepMeat's own large flywheel block is commented out in upstream `NMBlocks`. Flywheel-instanced machine renderers were not ported. There is no fallback renderer yet, because those machines are not registered. When they are, the fallback should be a plain block model or GeckoLib, not a second Flywheel.

## Recipe viewers

Both are in the pack:

- JEI `jei-1.21.1-neoforge-19.64.0.455.jar`, side BOTH. Required by the port brief. The dev runtime uses this version. `NeepMeatJeiPlugin` loads and registers no categories.
- EMI `emi-1.1.24+1.21.1+neoforge.jar`, side CLIENT, plus EMI addons. `NeepMeatEmiPlugin` (`@EmiEntrypoint`) loads and registers nothing.

Upstream's EMI and REI plugins (33 and 23 classes) target custom recipe types that do not exist yet. They were not ported. JEI is the viewer that must gain categories first. EMI can follow once the recipe types exist. The dev game-test server loaded both jars; EMI's datapack is marked incompatible on a dedicated server in the crash-report datapack line from the first failed boot, and the successful boot still loaded EMI because it is on the dev runtime classpath. The dedicated server also reached "Done" with EMI on that classpath. Shipping EMI only on the client, as the lock file does, is the right pack layout.

## GeckoLib

Pack jar: `geckolib-neoforge-1.21.1-4.9.3.jar`. This project depends on `software.bernie.geckolib:geckolib-neoforge-1.21.1:4.9.3` as `runtimeOnly` (not packed into the mod jar). `neoforge.mods.toml` requires `geckolib` `[4.9.3,5)`.

The mod constructor logs `NeepMeat 0.30.0-neoforge.1 loaded with GeckoLib 4.9.3`. No `GeoBlockEntity` or animation json is registered. Upstream entities that need GeckoLib (integrator, hound, worms, weapons) are not ported. GeckoLib's own mixin refmap warning in dev ("geckolib.refmap.json could not be read") is from their dev jar and is not a NeepMeat mixin.

## Other pack notes

Sodium is in the pack, so the old Fabric Indium requirement does not apply. NeoForge GeckoLib and Sodium are the pack's versions. Continuity (optional connected textures upstream) is not a substitute for anything in this phase; connected textures were not ported.
