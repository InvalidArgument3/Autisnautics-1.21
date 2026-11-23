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
 * Upstream volume is a flat disc of radius 1. The model boxes are the disc, not a separate mesh.
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
import com.neep.neepmeat.machine.live_machine.block.entity.CrusherSegmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CrusherSegmentBlock extends BigBlock<BigBlockStructure.Simple<BigBlockStructureEntity>> implements MeatlibBlock, EntityBlock {
    private final BigBlockPattern pattern;

    public CrusherSegmentBlock(RegistrationContext ctx, Properties settings) {
        super(ctx, settings.noOcclusion());
        this.pattern = BigBlockPattern.makeOddCylinder(1, 0, 0, getStructure().defaultBlockState());
    }

    @Override
    protected BigBlockStructure.Simple<BigBlockStructureEntity> registerStructureBlock(RegistrationContext ctx) {
        BlockEntityType[] box = new BlockEntityType[1];
        BigBlockStructure.BlockEntityRegisterererer<BigBlockStructureEntity> register = structure -> {
            box[0] = BlockEntityType.Builder.of((pos, state) -> new BigBlockStructureEntity(box[0], pos, state), structure).build(null);
            return StructureBlockEntities.register("crusher_segment_structure", box[0]);
        };
        return ctx.append(this, new BigBlockStructure.Simple<>(this, MeatlibBlockSettings.copyOf(properties), register,
                (pos, state) -> new BigBlockStructureEntity(box[0], pos, state)), "crusher_segment_structure");
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
        return new CrusherSegmentBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.is(this) && world.getBlockEntity(pos) instanceof CrusherSegmentBlockEntity crusher && !crusher.stack().isEmpty()) {
            net.minecraft.world.Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), crusher.stack().copy());
            crusher.insert(ItemStack.EMPTY);
        }
        super.onRemove(state, world, pos, newState, moved);
    }
}
