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

package com.neep.neepmeat.machine.live_machine.block;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.ItemOutputPortBlockEntity;
import com.neep.neepmeat.transport.api.pipe.ItemPipeConnectable;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ItemOutputPortBlock extends PortBlock<ItemOutputPortBlockEntity> implements ItemPipeConnectable
{
    public static final BooleanProperty AUTO_EJECT = BooleanProperty.create("auto_eject");

    public ItemOutputPortBlock(RegistrationContext ctx, ItemSettings itemSettings, Supplier<BlockEntityType<ItemOutputPortBlockEntity>> factory, Properties settings)
    {
        super(ctx, itemSettings.factory(ItemPortBlockItem.forList(LivingMachines.ITEM_PORTS)), factory, settings);
        registerDefaultState(defaultBlockState().setValue(AUTO_EJECT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(AUTO_EJECT);
    }
}
