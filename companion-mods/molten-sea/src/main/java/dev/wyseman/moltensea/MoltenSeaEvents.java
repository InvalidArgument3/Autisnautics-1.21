package dev.wyseman.moltensea;

import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.level.block.CreateFluidSourceEvent;

public final class MoltenSeaEvents {
    private MoltenSeaEvents() {}

    @SubscribeEvent
    public static void onTags(TagsUpdatedEvent event) {
        if (event.shouldUpdateStaticData()) {
            MoltenFluids.rebuild(event.getRegistryAccess());
        }
    }

    @SubscribeEvent
    public static void onCreateSource(CreateFluidSourceEvent event) {
        if (!Config.selfHeal()) {
            return;
        }
        if (!event.getLevel().dimension().equals(MoltenDepths.DIMENSION)) {
            return;
        }
        FluidState state = event.getFluidState();
        if (MoltenFluids.isSea(state.getType())) {
            event.setCanConvert(true);
        }
    }
}
