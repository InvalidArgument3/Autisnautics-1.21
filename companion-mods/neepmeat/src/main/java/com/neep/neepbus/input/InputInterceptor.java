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

package com.neep.neepbus.input;

import com.neep.meatlib.api.network.ParamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public interface InputInterceptor
{
    default void input(Player source, boolean action, int key)
    {
        if (key == GLFW.GLFW_KEY_ESCAPE)
            InputInterceptors.deactivate(source);
    }

    void movementInput(Player source, boolean action, Movement key);

    @Nullable ResourceLocation handlerId();

    default void open(Player source) { }

    void close(Player source);

    enum Movement
    {
        FORWARDS,
        BACKWARDS,
        LEFT,
        RIGHT,
        UP,
        DOWN;

        public static final ParamCodec<Movement> PARAM_CODEC = ParamCodec.of(Movement.class,
                (t, buf) -> buf.writeVarInt(t != null ?t.ordinal() : -1),
                buf ->
                {
                    int i = buf.readVarInt();
                    return i != -1 ? Movement.values()[i] : null;
                });
    }
}
