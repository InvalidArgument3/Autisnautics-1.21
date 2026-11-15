# Portal Render Guard

Client-only companion. It does not edit Sodium, Voxy, or Immersive Portals. The pack loads `portalrenderguard-1.0.0.jar` from `.pakku/client-overrides/mods/` (Git LFS), the same place as `iasodiumfix`.

Minecraft 1.21.1, NeoForge 21.1.265. Built with `build.sh` and Java 21. Stubs under `stub/` exist only so `javac` can see the mixin targets. They are not in the jar.

## Build

```
./build.sh
```

That compiles, runs `ClipMathHarness`, and writes `.pakku/client-overrides/mods/portalrenderguard-1.0.0.jar`.

## What it changes

Voxy draws LODs from inside Sodium's solid pass, including the pass that fills a portal framebuffer.

- `VoxyRenderSystemMixin` saves fixed-function GL state around `renderOpaque` and `renderPortalOpaque` and puts it back. That is stencil, depth (test, function, mask, range), blend (enable, function, equation), colour mask, cull face and winding, scissor, polygon offset, provoking vertex, clip distance 0, depth clamp, draw and read framebuffers, the viewport, the shader program, and the active texture unit. Voxy's own cleanup leaves GlStateManager on texture unit 11 and does not restore blend or the depth function.
- `VoxyClipMixin` shifts the portal clip plane from camera-relative space into world space. The blit shader reconstructs a world position with `inverse(projection * modelView)` and dots it with the plane. Immersive Portals' equation is `n·(x - camera) + c`. Without the shift, a rift clips LODs at the wrong height.
- `PortalCompositeMixin` turns blending off while `drawPortalAreaWithFramebuffer` copies the portal view onto the portal quad, then restores blend, depth, and the viewport. The copy was inheriting the destination translucent pass's blend, and it left the viewport on the framebuffer size.

`compatibilityRenderMode` stays true. This mod does not switch the renderer back to stencil.

If Voxy is not installed, the plugin skips the two Voxy mixins. The portal copy hook still applies. Removing the jar is the rollback.
