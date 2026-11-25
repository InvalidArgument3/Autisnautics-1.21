package me.cortex.voxy.client.mixin.sodium;

import me.cortex.voxy.client.VoxyClient;
import me.cortex.voxy.client.compat.CrossDimPortalManager;
import me.cortex.voxy.client.compat.ImmPortalsCompat;
import me.cortex.voxy.client.core.IGetVoxyRenderSystem;
import me.cortex.voxy.client.core.rendering.Viewport;
import me.cortex.voxy.client.core.util.IrisUtil;
import me.cortex.voxy.common.Logger;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public abstract class MixinDefaultChunkRenderer extends ShaderChunkRenderer {
    @Unique
    private boolean voxy$renderedThisFrame;
    @Unique
    private static int voxy$renderLogCount;

    public MixinDefaultChunkRenderer(RenderDevice device, ChunkVertexType vertexType) {
        super(device, vertexType);
    }

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    private void cancelThingie(ChunkRenderMatrices matrices, CommandList commandList, ChunkRenderListIterable renderLists, TerrainRenderPass renderPass, CameraTransform camera, boolean indexedRenderingEnabled, CallbackInfo ci) {
        if (renderPass == DefaultTerrainRenderPasses.SOLID || renderPass == DefaultTerrainRenderPasses.TRANSLUCENT) {
            this.voxy$renderedThisFrame = false;
        }
        if (VoxyClient.disableSodiumChunkRender()) {
            super.begin(renderPass);
            this.doRender(matrices, renderPass, camera);
            super.end(renderPass);
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/ShaderChunkRenderer;end(Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;)V", shift = At.Shift.BEFORE))
    private void injectRender(ChunkRenderMatrices matrices, CommandList commandList, ChunkRenderListIterable renderLists, TerrainRenderPass renderPass, CameraTransform camera, boolean indexedRenderingEnabled, CallbackInfo ci) {
        this.doRender(matrices, renderPass, camera);
    }

    @Unique
    private void doRender(ChunkRenderMatrices matrices, TerrainRenderPass renderPass, CameraTransform camera) {
        if (renderPass != DefaultTerrainRenderPasses.SOLID && renderPass != DefaultTerrainRenderPasses.CUTOUT) {
            return;
        }
        if (this.voxy$renderedThisFrame) {
            return;
        }
        //Immersive Portals/Aeronautics nested portal pass: LODs are drawn for both same-dimension portals
        // (primary LevelRenderer) and cross-dimension portals (budgeted secondary VoxyRenderSystem created
        // safely on the next main frame by CrossDimPortalManager). Never lazily create mid portal pass.
        if (ImmPortalsCompat.isRenderingPortal()) {
            if (!ImmPortalsCompat.canRenderLodsInCurrentPortal()) {
                return;
            }
            var portalLr = (IGetVoxyRenderSystem) Minecraft.getInstance().levelRenderer;
            var portalRenderer = portalLr.voxy$getRenderSystem();
            if (portalRenderer == null) {
                CrossDimPortalManager.requestSecondary(portalLr);
                return;
            }
            if (portalRenderer.isSecondary()) {
                CrossDimPortalManager.markSecondaryUsed(portalLr);
            }
            var portalViewport = portalRenderer.setupPortalViewport(matrices, camera.x, camera.y, camera.z);
            if (portalViewport != null) {
                portalRenderer.renderPortalOpaque(portalViewport);
            }
            this.voxy$renderedThisFrame = true;
            return;
        }

        var levelRenderer = (IGetVoxyRenderSystem) Minecraft.getInstance().levelRenderer;
        var renderer = levelRenderer.voxy$getRenderSystem();
        // If we just stepped through a cross-dimension portal into a dimension that had a budgeted secondary
        // renderer, upgrade it to a full-budget primary renderer.
        if (renderer != null && renderer.isSecondary()) {
            Logger.info("Upgrading secondary cross-dimension Voxy renderer to primary after dimension switch");
            levelRenderer.voxy$shutdownRenderer();
            renderer = null;
        }
        if (renderer == null && Minecraft.getInstance().level != null) {
            Logger.info("Voxy render hook saw null renderer during " + renderPass + "; attempting lazy creation");
            levelRenderer.voxy$createRenderer();
            renderer = levelRenderer.voxy$getRenderSystem();
        }
        if (voxy$renderLogCount < 12) {
            voxy$renderLogCount++;
            Logger.info("Voxy render hook pass=" + renderPass + ", rendererPresent=" + (renderer != null)
                    + ", shadersEnabled=" + IrisUtil.irisShaderPackEnabled());
        }
        if (renderer != null) {
            Viewport<?> viewport = null;
            if (IrisUtil.irisShaderPackEnabled()) {
                viewport = renderer.getViewport();
            } else {
                viewport = renderer.setupViewport(matrices, camera.x, camera.y, camera.z);
            }
            renderer.renderOpaque(viewport);
            this.voxy$renderedThisFrame = true;
        }
        CrossDimPortalManager.onMainFrame(levelRenderer);
    }
}
