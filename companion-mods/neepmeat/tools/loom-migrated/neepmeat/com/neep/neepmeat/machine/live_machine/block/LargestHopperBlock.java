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
import com.neep.neepmeat.machine.live_machine.block.entity.LargestHopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LargestHopperBlock extends BigBlock<LargestHopperBlock.StructureBlock> implements MeatlibBlock, EntityBlock
{
    private final BigBlockPattern pattern = new BigBlockPattern().oddCylinder(1, 0, 0, () -> getStructure().defaultBlockState());
    private final VoxelShape shape = Shapes.joinUnoptimized(Shapes.box(-1, 0, -1, 2, 1, 2),
            Block.box(-16 + 6, 2, -16 + 6, 32 - 6, 16, 32 - 6), BooleanOp.ONLY_FIRST);

    public static boolean isLargeHopper(BlockState state)
    {
        return state.is(LivingMachines.LARGEST_HOPPER) || state.is(LivingMachines.LARGEST_HOPPER.getStructure());
    }

    public LargestHopperBlock(RegistrationContext ctx, Properties settings, ItemSettings itemSettings)
    {
        super(ctx, settings);
        itemSettings.create(this, ctx);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return LivingMachines.LARGEST_HOPPER_BE.create(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return shape;
    }

    @Override
    protected StructureBlock registerStructureBlock(RegistrationContext ctx)
    {
        return ctx.append(this, new StructureBlock(this, MeatlibBlockSettings.copyOf(properties)), "largest_hopper_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState)
    {
        return pattern;
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
        super.entityInside(state, world, pos, entity);
        if (!world.isClientSide()
                && world.getGameTime() % 3 == 0 // This sometimes fails for items dropped by blocks. Strange.
                && entity.onGround()
                && entity instanceof ItemEntity itemEntity
                && world.getBlockEntity(pos) instanceof LargestHopperBlockEntity be
        )
        {
            be.insertEntity(itemEntity);
        }
    }

    @Override
    public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, float fallDistance)
    {
        super.fallOn(world, state, pos, entity, fallDistance);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(this) && world.getBlockEntity(pos) instanceof LargestHopperBlockEntity be)
        {
            MeatlibStorageUtil.scatterNoTransaction(world, pos, be.getStorage(null));
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    public static class StructureBlock extends BigBlockStructure<StructureBlockEntity>
    {
        public StructureBlock(BigBlock<?> parent, Properties settings)
        {
            super(parent, settings);
        }

        @Override
        protected BlockEntityType<StructureBlockEntity> registerBlockEntity()
        {
            return NMBlockEntities.register("largest_hopper_structure", (p, s) -> new StructureBlockEntity(getBlockEntityType(), p, s), this);
        }

        @Override
        public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
        {
            super.entityInside(state, world, pos, entity);
            if (!world.isClientSide()
                && world.getGameTime() % 3 == 0 // This sometimes fails for items dropped by blocks. Strange.
                    && entity.onGround()
                    && entity instanceof ItemEntity itemEntity
                    && world.getBlockEntity(pos) instanceof StructureBlockEntity be
            )
            {
                if (world.getBlockEntity(be.getControllerPos()) instanceof LargestHopperBlockEntity cbe)
                {
                    cbe.insertEntity(itemEntity);
                }
            }
        }

    }

    static class StructureBlockEntity extends BigBlockStructureEntity
    {
        public StructureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }
    }
}
