package dev.wyseman.autisnauticsrifts.mixin;

import dev.wyseman.autisnauticsrifts.RiftConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.peripheral.dim_stack.DimStackManagement;

@Mixin(value = DimStackManagement.class, remap = false)
public class DimStackBedrockMixin {
    @Inject(method = "replaceBedrock", at = @At("HEAD"), cancellable = true)
    private static void autisnautics$onlyReplaceBedrockAtRifts(ServerLevel level, ChunkAccess chunk, CallbackInfo ci) {
        if (RiftConfig.BEDROCK_ONLY_AT_RIFTS.get()) {
            ci.cancel();
        }
    }
}
