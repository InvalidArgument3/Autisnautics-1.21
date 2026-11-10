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

import com.neep.neepmeat.machine.live_machine.block.entity.LivingMachineControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.text.DecimalFormat;

public class TestLivingMachineBlock extends Block {
    public static final EnumProperty<Status> STATUS = EnumProperty.create("status", Status.class);

    public TestLivingMachineBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(STATUS, Status.INVALID_STRUCTURE));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LivingMachineControllerBlockEntity controller) {
            player.sendSystemMessage(Component.literal("Power: " + controller.getPower()));
            player.sendSystemMessage(Component.literal("Efficiency: " + Math.round(controller.getHealth() * 100) + "%"));
            player.sendSystemMessage(Component.literal("RUL: " + new DecimalFormat("###.##").format(controller.getHealth() * 100) + "hr"));
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATUS);
    }

    public enum Status implements StringRepresentable {
        INVALID_STRUCTURE, ACTIVE, BROKEN;

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
