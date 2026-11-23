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

package com.neep.neepmeat.api.big_block_2;

import com.neep.meatlib.block.BaseDummyBlock;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BigBlock2Structure extends BaseDummyBlock
{
    // For blockstate datagen
    // I hate datagen
    public static final List<BigBlock2Structure> INSTANCES = new ObjectArrayList<>();

    // I hate myself
    private static final ThreadLocal<BigBlock2> ABOMINATION = new ThreadLocal<>();

    protected final BigBlock2 parent;

    public BigBlock2Structure(BigBlock2 parent, Properties settings)
    {
        super(Util.make(() ->
        {
            // The agony!
            ABOMINATION.set(parent);

            // I am a sinner! I've sinned again!
            return settings.noOcclusion().forceSolidOn();
        }));
        ABOMINATION.remove();
        this.parent = parent;

        if (genEmptyModel())
            INSTANCES.add(this);
    }

    protected boolean genEmptyModel()
    {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.INVISIBLE;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        Vec3i offset = parent.getOffset(state);
        BlockState originState = world.getBlockState(pos.subtract(offset));

        // ShapeCache calls this with an empty world
        return parent.getShape(originState.is(parent) ? originState : parent.defaultBlockState(), world, pos, context).move(-offset.getX(), -offset.getY(), -offset.getZ());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        super.onRemove(state, world, pos, newState, moved);
        if (!newState.is(this))
        {
            BlockPos controllerPos = getOrigin(pos, state);
            if (world.getBlockState(controllerPos).is(parent))
            {
                world.destroyBlock(controllerPos, false);
            }
        }
    }

    @Override
    public String getDescriptionId()
    {
        return parent.getDescriptionId();
    }

    @Override
    public ItemLike dropsLike()
    {
        return parent;
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
    {
        return 1;
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader world, BlockPos pos, BlockState state)
    {
        return parent.getCloneItemStack(world, pos, state);
    }

    @Override
    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state)
    {
//        world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(parent.getDefaultState()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        if (parent == null)
            // AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
            builder.add(ABOMINATION.get().index);
        else
            builder.add(parent.index);
    }

    public BlockPos getOrigin(BlockPos pos, BlockState state)
    {
        return pos.subtract(parent.getOffset(state));
    }

    @Nullable
    public <T, C> T getApi(Level world, BlockPos pos, BlockState state, BlockApiLookup<T, C> api)
    {
        var offset = parent.getOffset(state);
        BlockPos originPos = pos.subtract(offset);
        var originState = world.getBlockState(originPos);

        if (originState.is(parent))
            return parent.getApi(world, originPos, originState, offset, api);

        return null;
    }

    public static <T, C> BlockApiLookup.BlockApiProvider<T, C> getApi(BlockApiLookup<T, C> api)
    {
        return (world, blockPos, blockState, blockEntity, c) ->
        {
            if (blockState.getBlock() instanceof BigBlock2Structure structure)
            {
                return structure.getApi(world, blockPos, blockState, api);
            }

            return null;
        };
    }
}
