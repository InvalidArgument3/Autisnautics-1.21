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

package com.neep.neepmeat.plc.block.entity;

import com.neep.neepbus.input.InputInterceptor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class ImplanterInputInterceptor implements InputInterceptor
{
    @Nullable public Player controller;

    public boolean forward;
    public boolean back;
    public boolean left;
    public boolean right;
    public boolean up;
    public boolean down;

    public ImplanterInputInterceptor()
    {

    }

    @Override
    public void movementInput(Player source, boolean action, Movement key)
    {
        switch (key)
        {
            case FORWARDS -> forward = action;
            case BACKWARDS -> back = action;
            case LEFT -> left = action;
            case RIGHT -> right = action;
            case UP -> up = action;
            case DOWN -> down = action;
        }
    }

    @Override
    public void open(Player source)
    {
        this.controller = source;
    }

    @Override
    public void close(Player source)
    {
        this.controller = null;

        forward = false;
        back = false;
        left = false;
        right = false;
        up = false;
        down = false;
    }

    @Override
    public @Nullable ResourceLocation handlerId()
    {
        return ImplanterBlockEntity.ID;
    }
}
