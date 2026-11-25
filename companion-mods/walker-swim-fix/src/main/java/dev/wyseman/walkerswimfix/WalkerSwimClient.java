package dev.wyseman.walkerswimfix;

import com.mojang.blaze3d.platform.InputConstants;
import dev.letitwalk.client.PilotInput;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class WalkerSwimClient {
    public static final KeyMapping TOGGLE_SWIM = new KeyMapping(
        "key.walkerswimfix.toggle_swim",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_B,
        "key.categories.let_it_walk"
    );

    private WalkerSwimClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(WalkerSwimClient::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(WalkerSwimClient::onClientTick);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_SWIM);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        while (TOGGLE_SWIM.consumeClick()) {
            if (PilotInput.isActive()) {
                PacketDistributor.sendToServer(new WalkerSwimFix.ToggleSwimPayload());
            }
        }
    }
}
