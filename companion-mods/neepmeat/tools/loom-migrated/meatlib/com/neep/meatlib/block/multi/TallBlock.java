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

package com.neep.meatlib.block.multi;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.block.BaseDummyBlock;
import com.neep.meatlib.block.MeatlibBlockExtension;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public abstract class TallBlock extends BaseBlock
{
    public static final VoxelShape OUTLINE = Block.box(0, 0, 0, 16, 32, 16);

    private final Block structureBlock;

    public TallBlock(RegistrationContext ctx, Properties settings)
    {
        this(ctx, ItemSettings.block(), settings);
    }

    public TallBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.pushReaction(PushReaction.IGNORE));
        this.structureBlock = createStructure(ctx);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return OUTLINE;
    }

    public final Block getStructureBlock()
    {
        return structureBlock;
    }

    protected abstract Structure createStructure(RegistrationContext ctx);

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY() + 1, pos.getZ());
        return world.getBlockState(pos.above()).isAir() && world.noCollision(box) && super.canSurvive(state, world, pos);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        if (world.getBlockState(pos.above()).isAir())
        {
            world.setBlockAndUpdate(pos.above(), structureBlock.defaultBlockState());
        }
        super.setPlacedBy(world, pos, state, placer, itemStack);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(this) && world.getBlockState(pos.above()).is(structureBlock))
        {
            world.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
    {
        return 1;
    }
    
    public class Structure extends BaseDummyBlock implements MeatlibBlockExtension
    {
        public Structure(Properties settings)
        {
            super(settings.pushReaction(PushReaction.IGNORE));
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
        {
            return TallBlock.this.getShape(state, world, pos, context).move(0, -1, 0);
        }

        @Override
        public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
        {
            if (!newState.is(this) && world.getBlockState(pos.below()).is(TallBlock.this))
            {
                world.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
            }
            super.onRemove(state, world, pos, newState, moved);
        }

        @Override
        public String getDescriptionId()
        {
            return TallBlock.this.getDescriptionId();
        }

        @Override
        public ItemLike dropsLike()
        {
            return TallBlock.this;
        }

        @Override
        public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
        {
            return 1;
        }

        @Override
        public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state)
        {
            return TallBlock.this.getCloneItemStack(world, pos, state);
        }

        @Override
        protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state)
        {
            world.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(TallBlock.this.defaultBlockState()));
        }

//        @Override
//        public void neepmeat$appendTags(TagConsumer<Block> consumer)
//        {
//            MeatlibBlockExtension.super.neepmeat$appendTags(consumer);
//        }
    }
}
