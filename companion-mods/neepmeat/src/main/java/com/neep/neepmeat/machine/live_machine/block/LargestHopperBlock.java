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
 *
 * Upstream volume is a flat disc of radius 1. Item entities on the origin or the structure are swallowed.
 */

package com.neep.neepmeat.machine.live_machine.block;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.api.big_block.StructureBlockEntities;
import com.neep.neepmeat.machine.live_machine.block.entity.LargestHopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LargestHopperBlock extends BigBlock<BigBlockStructure.Simple<BigBlockStructureEntity>> implements MeatlibBlock, EntityBlock {
    private final BigBlockPattern pattern;

    public LargestHopperBlock(RegistrationContext ctx, Properties settings) {
        super(ctx, settings.noOcclusion());
        this.pattern = BigBlockPattern.makeOddCylinder(1, 0, 0, getStructure().defaultBlockState());
    }

    @Override
    protected BigBlockStructure.Simple<BigBlockStructureEntity> registerStructureBlock(RegistrationContext ctx) {
        BlockEntityType[] box = new BlockEntityType[1];
        BigBlockStructure.BlockEntityRegisterererer<BigBlockStructureEntity> register = structure -> {
            box[0] = BlockEntityType.Builder.of((pos, state) -> new BigBlockStructureEntity(box[0], pos, state), structure).build(null);
            return StructureBlockEntities.register("largest_hopper_structure", box[0]);
        };
        return ctx.append(this, new BigBlockStructure.Simple<>(this, MeatlibBlockSettings.copyOf(properties), register,
                (pos, state) -> new BigBlockStructureEntity(box[0], pos, state)), "largest_hopper_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState state) {
        return pattern;
    }

    @Override
    public net.minecraft.world.item.BlockItem blockItem() {
        return (net.minecraft.world.item.BlockItem) net.minecraft.world.item.Item.BY_BLOCK.get(this);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LargestHopperBlockEntity(pos, state);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && level.getGameTime() % 3 == 0 && entity.onGround() && entity instanceof ItemEntity item) {
            BlockEntity found = level.getBlockEntity(pos);
            if (found instanceof LargestHopperBlockEntity hopper) {
                hopper.insertEntity(item);
            } else if (found instanceof BigBlockStructureEntity structure && level.getBlockEntity(structure.getControllerPos()) instanceof LargestHopperBlockEntity hopper) {
                hopper.insertEntity(item);
            }
        }
    }

    public void collect(Level level, BlockPos pos, ItemEntity item) {
        if (level.getBlockEntity(pos) instanceof LargestHopperBlockEntity hopper) {
            hopper.insertEntity(item);
        }
    }
}
