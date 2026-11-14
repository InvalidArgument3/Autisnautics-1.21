# Voxy portal-LOD ipa10j

The pack ships a private NeoForge build of Voxy, `voxy-0.2.17-alpha-sodium08-portal-lods-ipa10j.jar`, from `.pakku/overrides/mods`. There is no Voxy source tree in this repo. The jar is Voxy 0.2.17-alpha retargeted at NeoForge 1.21.1 and Sodium 0.8, plus the Immersive Portals: Aeronautics portal-LOD changes (`ImmPortalsCompat`, `PortalPassState`, portal viewports in `ViewportSelector` and `VoxyRenderSystem`). Those changes are not in a public fork. ipa10j is the ipa10i jar with only the tint and timing crash patched, so the rest of the file stays byte-for-byte.

`build.sh` reads the ipa10i jar (git history, before it was replaced), compiles `ColourTintSafety`, and writes ipa10j. The script checks that every other zip entry is unchanged and runs `TintSafetyHarness`.

Kill switch, unchanged: `-Dvoxy.portalLods=false`.
