package dev.wyseman.autisnauticsrifts;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class RiftEntityGuard {
    private RiftEntityGuard() {}

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity.xo == 0.0 && entity.yo == 0.0 && entity.zo == 0.0) {
            entity.xo = entity.getX();
            entity.yo = entity.getY();
            entity.zo = entity.getZ();
        }
    }
}
