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

package com.neep.neepmeat.machine.stirling_engine;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.advanced_motor.AdvancedMotorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.util.Wrenchable;
import org.jetbrains.annotations.Nullable;

public class StirlingEngineBlock extends BaseFacingBlock implements EntityBlock, Wrenchable
{
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public StirlingEngineBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any().setValue(LIT, false));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (!world.isClientSide)
        {
            MenuProvider screenHandlerFactory = state.getMenuProvider(world, pos);
            if (screenHandlerFactory != null)
            {
                player.openMenu(screenHandlerFactory);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return AdvancedMotorBlock.adjacentMotorisedDirection(context, this);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!state.is(newState.getBlock()) && !world.isClientSide)
        {
            if (world.getBlockEntity(pos) instanceof StirlingEngineBlockEntity be)
            {
                be.getStorage().dropItems(world, pos);
                be.onRemoved();
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos)
    {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider factory ? factory : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.STIRLING_ENGINE, (world1, pos, state1, blockEntity) -> ((StirlingEngineBlockEntity) blockEntity).serverTick(), null, world);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new StirlingEngineBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }
}
