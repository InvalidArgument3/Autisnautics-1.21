# NEEPMeat (NeoForge 1.21.1 port)

Companion mod for the Autisnautics pack. Phase 1 of a port of [NeepMeat](https://codeberg.org/MeatWheeze/NeepMeat) by MeatWheeze from Fabric 1.20.1 to NeoForge 1.21.1.

Minecraft 1.21.1, NeoForge 21.1.265. The Gradle wrapper is pinned to 8.10.2 and ModDevGradle 2.0.141. Java toolchain 21, which is the bytecode Java 25 runs. This environment booted the game on JDK 21. It was not booted on Java 25.

Upstream pin: `0d36776e4e78fd4049cbe9f556a5204d5e98c4ff` on `1.20/dev`, version `0.30.0-beta+1.20.1`. There is no git tag named `v0.30.0-beta`.

This jar is not the finished mod. It loads, registers 136 building blocks (full blocks, pillars, stairs, slabs, walls, dyed tiles, painted corrugated asbestos, reinforced glass), and includes the NEEPSpeak library. Machines, pipes, the PLC, weapons, fluids, entities, and worldgen are not in this build. See `docs/PORT_STATUS.md`.

## Build

From this directory, with Java 21:

```
./gradlew build
```

The jar is `build/libs/neepmeat-0.30.0-neoforge.71.jar`. A sources jar is written beside it. `build/`, `run/`, and `.gradle/` are not part of the pack.

`./gradlew test` runs the NEEPSpeak parser unit test. `./gradlew runGameTestServer` boots a headless NeoForge server and runs the three in-mod game tests. `./gradlew runClient` and `./gradlew runServer` are the dev client and dedicated server.

## Where the jar goes

This repository does not install the jar. Local pack mods that are not on Modrinth or CurseForge are stored with Git LFS at `.pakku/overrides/mods/` and marked both-sides, the same place as `fluidlimiter-1.0.0-local.jar` and `noheighttemp-1.0.0-local.jar`. Pakku copies that folder into the instance `mods/` directory for the client and the server. Drop `neepmeat-0.30.0-neoforge.71.jar` there when a later phase is actually playable. Do not put it in the gitignored top-level `mods/` folder of a player instance as the way the pack is distributed.

The mod has to be on the server and the client once blocks exist, which they do. GeckoLib 4.9.3 (already in the pack) is a required dependency. JEI and EMI are optional.

## Docs

- `docs/PORT_STATUS.md` — what is ported, stubbed, missing, or blocked
- `docs/COMPATIBILITY.md` — mod ids, mixins, Flywheel, JEI/EMI, GeckoLib
- `docs/ATLAS_REVIEW.md` — recipe and progression proposals, not applied
- `docs/LICENCE.md` — LGPL/GPL split and what was not redistributed
- `NOTICE` — upstream commit, permission note, submodule pins
