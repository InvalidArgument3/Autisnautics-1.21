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

package com.neep.neepmeat.transport.block.item_transport;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.block.item_transport.entity.RouterBlockEntity;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AdvancedRouterBlock extends RouterBlock
{
    public AdvancedRouterBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    public boolean canItemEnter(ItemVariant variant, long amount, Level world, BlockPos pos, BlockState state, Direction inFace, boolean routed)
    {
        if (world.getBlockEntity(pos) instanceof RouterBlockEntity be)
        {
            Direction output = be.getOutputDirection(variant);
            return inFace != output && output != null;
        }
        return false;
    }

    @Override
    public long insert(Level world, BlockPos pos, BlockState state, Direction direction, ItemInPipe item, TransactionContext transaction)
    {
        if (world.getBlockEntity(pos) instanceof RouterBlockEntity be)
        {
            Direction output = be.getOutputDirection(item.resource());
            if (direction != output && output != null)
            {
                return ItemPipeUtil.EjectOperation.of(world, pos, output).useRouting(false).pipeToAny(item, transaction).amount();
            }
        }
        return 0;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.ADVANCED_ROUTER.create(pos, state);
    }
}
