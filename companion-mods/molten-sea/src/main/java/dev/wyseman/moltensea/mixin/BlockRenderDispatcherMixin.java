package dev.wyseman.moltensea.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.wyseman.moltensea.Config;
import dev.wyseman.moltensea.DeepSeasBridge;
import dev.wyseman.moltensea.MoltenFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderDispatcher.class, remap = false)
public class BlockRenderDispatcherMixin {
    @Inject(method = "renderLiquid", at = @At("HEAD"), cancellable = true, remap = false)
    private void moltensea$cullInsideHull(BlockPos pos, BlockAndTintGetter level, VertexConsumer consumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        if (!Config.deepSeas() || !MoltenFluids.isSea(fluidState.getType())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && DeepSeasBridge.occluded(minecraft.level, pos)) {
            ci.cancel();
        }
    }
}
