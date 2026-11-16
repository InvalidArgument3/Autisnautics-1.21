# Voxy portal-LOD ipa10m

The pack ships a private NeoForge build of Voxy, `voxy-0.2.17-alpha-sodium08-portal-lods-ipa10m.jar`, from `.pakku/overrides/mods`. There is no Voxy source tree in this repo. The jar is Voxy 0.2.17-alpha retargeted at NeoForge 1.21.1 and Sodium 0.8, plus the Immersive Portals: Aeronautics portal-LOD changes (`ImmPortalsCompat`, `PortalPassState`, portal viewports in `ViewportSelector` and `VoxyRenderSystem`). Those changes are not in a public fork.

ipa10j patched the ipa10i tint crash. ipa10k made the inner radius follow each player's view distance. ipa10l makes the square-ring walker the only sender. ipa10m starts that ring on the view-distance square, skips columns the player already has without spending the send budget, and does not stall on a chunk that is not saved yet. `build.sh` reads the ipa10l jar, compiles the ring classes, and writes ipa10m. It runs `StreamHarness`, `RingHarness`, and `TintSafetyHarness`.

Kill switch, unchanged: `-Dvoxy.portalLods=false`. Server LOD packets still require `server_terrain_sync_broadcast` in `config/voxy-config.json`. An inner radius of 0 means "use the player's view distance"; a positive value is a fixed override. Set `server_terrain_sync_debug` to true for the periodic walker summary. The wire protocol is unchanged.
