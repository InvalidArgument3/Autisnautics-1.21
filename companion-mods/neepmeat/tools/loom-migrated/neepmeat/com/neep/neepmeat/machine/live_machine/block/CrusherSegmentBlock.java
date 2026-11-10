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
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.CrusherSegmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

public class CrusherSegmentBlock extends BigBlock<CrusherSegmentBlock.CrusherSegmentStructureBlock> implements MeatlibBlock, EntityBlock
{
    private final BigBlockPattern pattern = new BigBlockPattern().oddCylinder(1, 0, 0, () -> getStructure().defaultBlockState());

    public CrusherSegmentBlock(RegistrationContext ctx, Properties settings, ItemSettings itemSettings)
    {
        super(ctx, settings);
        itemSettings.create(this, ctx);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return LivingMachines.CRUSHER_SEGMENT_BE.create(pos, state);
    }

    @Override
    protected CrusherSegmentStructureBlock registerStructureBlock(RegistrationContext ctx)
    {
        return ctx.append(this, new CrusherSegmentStructureBlock(this, MeatlibBlockSettings.copyOf(properties)), "crusher_segment_structure");
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (world.getBlockEntity(pos) instanceof CrusherSegmentBlockEntity be)
        {
            MeatlibStorageUtil.scatter(world, pos, be.getStorage());
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState)
    {
        return pattern;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, LivingMachines.CRUSHER_SEGMENT_BE, null, (world1, pos, state1, blockEntity) -> blockEntity.clientTick(), world);
    }

    public static class CrusherSegmentStructureBlock extends BigBlockStructure<CrusherSegmentStructureBlockEntity>
    {
        public CrusherSegmentStructureBlock(BigBlock<?> parent, Properties settings)
        {
            super(parent, settings);
        }

        @Override
        protected BlockEntityType<CrusherSegmentStructureBlockEntity> registerBlockEntity()
        {
            return NMBlockEntities.register("crusher_segment_structure", (p, s) -> new CrusherSegmentStructureBlockEntity(getBlockEntityType(), p, s), this);
        }
    }

    static class CrusherSegmentStructureBlockEntity extends BigBlockStructureEntity
    {
        public CrusherSegmentStructureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }
    }
}
