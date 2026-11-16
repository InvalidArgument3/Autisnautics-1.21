# Portal Render Guard

Client-only companion. It does not edit Sodium, Voxy, or Immersive Portals. The pack loads `portalrenderguard-1.0.1.jar` from `.pakku/client-overrides/mods/` (Git LFS), the same place as `iasodiumfix`. It does not go in `.pakku/overrides/mods`, because that folder is copied onto the server.

Minecraft 1.21.1, NeoForge 21.1.265. Built with `build.sh` and Java 21. The compile classpath is the pack's Voxy ipa10o jar, `ip_aeronautics-2.8.17.jar`, JOML, and a Mojmap remap of the official 1.21.1 client. Those jars are not copied into the mod jar.

## Build

```
./build.sh
```

That compiles against the real jars, runs `ClipMathHarness` and `MixinAudit`, and writes `.pakku/client-overrides/mods/portalrenderguard-1.0.1.jar`.

## What it changes

Voxy draws LODs from inside Sodium's solid pass, including the pass that fills a portal framebuffer.

- `VoxyRenderSystemMixin` (priority 900) saves fixed-function GL state around `renderOpaque` and `renderPortalOpaque` and puts it back through `GlStateManager`. That is stencil, depth (test, function, mask, range), blend (enable, function, equation), colour mask, cull face and winding, scissor, polygon offset, provoking vertex, clip distance 0, depth clamp, draw and read framebuffers, the viewport, the shader program, and the active texture unit. Setters that skip the driver call when their cache already matches are flipped to the other value first, so a raw GL write from Voxy and a cached write from the portal copy both land back on the saved value. Voxy's own cleanup leaves GlStateManager on texture unit 11 and does not restore blend or the depth function. Immersive Portals' `MixinVoxyRenderSystem_PortalView` also injects `renderOpaque`; this mixin runs outside that one.
- `PortalCompositeMixin` turns blending off while `drawPortalAreaWithFramebuffer` copies the portal view onto the portal quad, then restores blend, depth, colour mask, and the viewport through `GlStateManager`. The copy was inheriting the destination translucent pass's blend. The copy calls `_viewport`, `_depthMask(true)`, `_colorMask(true, true, true, true)`, and `_enableDepthTest`, and it does not put them back. A raw `glDepthMask` restore left the cache at true, so the next pass that asked for a true depth mask did not call the driver.

The portal clip plane is not shifted. `activeClipEquation` copies `FrontClipping.getActiveClipPlaneEquationBeforeModelView`, which is `n·(x - camera) + c`. The blit shader unprojects with `inverse(projection * modelView)`, and that modelView is Sodium's camera pose, so the reconstructed point is camera-relative too. 1.0.0 looked up `Minecraft.gameRenderer()` and `Vec3.x()`; `gameRenderer` is a field, so the shift never ran. Applying it would move the plane off the portal. `ClipPlane` still has the formula, and nothing in the mixins calls it.

`compatibilityRenderMode` stays true. This mod does not switch the renderer back to stencil.

If Voxy is not installed, the plugin skips `VoxyRenderSystemMixin`. The portal copy hook still applies. Removing the jar is the rollback.
