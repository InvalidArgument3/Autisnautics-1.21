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

package com.neep.neepmeat.machine.alloy_kiln;

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.transfer.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class AlloyKilnBlock extends BaseHorFacingBlock implements EntityBlock
{
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final VoxelShape COLLISION_SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 18, 16),
            Block.box(1, 18, 1, 15, 20, 15),
            Block.box(3, 20, 3, 13, 22, 13)
            );

    public AlloyKilnBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        this.registerDefaultState(getStateDefinition().any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Direction facing = context.getHorizontalDirection();
        return facing.getAxis().isVertical() ? defaultBlockState() :
                this.defaultBlockState().setValue(FACING, facing.getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (!world.isClientSide)
        {
            MenuProvider screenHandlerFactory = state.getMenuProvider(world, pos);
            if (screenHandlerFactory != null)
            {
                player.openMenu(screenHandlerFactory);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
    {
        if (world.getBlockEntity(pos) instanceof AlloyKilnBlockEntity be)
        {
            return StorageUtil.calculateComparatorOutput(be.getStorage().getInputStorage());
        }
        return 0;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return COLLISION_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return COLLISION_SHAPE;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!state.is(newState.getBlock()) && !world.isClientSide)
        {
            if (world.getBlockEntity(pos) instanceof AlloyKilnBlockEntity be)
            {
                be.getStorage().dropItems(world, pos);
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT))
        {
            return;
        }

        double d = (double) pos.getX() + 0.5;
        double e = pos.getY();
        double f = (double) pos.getZ() + 0.5;

        if (random.nextDouble() < 0.1)
        {
            world.playLocalSound(d, e, f, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
        }
        Direction direction = state.getValue(FACING);
        Direction.Axis axis = direction.getAxis();
        double h = random.nextDouble() * 0.6 - 0.3;
        double i = axis == Direction.Axis.X ? (double) direction.getStepX() * 0.52 : h;
        double j = random.nextDouble() * 6.0 / 16.0;
        double k = axis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52 : h;
        world.addParticle(ParticleTypes.SMOKE, d, e + 1.4, f, 0.0, 0.0, 0.0);
        world.addParticle(ParticleTypes.FLAME, d + i, e + j + 0.1, f + k, 0.0, 0.0, 0.0);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos)
    {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider factory ? factory : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.ALLOY_KILN, AlloyKilnBlockEntity::serverTick, null, world);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.ALLOY_KILN.create(pos, state);
    }
}
