package dev.wyseman.portalrenderguard;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Skip the Voxy hooks when that mod is not installed. The portal copy hook stays. */
public final class GuardMixinPlugin implements IMixinConfigPlugin {
    private boolean voxy;

    @Override
    public void onLoad(String mixinPackage) {
        this.voxy = true;
        try {
            Class<?> loading = Class.forName("net.neoforged.fml.loading.LoadingModList");
            Object modList = loading.getMethod("get").invoke(null);
            if (modList != null) {
                Object file = modList.getClass().getMethod("getModFileById", String.class).invoke(modList, "voxy");
                this.voxy = file != null;
            }
        } catch (Throwable ignored) {
            this.voxy = true;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("VoxyRenderSystemMixin") || mixinClassName.endsWith("VoxyClipMixin")) {
            return this.voxy;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
