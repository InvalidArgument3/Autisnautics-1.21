package dev.wyseman.moltensea;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public final class MoltenSeaClient {
    private MoltenSeaClient() {}

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        if (!Config.deepSeas()) {
            return;
        }
        Camera camera = event.getCamera();
        Entity entity = camera.getEntity();
        if (entity == null || !(entity.level() instanceof Level level)) {
            return;
        }
        if (!DeepSeasBridge.sealed(level, entity.getEyePosition()) && !DeepSeasBridge.sealed(level, entity.position())) {
            return;
        }
        BlockPos at = BlockPos.containing(camera.getPosition());
        FluidState real = DeepSeasBridge.realFluid(level, at);
        boolean molten = MoltenFluids.isSea(real.getType()) || MoltenFluids.isSeaType(entity.getEyeInFluidType());
        if (!molten) {
            return;
        }
        event.setNearPlaneDistance(1.0F);
        event.setFarPlaneDistance(96.0F);
        event.setCanceled(true);
    }
}
