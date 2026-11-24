package dev.wyseman.autisnauticsrifts.client;

import dev.wyseman.autisnauticsrifts.AutisnauticsRifts;
import dev.wyseman.autisnauticsrifts.RiftTarget;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;

public final class RiftCompassClient {
    private RiftCompassClient() {}

    public static void registerItemProperties() {
        ItemProperties.register(
            AutisnauticsRifts.RIFT_COMPASS.get(),
            ResourceLocation.withDefaultNamespace("angle"),
            new CompassItemPropertyFunction((level, stack, entity) -> {
                RiftTarget target = stack.get(AutisnauticsRifts.RIFT_TARGET.get());
                if (target == null || !target.found()) {
                    return null;
                }
                if (!level.dimension().location().toString().equals(target.dimension())) {
                    return null;
                }
                return GlobalPos.of(level.dimension(), target.blockPos());
            })
        );
    }
}
