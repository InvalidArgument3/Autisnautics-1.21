package net.minecraft.client;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;

public class Minecraft {
    public ClientLevel level;

    public static Minecraft getInstance() {
        return new Minecraft();
    }

    public BlockColors getBlockColors() {
        return new BlockColors();
    }
}
