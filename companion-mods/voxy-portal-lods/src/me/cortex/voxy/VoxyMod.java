package me.cortex.voxy;

import me.cortex.voxy.client.config.VoxyConfigScreenRegistration;
import me.cortex.voxy.common.network.VoxyNetworking;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.cortex.voxy.server.VoxyDedicatedBootstrap;
import me.cortex.voxy.server.VoxyServerGeneratedChunkWalker;
import me.cortex.voxy.server.VoxyServerChunkSync;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Supplier;

@Mod(VoxyMod.MOD_ID)
public final class VoxyMod {
    public static final String MOD_ID = "voxy";

    public VoxyMod(IEventBus modEventBus, ModContainer modContainer) {
        VoxyNetworking.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(VoxyDedicatedBootstrap::onServerAboutToStart);
        NeoForge.EVENT_BUS.addListener(VoxyDedicatedBootstrap::onServerStopped);
        NeoForge.EVENT_BUS.addListener(me.cortex.voxy.server.VoxyTerrainCommands::register);
        VoxyServerChunkSync.register();
        VoxyServerGeneratedChunkWalker.register();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(VoxyNeoForgeClientEvents.class);
            Supplier<IConfigScreenFactory> factorySupplier = () -> VoxyConfigScreenRegistration::createScreen;
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, factorySupplier);
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        VoxyCommon.onInitialize();
    }
}
