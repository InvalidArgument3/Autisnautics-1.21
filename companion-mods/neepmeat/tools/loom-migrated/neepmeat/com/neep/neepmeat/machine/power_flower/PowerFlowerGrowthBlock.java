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

package com.neep.neepmeat.machine.power_flower;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.datagen.tag.NMTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PowerFlowerGrowthBlock extends BaseBlock implements PowerFlower
{
    public static final IntegerProperty GROWTH = BlockStateProperties.AGE_1;

    private final VoxelShape fullShape = Block.box(0, 0, 0, 16, 16, 16);
    private final VoxelShape topShape = Block.box(0, 0, 0, 16, 14, 16);

    public PowerFlowerGrowthBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
        this.registerDefaultState(defaultBlockState().setValue(GROWTH, 0));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    @Override
    public boolean autoGenDrop()
    {
        return false;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context)
    {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return state.getValue(GROWTH) == 0 ? fullShape : topShape;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
//        return validBlock(world.getBlockState(pos.down()).getBlock()) && super.canPlaceAt(state, world, pos);
        return super.canSurvive(state, world, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
//        if (state.get(GROWTH) == 0 && sourcePos.equals(pos.down()) && !validBlock(world.getBlockState(sourcePos).getBlock()))
//        {
//            world.breakBlock(pos, true);
//        }
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        ItemStack stack = player.getItemInHand(hand);
        int tries = triesForItem(stack);

        if (tries > 0)
        {
            if (!world.isClientSide())
            {
                for (int i = 0; i < tries; ++i)
                {
                    grow(world, pos, state);
                }
            }
            if (!player.isCreative())
            {
                stack.shrink(1);
                player.setItemInHand(hand, stack);
            }

            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    public static int triesForItem(ItemStack stack)
    {
        if (stack.is(NMTags.CHARNEL_COMPACTOR))
            return 2;

        if (stack.is(Items.BONE_MEAL))
            return 1;

        return 0;
    }

    public void grow(Level world, BlockPos pos, BlockState state)
    {
        RandomSource random = world.getRandom();
        Direction direction = Direction.values()[random.nextInt(5) + 1];
        BlockPos growPos = pos.relative(direction);

        if (canGrowTo(world, growPos))
            world.setBlockAndUpdate(growPos, getState(world, growPos));
    }

    private boolean canGrowTo(Level world, BlockPos pos)
    {
        BlockState downState = world.getBlockState(pos.below());
        BlockState nextState = world.getBlockState(pos);
        return nextState.isAir() && (downState.is(this) || !downState.isAir());
    }

    public BlockState getState(LevelAccessor world, BlockPos pos)
    {
        boolean upAir = world.isEmptyBlock(pos.above());
        boolean downThis = world.getBlockState(pos.below()).getBlock() instanceof PowerFlower;

        return defaultBlockState().setValue(GROWTH, upAir && downThis ? 1 : 0);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        return getState(world, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return getState(ctx.getLevel(), ctx.getClickedPos());
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
//        return state.get(GROWTH) == 0;
        return false;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
//        if (state.get(GROWTH) == 0 && validBlock(world.getBlockState(pos.down()).getBlock()))
//        {
//            world.setBlockState(pos.down(), state.with(GROWTH, 1));
//            world.setBlockState(pos, Blocks.AIR.getDefaultState());
//        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(GROWTH);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random)
    {
        super.animateTick(state, world, pos, random);
        if (random.nextInt(10) == 0)
        {
            world.addParticle(ParticleTypes.MYCELIUM,
                    pos.getX() + random.nextFloat() * 1.4 - 0.2,
                    pos.getY() + random.nextFloat() * 1.4 - 0.2,
                    pos.getZ() + random.nextFloat() * 1.4 - 0.2,
                    random.nextFloat() - 0.5, random.nextFloat() - 0.5, random.nextFloat() - 0.5);
        }
    }
}
