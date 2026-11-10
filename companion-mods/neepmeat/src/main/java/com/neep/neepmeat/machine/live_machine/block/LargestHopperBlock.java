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

import com.mojang.serialization.MapCodec;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.neepmeat.machine.live_machine.block.entity.LargestHopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LargestHopperBlock extends BaseEntityBlock {
    public LargestHopperBlock() {
        super(MeatlibBlockSettings.create().strength(2).properties());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LargestHopperBlockEntity(pos, state);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && level.getGameTime() % 3 == 0 && entity.onGround()
                && entity instanceof ItemEntity item && level.getBlockEntity(pos) instanceof LargestHopperBlockEntity hopper) {
            hopper.insertEntity(item);
        }
    }

    public void collect(Level level, BlockPos pos, ItemEntity item) {
        if (level.getBlockEntity(pos) instanceof LargestHopperBlockEntity hopper) {
            hopper.insertEntity(item);
        }
    }
}
