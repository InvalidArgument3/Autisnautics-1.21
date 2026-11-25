package me.cortex.voxy;

import me.cortex.voxy.client.VoxyClientNetworkHooks;
import me.cortex.voxy.common.network.TerrainSyncClientHandler;
import me.cortex.voxy.client.VoxyCommands;
import me.cortex.voxy.commonImpl.VoxyCommon;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

public final class VoxyNeoForgeClientEvents {
    private VoxyNeoForgeClientEvents() {}

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        if (VoxyCommon.isAvailable()) {
            event.getDispatcher().register(VoxyCommands.register());
        }
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        VoxyClientNetworkHooks.onClientTick();
        TerrainSyncClientHandler.tickDrainApplyQueue();
    }

    @SubscribeEvent
    public static void onClientChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ClientLevel level)) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        VoxyClientNetworkHooks.queueSeenChunk(level.dimension().location(), chunk.getPos().x, chunk.getPos().z);
    }
}
