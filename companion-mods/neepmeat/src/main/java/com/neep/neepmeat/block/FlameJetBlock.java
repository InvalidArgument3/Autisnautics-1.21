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

package com.neep.neepmeat.block;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.util.RedstoneMode;
import com.neep.neepmeat.block.entity.FlameJetBlockEntity;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.util.ItemUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

public class FlameJetBlock extends BaseFacingBlock implements EntityBlock
{
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");
    public static final EnumProperty<RedstoneMode> MODE = EnumProperty.create("redstone_mode", RedstoneMode.class);

    public FlameJetBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        registerDefaultState(getStateDefinition().any().setValue(RUNNING, false));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (world.getBlockEntity(pos) instanceof FlameJetBlockEntity be && !ItemUtil.playerHoldingPipe(player, hand))
        {
            if (player.isShiftKeyDown())
            {
                var newState = state.cycle(MODE);
//                newState = newState.with(RUNNING, ));

                be.setBlockState(newState);
                be.update();

                world.setBlock(pos, newState, UPDATE_ALL);
                world.playSound(null, pos, MeatlibSounds.CLICK, SoundSource.BLOCKS, 1, 1);
                return InteractionResult.SUCCESS;
            }
            else
            {
                return InteractionResult.sidedSuccess(be.onUse(player, hand));
            }
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        if (world.getBlockEntity(pos) instanceof FlameJetBlockEntity be)
        {
//            var nextState = state;
//            nextState = nextState.with(RUNNING, state.get(MODE).active(world.isReceivingRedstonePower(pos)));
            be.update();
//            world.setBlockState(pos, nextState);
        }
        super.neighborChanged(state, world, pos, block, fromPos, notify);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        var state = super.getStateForPlacement(context);

        if (state == null)
            return null;

//        state = state.with(RUNNING, state.get(MODE).active(context.getWorld().isReceivingRedstonePower(context.getBlockPos())));
        return state;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.FLAME_JET.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.FLAME_JET, FlameJetBlockEntity::serverTick, FlameJetBlockEntity::clientTick, world);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(RUNNING, MODE);
    }
}
