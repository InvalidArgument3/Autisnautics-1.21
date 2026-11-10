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

package com.neep.neepmeat.machine.synthesiser;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.block.multi.TallBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.util.ItemUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import org.jetbrains.annotations.Nullable;

public class SynthesiserBlock extends TallBlock implements EntityBlock
{
    public static final BooleanProperty FULL = BooleanProperty.create("full");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public SynthesiserBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion().isRedstoneConductor((state, world, pos) -> true));
        this.registerDefaultState(getStateDefinition().any().setValue(FULL, false).setValue(POWERED, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (ItemUtil.playerHoldingPipe(player, hand))
            return InteractionResult.PASS;

        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof SynthesiserBlockEntity be)
        {
            if (player.isShiftKeyDown())
            {
                Component name = be.getEntityType() != null ? be.getEntityType().getDescription() : Component.nullToEmpty("Empty");
                player.displayClientMessage(Component.translatable("message.neepmeat.synthesiser.template", name), true);
                return InteractionResult.SUCCESS;
            }

            ItemStack stack = player.getItemInHand(hand);
            if (be.changeEntityType(player, stack))
            {
                world.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1, 1);
            }

        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FULL, POWERED);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof SynthesiserBlockEntity be && be.getEntityType() != null)
        {
            MeatlibStorageUtil.scatter(world, pos, be.getItemStorage(null));
//            ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, be.createItem());
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    protected Structure createStructure(RegistrationContext ctx)
    {
        return ctx.append(this, new Structure(MeatlibBlockSettings.copyOf(this.properties)), MeatlibBlock::structure);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.SYNTHESISER.create(pos, state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return defaultBlockState().setValue(POWERED, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);

        if (!world.isClientSide)
        {
            boolean bl = state.getValue(POWERED);
            if (bl != world.hasNeighborSignal(pos))
            {
                BlockState newState = state.cycle(POWERED);
                world.setBlock(pos, state.cycle(POWERED), Block.UPDATE_KNOWN_SHAPE);
                if (world.getBlockEntity(pos) instanceof SynthesiserBlockEntity be)
                {
                    be.setBlockState(newState);
                }
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.SYNTHESISER, (world1, pos, state1, blockEntity) -> blockEntity.tick(), null, world);
    }
}
