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

package com.neep.neepmeat.block.vat;

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.multiblock.MultiBlock;
import com.neep.neepmeat.block.entity.machine.VatControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VatControllerBlock extends BaseHorFacingBlock implements MultiBlock, EntityBlock, VatComponent
{
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

    public VatControllerBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return context.getNearestLookingDirection().getAxis().isVertical() ? defaultBlockState() :
                this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite())
                        .setValue(ASSEMBLED, false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        System.out.println(world.getBlockEntity(pos));
        if (world.getBlockEntity(pos) instanceof VatControllerBlockEntity be)
        {
            if (!be.isAssembled() && !world.isClientSide() && !player.isShiftKeyDown())
            {
                be.tryAssemble((ServerLevel) world);
            }
            else if (player.isShiftKeyDown())
            {
                System.out.println(be.blocks);
                System.out.println(be.getFluidStorage());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(this) && world.getBlockEntity(pos) instanceof VatControllerBlockEntity be && !world.isClientSide())
        {
            be.disassemble((ServerLevel) world, true);
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ASSEMBLED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new VatControllerBlockEntity(pos, state);
    }
}
