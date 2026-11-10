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

package com.neep.neepmeat.machine.heater;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.Heatable;
import com.neep.neepmeat.transport.block.energy_transport.VascularConduitBlock;
import com.neep.neepmeat.util.ItemUtil;
import com.neep.meatlib.util.MiscUtil;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class HeaterBlock extends BaseFacingBlock implements EntityBlock
{
    public HeaterBlock(RegistrationContext ctx, ItemSettings itemSettings, FabricBlockSettings settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return MiscUtil.placeFacingPredicate(context, this, (w, p) -> Heatable.LOOKUP.find(w, p, null) != null);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.HEATER.create(pos, state);
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A>
    checkType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<E> ticker, Level world)
    {
        return expectedType == givenType && !world.isClientSide ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return checkType(type, NMBlockEntities.HEATER, HeaterBlockEntity::serverTick, world);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(state.getBlock()) && world.getBlockEntity(pos) instanceof HeaterBlockEntity be)
        {
            be.onRemove();
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (ItemUtil.playerHoldingPipe(player, hand) || VascularConduitBlock.matches(player.getItemInHand(hand)))
            return InteractionResult.PASS;

        if (!world.isClientSide)
        {
            if (world.getBlockEntity(pos) instanceof HeaterBlockEntity be)
            {
                player.displayClientMessage(PowerUtil.perUnitToLabelText(be.getPUPower()).append(", Furnace speed: " + Heatable.getFurnaceTickIncrement((float) be.getPUPower()) + "x"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
