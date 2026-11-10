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

package com.neep.neepmeat.machine.content_detector;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.screen_handler.ContentDetectorScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class InventoryDetectorBlock extends BaseFacingBlock implements EntityBlock
{
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public InventoryDetectorBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion().isRedstoneConductor(InventoryDetectorBlock::never));
        this.registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof InventoryDetectorBlockEntity be)
        {
            player.openMenu( (SimpleScreenHandlerFactory)
                    ((syncId, playerInventory, p) -> new ContentDetectorScreenHandler(syncId, playerInventory, be.filterInventory, be)));
            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!world.isClientSide && state.getValue(POWERED) && world.getBlockTicks().hasScheduledTick(pos, this))
        {
            this.updateNeighbors(world, pos, state.setValue(POWERED, false));
        }

        // Dump contents if destroyed
        if (!newState.is(state.getBlock()))
        {
            world.getBlockEntity(pos, NMBlockEntities.INVENTORY_DETECTOR).ifPresent(be ->
                    MeatlibStorageUtil.scatterNoTransaction(world, pos, be.getStorage(null)));
        }

        super.onRemove(state, world, pos, newState, moved);
    }

    protected void updateNeighbors(Level world, BlockPos pos, BlockState state)
    {
        Direction direction = state.getValue(FACING);
        BlockPos newPos = pos.relative(direction.getOpposite());
        world.neighborChanged(newPos, this, pos);
        world.updateNeighborsAtExceptFromFacing(newPos, this, direction);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new InventoryDetectorBlockEntity(pos, state);
    }

    @Override
    public boolean isSignalSource(BlockState state)
    {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        return state.getSignal(world, pos, direction);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        if (state.getValue(POWERED) && state.getValue(FACING) == direction)
        {
            return 15;
        }
        return 0;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.INVENTORY_DETECTOR, InventoryDetectorBlockEntity::serverTick, null, world);
    }

    public static boolean never(BlockState state, BlockGetter world, BlockPos pos)
    {
        return false;
    }
}
