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

package com.neep.neepmeat.implant.entity;

import com.neep.neepmeat.NeepMeat;
import com.sun.jna.platform.win32.WinBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class CatEarsImplant implements EntityImplant
{
    public static final ResourceLocation ID = new ResourceLocation(NeepMeat.NAMESPACE, "cat_ears");
    public static final ResourceLocation CAT_EARS_PLAYER_SKIN = new ResourceLocation(NeepMeat.NAMESPACE, "skin");
    public static final ResourceLocation CAT_EARS_COLOUR = new ResourceLocation(NeepMeat.NAMESPACE, "colour");

    public static final int INNER_PINK = 0xffd9a5a5;

    private final Entity entity;

    private ResourceLocation skin = CAT_EARS_COLOUR;
    private int col = 0xffffffff;
    private int innerCol = INNER_PINK;

    public CatEarsImplant(Entity entity)
    {
        this.entity = entity;
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void tick(Runnable remove)
    {
    }

    @Override
    public void clientTick()
    {
        if (entity instanceof ImplantRenderingHelper helper)
        {
            helper.neepmeat$setCatEars(this);
        }
    }

    @Override
    public void onUninstall()
    {
        if (entity instanceof ImplantRenderingHelper helper)
        {
            helper.neepmeat$setCatEars(null);
        }
    }

    public void setAppearance(ResourceLocation skin, int col, int innerCol)
    {
        this.skin = skin;
        this.col = col;
        this.innerCol = innerCol;
    }

    public int getCol()
    {
        return col;
    }

    public int getInnerCol()
    {
        return innerCol;
    }

    public ResourceLocation getSkin()
    {
        return skin;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putString("skin", skin.toString());
        nbt.putInt("col", col);
        nbt.putInt("inner_col", innerCol);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        this.skin = ResourceLocation.tryParse(nbt.getString("skin"));
        this.col = nbt.getInt("col");
        this.innerCol = nbt.getInt("inner_col");
    }
}
