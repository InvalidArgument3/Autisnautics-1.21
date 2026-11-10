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

package com.neep.neepmeat.transport.block.fluid_transport;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.item.FluidComponentItem;
import com.neep.neepmeat.transport.api.pipe.AbstractAxialFluidPipe;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import com.neep.neepmeat.transport.fluid_network.node.BlockFluidPipeVertex;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CheckValveBlock extends AbstractAxialFluidPipe implements EntityBlock
{
    public CheckValveBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings.factory(FluidComponentItem::new), settings.noOcclusion());
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public AcceptorModes getDirectionMode(Level world, BlockPos pos, BlockState state, Direction direction)
    {
        Direction facing = state.getValue(FACING);
        if (direction == facing)
        {
            return AcceptorModes.PUSH;
        }
        else if (direction == facing.getOpposite())
        {
            return AcceptorModes.INSERT_ONLY;
        };
        return AcceptorModes.NONE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.CHECK_VALVE.create(pos, state);
    }

    public static class CheckValveFluidPipeVertex extends BlockFluidPipeVertex
    {
        public CheckValveFluidPipeVertex(FluidPipeBlockEntity fluidPipeBlockEntity)
        {
            super(fluidPipeBlockEntity);
        }

        @Override
        public long canInsert(ServerLevel world, int inDir, FluidVariant variant, long maxAmount)
        {
            // TODO: make this actually work
            long superAmount = super.canInsert(world, inDir, variant, maxAmount);
            return  inDir == parent.getBlockState().getValue(FACING).ordinal() ? superAmount : 0;
        }

        @Override
        public boolean canSimplify()
        {
            return false;
        }
    }
}