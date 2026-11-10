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

package com.neep.neepbus.block;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.block.entity.MouthBlockEntity;
import com.neep.neepbus.screen.MouthScreenHandler;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

public class MouthBlock extends BaseBlock implements DataCable, NeepBusProvider, EntityBlock
{
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public MouthBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.isRedstoneConductor((state, world, pos) -> true));
        registerDefaultState(getStateDefinition().any().setValue(POWERED, false));
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {

    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof MouthBlockEntity be)
        {
            player.openMenu((SimpleScreenHandlerFactory) (syncId, playerInventory, player1) ->
                    new MouthScreenHandler(playerInventory, syncId, be));

            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder)
    {
        List<ItemStack> stacks = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof MouthBlockEntity be && !be.isEmpty())
        {
            stacks.forEach(be::saveToItem);

            stacks.forEach(this::processDroppedStack);
            return stacks;
        }
        return stacks;
    }

    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player)
    {
        if (world.getBlockEntity(pos) instanceof MouthBlockEntity be)
        {
            if (!world.isClientSide() && player.isCreative() && !be.isEmpty())
                MiscUtil.dropBlockWithNbt(world, this, pos, be, this::processDroppedStack);
        }
        super.playerWillDestroy(world, pos, state, player);
    }

    private void processDroppedStack(ItemStack stack)
    {
        stack.setHoverName(stack.getHoverName().copy().append(" (" + MouthBlockEntity.getMessage(stack) + ")"));
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);

        boolean receivingPower = world.hasNeighborSignal(pos);
        boolean powered = state.getValue(POWERED);
        if (powered != receivingPower)
        {
            if (receivingPower && world.getBlockEntity(pos) instanceof MouthBlockEntity be)
            {
                be.trigger();
            }

            world.setBlock(pos, state.setValue(POWERED, receivingPower), UPDATE_KNOWN_SHAPE);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NeepBus.MOUTH_BE.create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }
}
