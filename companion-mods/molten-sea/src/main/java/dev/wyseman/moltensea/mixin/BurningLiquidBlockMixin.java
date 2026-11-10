package dev.wyseman.moltensea.mixin;

import dev.wyseman.moltensea.Config;
import dev.wyseman.moltensea.compat.DeepSeasTags;
import dev.wyseman.moltensea.DeepSeasBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applied only when modernfoundry is loaded. Signature checked against
 * TinkersConstruct-Unofficial-Port 1.21.1-1.6.1 entityInside(BlockState, Level, BlockPos, Entity).
 */
@Mixin(targets = "modernmods.modernfoundry.fluids.block.BurningLiquidBlock", remap = false)
public class BurningLiquidBlockMixin {
    @Inject(method = "entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void moltensea$skipSealedOccupant(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!DeepSeasTags.DEEP_SEAS_PRESENT || entity == null || !Config.deepSeas()) {
            return;
        }
        if (DeepSeasBridge.sealed(level, entity.getEyePosition()) || DeepSeasBridge.sealed(level, entity.position())) {
            ci.cancel();
        }
    }
}
