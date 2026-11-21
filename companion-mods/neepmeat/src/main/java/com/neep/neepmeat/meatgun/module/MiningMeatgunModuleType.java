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

package com.neep.neepmeat.meatgun.module;

import com.neep.meatweapons.meatgun.module.AttributeContainer;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import net.fabricmc.yarn.constants.MiningLevels;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;

public class MiningMeatgunModuleType<T extends MeatgunModule> extends MeatgunModule.Type<T>
{
    private final Tier toolMaterial;

    public MiningMeatgunModuleType(Tier toolMaterial, AttributeContainer attributes, MeatgunModule.Factory<T> factory, MeatgunModule.NbtFactory<T> nbtFactory)
    {
        super(attributes, factory, nbtFactory);
        this.toolMaterial = toolMaterial;
    }

    public int miningLevel()
    {
        return toolMaterial.getLevel();
    }

    public float miningSpeed()
    {
        return toolMaterial.getSpeed();
    }

    public boolean harvestable(BlockState state)
    {
        int level = toolMaterial.getLevel();
        if (level < MiningLevels.DIAMOND && state.is(BlockTags.NEEDS_DIAMOND_TOOL))
        {
            return false;
        }
        else if (level < MiningLevels.IRON && state.is(BlockTags.NEEDS_IRON_TOOL))
        {
            return false;
        }
        else
        {
            return level < MiningLevels.STONE && state.is(BlockTags.NEEDS_STONE_TOOL) ? false : true;
        }
    }
}
