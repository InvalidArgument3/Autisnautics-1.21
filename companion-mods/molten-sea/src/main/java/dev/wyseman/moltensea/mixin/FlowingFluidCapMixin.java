package dev.wyseman.moltensea.mixin;

import dev.wyseman.moltensea.Config;
import dev.wyseman.moltensea.MoltenDepths;
import dev.wyseman.moltensea.MoltenFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FlowingFluid.class, priority = 900, remap = false)
public class FlowingFluidCapMixin {
    @Inject(method = "canSpreadTo", at = @At("HEAD"), cancellable = true, remap = false)
    private void moltensea$holdCaps(BlockGetter level, BlockPos fromPos, BlockState fromState, Direction direction, BlockPos toPos, BlockState toState, FluidState toFluid, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.holdRiftCaps() || direction != Direction.DOWN) {
            return;
        }
        if (!(level instanceof Level world) || !world.dimension().equals(MoltenDepths.DIMENSION)) {
            return;
        }
        if (!MoltenDepths.capColumn(toPos.getY())) {
            return;
        }
        if (MoltenFluids.isSea(fluid)) {
            cir.setReturnValue(false);
        }
    }
}
