package me.cortex.voxy.server;

import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.config.VoxyConfig;
import me.cortex.voxy.commonImpl.PlatformUtil;
import me.cortex.voxy.commonImpl.VoxyCommon;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Dedicated-server-only operator controls. No client /voxy collision, implicit enable, or protocol change. */
public final class VoxyTerrainCommands {
    private VoxyTerrainCommands() {}
    public static boolean supported(Commands.CommandSelection selection) { return selection == Commands.CommandSelection.DEDICATED; }
    public static void register(RegisterCommandsEvent event) {
        if (!supported(event.getCommandSelection())) return;
        TerrainCommandTree.register(event.getDispatcher(), source -> TerrainCommandTree.isOperator(source::hasPermission), new TerrainCommandTree.Handler<CommandSourceStack>() {
            public int status(CommandSourceStack source) { return showStatus(source); }
            public int set(CommandSourceStack source, TerrainControl.Setting setting, boolean enabled) { return change(source, setting, enabled); }
        });
    }
    private static TerrainControl.State state() {
        return new TerrainControl.State(VoxyConfig.CONFIG.serverTerrainSyncBroadcast, VoxyConfig.CONFIG.serverTerrainSyncBackgroundWalkEnabled);
    }
    private static void checkThread(MinecraftServer server) {
        if (!server.isDedicatedServer() || !server.isSameThread()) throw new IllegalStateException("Dedicated server thread required");
    }
    private static int change(CommandSourceStack source, TerrainControl.Setting setting, boolean enabled) {
        MinecraftServer server = source.getServer();
        try {
            checkThread(server);
            TerrainControl.State previous = state();
            TerrainControl controller = new TerrainControl(new TerrainControl.Runtime() {
                public TerrainControl.State state() { return VoxyTerrainCommands.state(); }
                public void ensureReady() { VoxyDedicatedBootstrap.ensureRuntime(server); }
                public void apply(TerrainControl.State next) {
                    VoxyConfig.CONFIG.serverTerrainSyncBroadcast = next.broadcast();
                    VoxyConfig.CONFIG.serverTerrainSyncBackgroundWalkEnabled = next.background();
                }
                public void cancelPending() { ServerTerrainTransport.clear(); }
                public void resetWalker() { VoxyServerGeneratedChunkWalker.resetScheduling(); }
            }, next -> TerrainCommandSettings.save(PlatformUtil.getConfigDir().resolve("voxy-config.json"), next));
            TerrainControl.State next = controller.set(setting, enabled);
            String message = "Voxy terrain saved: " + describe(next)
                    + ". Effective now; no restart required. Clients must opt in to receiving terrain."
                    + (setting == TerrainControl.Setting.BACKGROUND && !enabled && previous.background() ? " Pending terrain transfers (including live sync) cancelled; subsequent events may retry." : "");
            source.sendSuccess(() -> Component.literal(message), true);
            return 1;
        } catch (Exception failure) {
            Logger.error("Voxy terrain command failed", failure);
            source.sendFailure(Component.literal("Voxy terrain setting was not changed: " + failure.getClass().getSimpleName()
                    + ". Check server log / config permissions; nothing was enabled automatically."));
            return 0;
        }
    }
    private static String describe(TerrainControl.State state) {
        return "broadcast=" + (state.broadcast() ? "on" : "off") + ", background=" + (state.background() ? "on" : "off")
                + ", walker=" + (state.walking() ? "enabled (subject to recipients, interval and MSPT limits)" : state.background() ? "paused: requires broadcast on; broadcasting remains off" : "off");
    }
    private static int showStatus(CommandSourceStack source) {
        checkThread(source.getServer());
        String status = "Voxy terrain: " + describe(state()) + "; runtime="
                + (VoxyCommon.getInstance() instanceof VoxyServerInstance ? "ready" : "not started")
                + "; " + ServerTerrainTransport.status()
                + ". Queues: global 512 batches/16 MiB; per player 128/4 MiB; 2 in flight/player. No setting changed.";
        source.sendSuccess(() -> Component.literal(status), false);
        return 1;
    }
}
