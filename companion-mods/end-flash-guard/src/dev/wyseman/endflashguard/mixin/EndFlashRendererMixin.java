package dev.wyseman.endflashguard.mixin;

import com.blackgear.vanillabackport.client.api.modules.end_flashes.EndFlashState;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.wyseman.endflashguard.EndFlashGuard;
import dev.wyseman.endflashguard.EndFlashPolicy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * VanillaBackport ticks {@code EndFlashState} from every End {@code ClientLevel}
 * and plays {@code minecraft:weather.end_flash} on the shared sound manager.
 * Immersive Portals keeps a background End level and ticks it after the
 * player's level, with {@code Minecraft.level} swapped to that background level.
 * Cancelling here drops the tick, the delayed sound, and the static lightmap
 * dirty flag. Sky and light read {@code Minecraft.level} later in the frame,
 * including after a portal render has swapped it, so those two check the level
 * remembered from the unswitched tick.
 */
@Mixin(targets = "com.blackgear.vanillabackport.client.api.modules.end_flashes.EndFlashRenderer", remap = false)
public class EndFlashRendererMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void endflash$tick(Minecraft minecraft, ClientLevel level, EndFlashState state, CallbackInfo ci) {
        EndFlashPolicy policy = EndFlashGuard.POLICY;
        boolean worldSwitched = EndFlashGuard.worldSwitched();
        policy.observe(minecraft.level, worldSwitched);
        if (!policy.allowsTick(level, worldSwitched)) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private static void endflash$render(PoseStack poseStack, Minecraft minecraft, CallbackInfo ci) {
        if (!EndFlashGuard.POLICY.allowsVisual(minecraft.level)) {
            ci.cancel();
        }
    }

    @Inject(method = "calculateLightIntensity", at = @At("HEAD"), cancellable = true, remap = false)
    private static void endflash$light(Minecraft minecraft, float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (!EndFlashGuard.POLICY.allowsVisual(minecraft.level)) {
            cir.setReturnValue(0f);
        }
    }
}
