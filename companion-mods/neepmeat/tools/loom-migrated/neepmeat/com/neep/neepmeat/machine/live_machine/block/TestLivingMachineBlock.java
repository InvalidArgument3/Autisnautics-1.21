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

package com.neep.neepmeat.machine.live_machine.block;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.neepmeat.api.live_machine.LivingMachineBlock;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.LivingMachineControllerBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class TestLivingMachineBlock extends LivingMachineBlock implements MeatlibBlock
{
    public static final EnumProperty<Status> STATUS = EnumProperty.create("status", Status.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public TestLivingMachineBlock(Properties settings)
    {
        super(settings);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof LivingMachineControllerBlockEntity be)
        {
            if (!world.isClientSide())
            {
                if (player.isShiftKeyDown())
                {
                    player.sendSystemMessage(Component.nullToEmpty("Rated power: ").copy().append(PowerUtil.perUnitToText(be.getRatedPower())));
                    player.sendSystemMessage((PowerUtil.perUnitToLabelText(be.getPower())));
                    player.sendSystemMessage(Component.nullToEmpty("Efficiency: " + Math.round(be.getEfficiency() * 100) + "%"));
                    player.sendSystemMessage(Component.nullToEmpty("RUL: " + new DecimalFormat("###.##").format(be.getRulHours()) + "hr"));
                }
                else
                {
                    player.openMenu(be);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        BlockState superState = super.getStateForPlacement(ctx);

        return superState != null ? superState.setValue(FACING, ctx.getHorizontalDirection().getOpposite()) : null;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(this) && world.getBlockEntity(pos) instanceof LivingMachineBlockEntity be)
        {
            be.onBlockRemoved();
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(STATUS, FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return LivingMachines.LIVING_MACHINE_CONTROLLER_BE.create(pos, state);
    }

    public enum Status implements StringRepresentable
    {
        INVALID_STRUCTURE,
        BROKEN,
        ACTIVE;

        @Override
        public String getSerializedName()
        {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
