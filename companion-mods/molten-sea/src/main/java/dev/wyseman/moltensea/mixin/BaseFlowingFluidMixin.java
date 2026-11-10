package dev.wyseman.moltensea.mixin;

import dev.wyseman.moltensea.Config;
import dev.wyseman.moltensea.MoltenFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BaseFlowingFluid.class, remap = false)
public class BaseFlowingFluidMixin {
    @Inject(method = "canBeReplacedWith", at = @At("HEAD"), cancellable = true, remap = false)
    private void moltensea$refuseSeaOnSea(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.refuseReplacement()) {
            return;
        }
        if (MoltenFluids.bothSea(state.getType(), fluid)) {
            cir.setReturnValue(false);
        }
    }
}
