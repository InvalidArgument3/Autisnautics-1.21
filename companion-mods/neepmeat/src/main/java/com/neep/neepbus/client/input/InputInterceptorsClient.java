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

package com.neep.neepbus.client.input;

import com.google.common.base.Suppliers;
import com.neep.meatlib.client.event.KeyboardEvents;
import com.neep.neepbus.input.InputInterceptor;
import com.neep.neepbus.input.InputInterceptors;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class InputInterceptorsClient
{
    private static final Map<ResourceLocation, InputInterceptorHandler.Factory<?>> OVERLAYS = new HashMap<>();

    private static boolean ACTIVE = false;
    @Nullable private static InputInterceptorHandler HANDLER = null;

    // List of key bindings that are likely to be down when an interceptor is activated.
    // If these are not cancelled, the player may start continuously walking with no way to stop themselves.
    private static final Supplier<List<KeyMapping>> KEYS_TO_CANCEL = Suppliers.memoize(() ->
    {
        Minecraft client = Minecraft.getInstance();
        var options = client.options;
        return List.of(options.keyUse, options.keyAttack, options.keyUp, options.keyDown, options.keyLeft, options.keyRight, options.keyJump, options.keyShift);
    });

    public static <T extends InputInterceptorHandler> InputInterceptorHandler.Factory<T> register(ResourceLocation id, InputInterceptorHandler.Factory<T> factory)
    {
        OVERLAYS.put(id, factory);
        return factory;
    }

    public static void init()
    {
        InputInterceptors.ACTIVATE.receiver(Dist.CLIENT, InputInterceptorsClient::setActive);

        KeyboardEvents.PRE_INPUT.register((window, key, scancode, action, modifiers) ->
        {
            if (ACTIVE)
            {
                if (key == GLFW.GLFW_KEY_ESCAPE)
                {
                    signalDeactivate();
                    return true;
                }

                InputInterceptor.Movement movement = getMovement(key, scancode);
                if (movement != null)
                {
                    if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_RELEASE)
                        InputInterceptors.EVENT.emitter(null).apply(action != GLFW.GLFW_RELEASE, key, movement);

                    return true;
                }
            }
            return false;
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
        {
            if (HANDLER != null)
            {
                HANDLER.render(drawContext, tickDelta);
            }
        });

        WorldRenderEvents.BLOCK_OUTLINE.register((worldRenderContext, blockOutlineContext) ->
        {
            if (HANDLER != null)
            {
                return HANDLER.onRenderOutline(worldRenderContext, blockOutlineContext);
            }
            return true;
        });
    }

    @Nullable
    private static InputInterceptor.Movement getMovement(int keycode, int scancode)
    {
        Minecraft client = Minecraft.getInstance();
        var options = client.options;

        // Horrible
        if (options.keyUp.matches(keycode, scancode))
            return InputInterceptor.Movement.FORWARDS;
        else if (options.keyDown.matches(keycode, scancode))
            return InputInterceptor.Movement.BACKWARDS;
        else if (options.keyLeft.matches(keycode, scancode))
            return InputInterceptor.Movement.LEFT;
        else if (options.keyRight.matches(keycode, scancode))
            return InputInterceptor.Movement.RIGHT;
        else if (options.keyJump.matches(keycode, scancode))
            return InputInterceptor.Movement.UP;
        else if (options.keyShift.matches(keycode, scancode))
            return InputInterceptor.Movement.DOWN;

        return null;
    }

    private static void setActive(boolean active, @Nullable ResourceLocation id)
    {
        ACTIVE = active;

        if (ACTIVE)
        {
            for (var binding : KEYS_TO_CANCEL.get())
            {
                binding.setDown(false);
            }
        }

        if (id != null)
        {
            InputInterceptorHandler.Factory<?> factory = OVERLAYS.get(id);
            if (factory != null)
            {
                HANDLER = factory.create();
            }
        }
        else
        {
            HANDLER = null;
        }
    }

    public static boolean isActive()
    {
        return ACTIVE;
    }

    public static void signalDeactivate()
    {
        if (HANDLER != null && HANDLER.acceptScreen(Minecraft.getInstance().screen))
            Minecraft.getInstance().player.closeContainer();

        InputInterceptors.ACTIVATE.emitter(null).apply(false, null);
    }

    @Nullable
    public static InputInterceptorHandler currentHandler()
    {
        return HANDLER;
    }
}
