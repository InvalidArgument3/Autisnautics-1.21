package dev.wyseman.portallinker;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class PortalLinkerEvents {
    private PortalLinkerEvents() {}

    public static void onNeighbor(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        Links.get(level.getServer()).onBlockChanged(level, pos);
    }

    public static void onTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 != 0) {
            return;
        }
        Links.get(event.getServer()).sweep(event.getServer());
    }
}
