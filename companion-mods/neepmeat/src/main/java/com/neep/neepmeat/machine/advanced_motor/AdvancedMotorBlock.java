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

package com.neep.neepmeat.machine.advanced_motor;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.transport.block.energy_transport.VascularConduitBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.util.Wrenchable;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class AdvancedMotorBlock extends BaseFacingBlock implements EntityBlock, Wrenchable
{
    public AdvancedMotorBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (VascularConduitBlock.matches(player.getItemInHand(hand)))
        {
            return InteractionResult.PASS;
        }

        if (world.getBlockEntity(pos) instanceof AdvancedMotorBlockEntity be && !world.isClientSide())
        {
            player.displayClientMessage(PowerUtil.perUnitToLabelText(be.getMechPUPower()), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return adjacentMotorisedDirection(context, this);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!state.is(newState.getBlock()))
        {
            world.getBlockEntity(pos, NMBlockEntities.ADVANCED_MOTOR).ifPresent(MotorEntity::onRemoved);
        }

        super.onRemove(state, world, pos, newState, moved);
    }

    public static BlockState adjacentMotorisedDirection(BlockPlaceContext context, Block block)
    {
        return MiscUtil.placeFacingPredicate(context, block, (w, p) -> w.getBlockEntity(p) instanceof MotorisedBlock);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.ADVANCED_MOTOR, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(), null, world);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.ADVANCED_MOTOR.create(pos, state);
    }
}
