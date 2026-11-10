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

package com.neep.neepmeat.api.big_block;

import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class BigBlock<T extends BigBlockStructure<?>> extends Block implements BlockPlacementPreview
{
//    public static final BlockVolume VOLUME = BlockVolume.oddCylinder(1, 0, 2);

    private final T structureBlock;

    public BigBlock(RegistrationContext ctx, Properties settings)
    {
        super(settings.pushReaction(PushReaction.IGNORE));
        this.structureBlock = registerStructureBlock(ctx);
    }

    protected abstract T registerStructureBlock(RegistrationContext ctx);

    public abstract BigBlockPattern getVolume(BlockState blockState);

//    protected VoxelShape getShape(BlockState state)
//    {
//        return getVolume(state).toVoxelShape();
//    }
//    protected BigBlockStructure registerStructureBlock()
//    {
//        return BlockRegistry.queue(new BigBlockStructure(this, FabricBlockSettings.of(Material.METAL)), "obj_test_structure");

//    }

//    protected BlockVolume getVolume()
//    {
//        return VOLUME;
//    }

    @Override
    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state)
    {

        super.spawnDestroyParticles(world, player, pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return getVolume(state).toVoxelShape();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        for (var a : getVolume(state).iterable())
        {
            if (!world.isEmptyBlock(pos.offset(a)))
            {
                return false;
            }
        }

        for (var box : getVolume(state).toVoxelShape().toAabbs())
        {
            if (!world.noCollision(box.move(pos)))
                return false;
        }

        return super.canSurvive(state, world, pos);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);

        getVolume(state).placeBlocks(world, pos, pos);
//        BlockPos.Mutable mutable = pos.mutableCopy();
//        for (Vec3i vec : getVolume(state).iterable())
//        {
//            mutable.set(pos, vec);
//
//            // Do not replace the origin block
//            if (mutable.equals(pos)) continue;
//
//            world.setBlockState(mutable, structureBlock.getDefaultState(), NOTIFY_ALL);
//            if (world.getBlockEntity(mutable) instanceof BigBlockStructureEntity be)
//            {
//                be.setController(pos);
//            }
//        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        super.onRemove(state, world, pos, newState, moved);

        if (!newState.is(this))
        {
            BlockPos.MutableBlockPos mutable = pos.mutable();
            for (Vec3i vec : getVolume(state).iterable())
            {
                mutable.setWithOffset(pos, vec);
                world.setBlock(mutable, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
            }
        }
    }

    public T getStructure()
    {
        return structureBlock;
    }
}
