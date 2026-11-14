/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.keybinding.v1;

import com.mojang.blaze3d.platform.InputConstants;
import com.neep.meatlib.event.FabricClientEvents;
import net.minecraft.client.KeyMapping;

public final class KeyBindingHelper {
    private KeyBindingHelper() {}

    public static KeyMapping registerKeyBinding(KeyMapping key) {
        FabricClientEvents.key(key);
        return key;
    }

    public static InputConstants.Key getBoundKeyOf(KeyMapping key) {
        return key.getKey();
    }
}
