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

package com.neep.neepmeat.machine.pedestal;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

public class PedestalBlock extends BaseBlock implements EntityBlock, DataCable
{
    public PedestalBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return Block.box(0, 0, 0, 16, 11, 16);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        if (world.getBlockEntity(pos) instanceof PedestalBlockEntity be && !world.isClientSide())
        {
            be.update(world.hasNeighborSignal(pos));
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, net.minecraft.util.RandomSource random)
    {
        super.tick(state, world, pos, random);
        if (world.getBlockEntity(pos) instanceof PedestalBlockEntity be)
        {
            be.recipeBehaviour.finishRecipe();
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof PedestalBlockEntity be && !world.isClientSide)
        {
            return InteractionResult.sidedSuccess(WritableStackStorage.handleInteract(player, hand, be.storage));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (world.getBlockEntity(pos) instanceof PedestalBlockEntity be)
        {
            MeatlibStorageUtil.scatterNoTransaction(world, pos, be.storage);
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PedestalBlockEntity be && !world.isClientSide())
        {
            onEntityCollided(world, pos, state, entity, be);
        }
    }

//    @Override
//    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
//    {
//        super.randomDisplayTick(state, world, pos, random);
//        if (world.getBlockEntity(pos) instanceof PedestalBlockEntity be && .hasRecipe)
//        {
////            be.recipeBehaviour.load(world);
////            if (be.recipeBehaviour.getRecipeId() != null)
//            spawnParticles(world, pos.down(2), 5, 0.4, 0.1);
//        }
//    }
        //  Amethyst???
//    public static void spawnParticles(World world, BlockPos pos, int count, double dy, double speed)
//    {
//        Random random = Random.create();
//        for (int i = 0; i <= count; ++i)
//        {
//            world.addParticle(new SwirlingParticleEffect(NMParticles.BLOCK_SWIRL,
//                    Blocks.AMETHYST_BLOCK.getDefaultState(), 0.4, speed), pos.getX() + 0.5, pos.getY() + 0.1 + random.nextFloat(), pos.getZ() + 0.5, 0, dy, 0.1);
//        }
//    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.PEDESTAL, (world1, pos, state1, blockEntity) -> blockEntity.tick(), null, world);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.PEDESTAL.create(pos, state);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
    {
        if (world.getBlockEntity(pos) instanceof PedestalBlockEntity be)
        {
            return be.storage.amount == 1 ? 15 : 0;
        }
        return 0;
    }

    public static void onEntityCollided(Level world, BlockPos pos, BlockState state, Entity entity, PedestalBlockEntity be)
    {
        if (!world.isClientSide && entity instanceof ItemEntity item)
        {
            be.extractFromItem(item);
        }
    }
}