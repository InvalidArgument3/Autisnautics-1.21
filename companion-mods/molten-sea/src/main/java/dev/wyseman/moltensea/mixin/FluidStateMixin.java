package dev.wyseman.moltensea.mixin;

import dev.wyseman.moltensea.Config;
import dev.wyseman.moltensea.MoltenFluids;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidState.class, remap = false)
public class FluidStateMixin {
    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void moltensea$asDeepSeasWater(TagKey<Fluid> tag, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.deepSeas() || !DeepSeasTags.isWater(tag)) {
            return;
        }
        if (MoltenFluids.isSea(((FluidState) (Object) this).getType())) {
            cir.setReturnValue(true);
        }
    }
}
