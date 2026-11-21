/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatweapons.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.neep.meatlib.client.event.KeyboardEvents;
import com.neep.meatlib.client.event.UseAttackCallback;
import com.neep.meatlib.client.input.KeyBindingAccess;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.item.GunItem;
import com.neep.meatweapons.meatgun.MeatgunModuleCapability;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class MWKeys
{
    public static KeyMapping AIRTRUCK_DOWN;
    public static KeyMapping USE_ITEM_WITH_MEATGUN;

//    public static KeyBinding MEATGUN_TRIGGER_1;
//    public static KeyBinding MEATGUN_TRIGGER_2;

    // For fixing conflicts when the meatgun triggers and use/attack bindings use the same keys
    // Appears to work, but breaks when other mods are present.
//    @Nullable private static KeyBinding ACTUAL_TRIGGER_1 = null;
//    @Nullable private static KeyBinding ACTUAL_TRIGGER_2 = null;

    public static KeyMapping registerKeyBinding(String namespace, String id, String category, int def)
    {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping("key." + namespace + "." + id,
                InputConstants.Type.KEYSYM,
                def,
                "key." + namespace + ".category." + category));
    }

    public static KeyMapping registerVirtualKeyBinding(String namespace, String id, String category, int def)
    {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping("key." + namespace + "." + id,
                InputConstants.Type.MOUSE,
                def,
                "key." + namespace + ".category." + category));
    }

    public static boolean trigger1Held;
    public static boolean trigger2Held;

    public static void registerKeybinds()
    {
        AIRTRUCK_DOWN = registerKeyBinding(MeatWeapons.NAMESPACE, "down", "general", GLFW.GLFW_KEY_BACKSLASH);
        USE_ITEM_WITH_MEATGUN = registerKeyBinding(MeatWeapons.NAMESPACE, "use_item", "general", GLFW.GLFW_KEY_R);
//        RELOAD = registerKeyBinding(MeatWeapons.NAMESPACE, "reload", "general", GLFW.GLFW_KEY_R);
//        MEATGUN_TRIGGER_1 = registerVirtualKeyBinding(MeatWeapons.NAMESPACE, "trigger_1", "general", GLFW.GLFW_MOUSE_BUTTON_1);
//        MEATGUN_TRIGGER_2 = registerVirtualKeyBinding(MeatWeapons.NAMESPACE, "trigger_2", "general", GLFW.GLFW_MOUSE_BUTTON_2);
//        ADS = registerKeyBinding(MeatWeapons.NAMESPACE, "ads", "general", GLFW.);

        KeyboardEvents.POST_INPUT.register((window, key, scancode, action, modifiers) ->
        {
            onKey(window, key, scancode, action, modifiers);
            return false;
        });

        // Suppress use item and attack events when we are listening for key release events
        UseAttackCallback.DO_USE.register((client, player) -> !trigger1Held);
        UseAttackCallback.DO_ATTACK.register((client, player) -> !trigger2Held);
    }

    private static void sendTrigger(Level world, Player player, @Nullable GunItem main, @Nullable GunItem off,
                                    int trigger, double pitch, double yaw, MWAttackC2SPacket.HandType handType, MWAttackC2SPacket.ActionType actionType)
    {
        if (main != null)
        {
            if (actionType.pressed())
                main.triggerClient(world, player, player.getItemInHand(InteractionHand.MAIN_HAND), trigger, pitch, yaw, handType);
        }

        if (off != null)
        {
            if (actionType.pressed())
                off.triggerClient(world, player, player.getItemInHand(InteractionHand.OFF_HAND), trigger, pitch, yaw, handType);
        }
        MWAttackC2SPacket.CHANNEL.emitter(player).apply(trigger, pitch, yaw, handType, actionType);
    }

    private static KeyMapping fixUseAttackKey(KeyMapping requested, InputConstants.Key m1, InputConstants.Key m2, Minecraft client)
    {
        var k1 = KeyBindingHelper.getBoundKeyOf(requested);
        if (k1 == m1)
            return client.options.keyUse;
        else if (k1 == m2)
            return client.options.keyAttack;

        return requested;
    }

    private static void onKey(long window, int key, int scancode, int action, int modifiers)
    {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null)
            return;

        ItemStack mainStack = client.player.getMainHandItem();
        ItemStack offStack = client.player.getOffhandItem();
        GunItem mainHand = MeatWeapons.redirectClicks(mainStack);
        GunItem offHand = MeatWeapons.redirectClicks(offStack);

        Level world = client.level;

        KeyMapping trigger1 = client.options.keyUse;
        KeyMapping trigger2 = client.options.keyAttack;

        keyBlock: if (mainHand != null || offHand != null)
        {
            if (USE_ITEM_WITH_MEATGUN.isDown())
            {
                ((KeyBindingAccess) client.options.keyUse).meatlib$setPressedOnce();
                return;
            }

            MWAttackC2SPacket.HandType handType = MWAttackC2SPacket.HandType.of(mainHand != null, offHand != null);

//            var camera = client.gameRenderer.getCamera();
            double pitch = Math.toRadians(client.player.getXRot());
            double yaw = Math.toRadians(client.player.getYRot());
//            double pitch = camera.getPitch() * MathHelper.RADIANS_PER_DEGREE;
//            double yaw = camera.getYaw() * MathHelper.RADIANS_PER_DEGREE;

            // Dual wielding
            // Redirect secondary trigger to the offhand
            if (mainHand != null && mainHand.redirectTrigger(mainStack, MeatgunModuleCapability.USE)
                    && offHand != null && offHand.redirectTrigger(offStack, MeatgunModuleCapability.USE))
            {
                if (trigger1.isDown())
                {
                    if (!trigger1Held)
                        sendTrigger(world, client.player, mainHand, offHand,
                                MWAttackC2SPacket.TRIGGER_PRIMARY, pitch, yaw, MWAttackC2SPacket.HandType.MAIN, MWAttackC2SPacket.ActionType.PRESS);
                    trigger1Held = true;
                }
                else if (trigger1Held)
                {
                    trigger1Held = false;
                    sendTrigger(world, client.player, mainHand, offHand,
                            MWAttackC2SPacket.TRIGGER_PRIMARY, pitch, yaw, MWAttackC2SPacket.HandType.MAIN, MWAttackC2SPacket.ActionType.RELEASE);
                }

                if (trigger2.isDown())
                {
                    if (!trigger2Held)
                        sendTrigger(world, client.player, mainHand, offHand,
                                MWAttackC2SPacket.TRIGGER_PRIMARY, pitch, yaw, MWAttackC2SPacket.HandType.OFF, MWAttackC2SPacket.ActionType.PRESS);
                    trigger2Held = true;
                }
                else if (trigger2Held)
                {
                    trigger2Held = false;
                    sendTrigger(world, client.player, mainHand, offHand,
                            MWAttackC2SPacket.TRIGGER_PRIMARY, pitch, yaw, MWAttackC2SPacket.HandType.OFF, MWAttackC2SPacket.ActionType.RELEASE);
                }

                break keyBlock;
            }

            if ((mainHand != null && mainHand.redirectTrigger(mainStack, MeatgunModuleCapability.USE))
                    || (offHand != null && offHand.redirectTrigger(offStack, MeatgunModuleCapability.USE)))
            {
                if (trigger1.isDown())
                {
                    if (!trigger1Held)
                        sendTrigger(world, client.player, mainHand, offHand,
                                MWAttackC2SPacket.TRIGGER_PRIMARY, pitch, yaw, handType, MWAttackC2SPacket.ActionType.PRESS);
                    trigger1Held = true;
                }
                else if (trigger1Held)
                {
                    trigger1Held = false;
                    sendTrigger(world, client.player, mainHand, offHand,
                            MWAttackC2SPacket.TRIGGER_PRIMARY, pitch, yaw, handType, MWAttackC2SPacket.ActionType.RELEASE);
                }

                // Suppress the base game's input processing.
                while (trigger1.consumeClick()) { }
            }

            if (offHand != null ||
                    (mainHand != null && mainHand.redirectTrigger(mainStack, MeatgunModuleCapability.ATTACK))) // Attack only uses main hand item
            {
                if (trigger2.isDown())
                {
                    if (!trigger2Held)
                        sendTrigger(world, client.player, mainHand, offHand,
                                MWAttackC2SPacket.TRIGGER_SECONDARY, pitch, yaw, handType, MWAttackC2SPacket.ActionType.PRESS);
                    trigger2Held = true;
                }
                else if (trigger2Held)
                {
                    trigger2Held = false;
                    sendTrigger(world, client.player, mainHand, offHand, MWAttackC2SPacket.TRIGGER_SECONDARY, pitch, yaw, handType, MWAttackC2SPacket.ActionType.RELEASE);
                }

                // Suppress the base game's input processing.
                while (trigger2.consumeClick()) { }
            }
        }
        else
        {
            // Release held keys when the special item is no longer held.
            // This should fix switching to a new item without releasing the key.
            if (trigger1Held)
            {
                double pitch = Math.toRadians(client.player.getXRot());
                double yaw = Math.toRadians(client.player.getYRot());
                sendTrigger(world, client.player, mainHand, offHand,
                        MWAttackC2SPacket.TRIGGER_PRIMARY, pitch, yaw, MWAttackC2SPacket.HandType.BOTH, MWAttackC2SPacket.ActionType.RELEASE);

                trigger1Held = false;
            }

            if (trigger2Held)
            {
                double pitch = Math.toRadians(client.player.getXRot());
                double yaw = Math.toRadians(client.player.getYRot());
                sendTrigger(world, client.player, mainHand, offHand,
                        MWAttackC2SPacket.TRIGGER_SECONDARY, pitch, yaw, MWAttackC2SPacket.HandType.BOTH, MWAttackC2SPacket.ActionType.RELEASE);

                trigger2Held = false;
            }
        }
    }
}
