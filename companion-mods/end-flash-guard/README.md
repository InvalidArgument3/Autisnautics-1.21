# End Flash Guard

Client-only companion. It does not edit VanillaBackport or Immersive Portals. The pack loads `endflashguard-1.0.0.jar` from `.pakku/client-overrides/mods/` (Git LFS). It does not go in `.pakku/overrides/mods`, because that folder is copied onto the server.

Minecraft 1.21.1, NeoForge 21.1.265. Built with `build.sh` and Java 21. The compile classpath is VanillaBackport `1.2.2` (sha1 `12871e5330f8b33df7740a975047fc9a584d0387`) and a Mojmap remap of the official 1.21.1 client. The audit also reads `ip_aeronautics-2.8.17.jar`. Those jars are not copied into the mod jar.

## Build

```
./build.sh
```

That compiles against the real jars, runs `FlashGateHarness` and `MixinAudit`, and writes `.pakku/client-overrides/mods/endflashguard-1.0.0.jar`.

## What it changes

Immersive Portals creates a `ClientLevel` for every dimension at login, including `minecraft:the_end`, and ticks the ones the player is not standing in. For that tick it points `Minecraft.level` at the background level. VanillaBackport attaches `EndFlashState` to every client level whose sky type is `END`, and `EndFlashRenderer.tick` plays `minecraft:weather.end_flash` ("End Flash rumbles") on the shared sound manager.

`EndFlashRendererMixin` cancels `tick` unless the `ClientLevel` argument is the level remembered from an unswitched tick. `render` and `calculateLightIntensity` use the same remembered level, so a portal render that has swapped `Minecraft.level` does not draw the sky flash or change the light for a background End. The real End still ticks, plays, and draws, because that tick is not switched.

Removing the jar is the rollback.
