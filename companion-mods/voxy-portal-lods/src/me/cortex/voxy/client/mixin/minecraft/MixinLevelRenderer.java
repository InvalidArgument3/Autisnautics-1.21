package me.cortex.voxy.client.mixin.minecraft;

import me.cortex.voxy.client.VoxyClientInstance;
import me.cortex.voxy.client.compat.CrossDimPortalManager;
import me.cortex.voxy.client.compat.ImmPortalsCompat;
import net.minecraft.client.Minecraft;
import me.cortex.voxy.common.config.VoxyConfig;
import me.cortex.voxy.client.core.IGetVoxyRenderSystem;
import me.cortex.voxy.client.core.VoxyRenderSystem;
import me.cortex.voxy.client.core.util.IrisUtil;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer implements IGetVoxyRenderSystem {
    @Shadow private ClientLevel level;
    @Unique private VoxyRenderSystem renderer;

    @Override
    public VoxyRenderSystem voxy$getRenderSystem() {
        return this.renderer;
    }

    @Inject(method = "allChanged()V", at = @At("RETURN"), order = 900)//We want to inject before sodium
    private void voxy$reloadVoxyRenderer(CallbackInfo ci) {
        this.voxy$shutdownRenderer();
        if (this.level != null) {
            //Secondary (Immersive Portals) LevelRenderers get a renderer lazily once they become the main one
            if (ImmPortalsCompat.isPresent() && (Object) Minecraft.getInstance().levelRenderer != (Object) this) {
                return;
            }
            this.voxy$createRenderer();
        }
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void voxy$captureSetWorld(ClientLevel world, CallbackInfo ci) {
        if (this.level != world) {
            this.voxy$shutdownRenderer();
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void voxy$injectClose(CallbackInfo ci) {
        this.voxy$shutdownRenderer();
    }

    @Override
    public void voxy$shutdownRenderer() {
        CrossDimPortalManager.onShutdown(this);
        if (this.renderer != null) {
            Logger.info("Shutting down Voxy renderer attached to LevelRenderer");
            this.renderer.shutdown();
            this.renderer = null;
        }
    }

    @Override
    public void voxy$createRenderer() {
        if (this.renderer != null) throw new IllegalStateException("Cannot have multiple renderers");
        if (ImmPortalsCompat.isRenderingPortal()) {
            return;//Never create mid portal pass
        }
        CrossDimPortalManager.onPrimaryCreating(this);
        Logger.info("Attempting to create Voxy renderer: enabled=" + VoxyConfig.CONFIG.enabled
                + ", renderingEnabled=" + VoxyConfig.CONFIG.isRenderingEnabled()
                + ", hasInstance=" + (VoxyCommon.getInstance() != null)
                + ", levelNull=" + (this.level == null));
        if (!VoxyConfig.CONFIG.enabled) {
            Logger.info("Not creating renderer due to disabled");
            return;
        }
        if (!VoxyConfig.CONFIG.isRenderingEnabled()) {
            Logger.info("Not creating renderer due to disabled rendering");
            return;
        }
        if (this.level == null) {
            Logger.error("Not creating renderer due to null world");
            return;
        }
        var instance = (VoxyClientInstance)VoxyCommon.getInstance();
        if (instance == null) {
            Logger.error("Not creating renderer due to null instance");
            return;
        }
        WorldEngine world = WorldIdentifier.ofEngine(this.level);
        if (world == null) {
            Logger.error("Null world selected");
            return;
        }
        try {
            this.renderer = new VoxyRenderSystem(world, instance.getServiceManager(), this.level, false);
            CrossDimPortalManager.setPrimary(this);
            Logger.info("Voxy renderer created for world " + world);
        } catch (RuntimeException e) {
            if (IrisUtil.irisShaderPackEnabled()) {
                IrisUtil.disableIrisShaders();
            } else {
                throw e;
            }
        }
        instance.updateDedicatedThreads();
    }

    @Override
    public void voxy$createSecondaryRenderer() {
        if (this.renderer != null) {
            return;
        }
        if (ImmPortalsCompat.isRenderingPortal()) {
            return;//Never create mid portal pass
        }
        if (!VoxyConfig.CONFIG.enabled || !VoxyConfig.CONFIG.isRenderingEnabled() || this.level == null) {
            return;
        }
        var instance = (VoxyClientInstance) VoxyCommon.getInstance();
        if (instance == null) {
            return;
        }
        WorldEngine world = WorldIdentifier.ofEngine(this.level);
        if (world == null) {
            return;
        }
        ImmPortalsCompat.beginRendererCreation(this.level);
        try {
            this.renderer = new VoxyRenderSystem(world, instance.getServiceManager(), this.level, true);
            Logger.info("Cross-dimension secondary Voxy renderer created for world " + world);
        } finally {
            ImmPortalsCompat.endRendererCreation();
        }
        instance.updateDedicatedThreads();
    }
}
