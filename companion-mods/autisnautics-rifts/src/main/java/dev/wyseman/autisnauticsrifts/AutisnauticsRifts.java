package dev.wyseman.autisnauticsrifts;

import dev.wyseman.autisnauticsrifts.client.RiftCompassClient;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(AutisnauticsRifts.MOD_ID)
public class AutisnauticsRifts {
    public static final String MOD_ID = "autisnautics_rifts";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RiftTarget>> RIFT_TARGET =
        DATA_COMPONENTS.register("rift_target", () ->
            DataComponentType.<RiftTarget>builder()
                .persistent(RiftTarget.CODEC)
                .networkSynchronized(RiftTarget.STREAM_CODEC)
                .build()
        );

    public static final DeferredItem<RiftCompassItem> RIFT_COMPASS = ITEMS.register("rift_compass",
        () -> new RiftCompassItem(new Item.Properties().stacksTo(1)));

    public AutisnauticsRifts(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, RiftConfig.SPEC);
        ITEMS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::onClientSetup);
        }
        NeoForge.EVENT_BUS.addListener(RiftEntityGuard::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(RiftPostgenCarver::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(RiftPostgenCarver::onServerTick);
        NeoForge.EVENT_BUS.addListener(RiftRepairCommand::onRegisterCommands);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(RIFT_COMPASS);
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(RiftCompassClient::registerItemProperties);
    }
}
