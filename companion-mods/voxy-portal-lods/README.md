# Voxy portal-LOD ipa10k

The pack ships a private NeoForge build of Voxy, `voxy-0.2.17-alpha-sodium08-portal-lods-ipa10k.jar`, from `.pakku/overrides/mods`. There is no Voxy source tree in this repo. The jar is Voxy 0.2.17-alpha retargeted at NeoForge 1.21.1 and Sodium 0.8, plus the Immersive Portals: Aeronautics portal-LOD changes (`ImmPortalsCompat`, `PortalPassState`, portal viewports in `ViewportSelector` and `VoxyRenderSystem`). Those changes are not in a public fork.

ipa10j patched the ipa10i tint crash. ipa10k patches ipa10j's dedicated-server LOD stream: the inner radius follows each player's view distance, the existing square-ring walker stays nearest-first, and fast movement pauses it. `build.sh` reads the ipa10j jar (git history, before it was replaced), compiles `TerrainStreamPolicy`, and writes ipa10k. The script checks that every other zip entry is unchanged and runs `StreamHarness` and `TintSafetyHarness`.

Kill switch, unchanged: `-Dvoxy.portalLods=false`. Server LOD packets still require `server_terrain_sync_broadcast` in `config/voxy-config.json`. An inner radius of 0 means "use the player's view distance"; a positive value is a fixed override.
