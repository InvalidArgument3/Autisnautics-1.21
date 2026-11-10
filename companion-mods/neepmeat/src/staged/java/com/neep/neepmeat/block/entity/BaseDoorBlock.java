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

package com.neep.neepmeat.block.entity;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

public class BaseDoorBlock extends DoorBlock implements MeatlibBlock
{
    public BaseDoorBlock(RegistrationContext ctx, Properties settings, ItemSettings itemSettings, BlockSetType blockSetType)
    {
        super(settings, blockSetType);
        ctx.append(this, itemSettings.create(this, ctx));
    }

    @Override
    public @Nullable LootTable.Builder genLoot(BlockLootSubProvider generator)
    {
        return generator.createDoorTable(this);
    }
}
