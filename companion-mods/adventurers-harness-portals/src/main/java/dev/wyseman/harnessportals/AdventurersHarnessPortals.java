package dev.wyseman.harnessportals;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(AdventurersHarnessPortals.MOD_ID)
public class AdventurersHarnessPortals {
    public static final String MOD_ID = "adventurers_harness_portals";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);

    public static final DeferredHolder<Item, DiamondWarriorHarnessItem> DIAMOND_WARRIOR_HARNESS = ITEMS.register(
            "diamond_warrior_harness",
            () -> new DiamondWarriorHarnessItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(528)
                    .rarity(Rarity.RARE))
    );

    private static final ResourceKey<CreativeModeTab> HARNESS_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath("adventurers_harness", "main")
    );

    public AdventurersHarnessPortals(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::registerPayloads);
        modBus.addListener(this::addCreative);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(
                        PortalTargetPayload.TYPE,
                        PortalTargetPayload.STREAM_CODEC,
                        PortalTargetPayload::handle
                );
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(HARNESS_TAB) || event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
            event.accept(DIAMOND_WARRIOR_HARNESS.get());
        }
    }
}
