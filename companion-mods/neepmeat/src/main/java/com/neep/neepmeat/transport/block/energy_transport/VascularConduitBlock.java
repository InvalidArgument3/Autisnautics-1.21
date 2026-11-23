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

package com.neep.neepmeat.transport.block.energy_transport;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.transport.api.pipe.*;
import com.neep.neepmeat.transport.block.energy_transport.entity.VascularConduitBlockEntity;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VascularConduitBlock extends PrecisePipeBlock implements EntityBlock, VascularConduit
{
    public VascularConduitBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    public static boolean matches(ItemStack stackInHand)
    {
        return stackInHand.getItem() instanceof BlockItem bi && (bi.getBlock() instanceof VascularConduit || bi.getBlock() instanceof VSCBlock);
    }

    @Override
    public boolean supportsWrenching()
    {
        return false;
    }

    @Override
    public boolean canConnectTo(BlockState hereState, BlockState toState, Direction toFace, Level world, BlockPos toPos, boolean newConnection)
    {
        if (toState.getBlock() instanceof VascularConduit)
        {
            return true;
        }
        else return BloodAcceptor.SIDED.find(world, toPos, toFace) != null;
    }

    @Override
    protected boolean adjacentAutoConnect(Level world, BlockPos adjPos, BlockState adjState, BlockState hereState, Direction direction)
    {
//        if (adjState.getBlock() instanceof VascularConduit pipe
//                && canConnectTo(hereState, adjState, direction.getOpposite(), world, adjPos, true))
//        {
//            return true;
//        }
//
//        return BloodAcceptor.SIDED.find(world, adjPos, direction) != null;

        return canConnectTo(hereState, adjState, direction.getOpposite(), world, adjPos, true);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        if (state.getValue(WATERLOGGED))
        {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        boolean canConnect = canConnectTo(state, neighborState, direction.getOpposite(), (Level) world, neighborPos, false);

//        boolean otherConnected = false;
//        if (neighborState.isOf(this))
//        {
//            otherConnected = neighborState.get(DIR_TO_CONNECTION.get(direction.getOpposite())) == ConnectionType.SIDE;
//        }

        ConnectionType finalConnection = canConnect ? ConnectionType.SIDE : ConnectionType.NONE;

        return state.setValue(DIR_TO_CONNECTION.get(direction), finalConnection);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        updatePosition(world, pos, state, VascularConduitEntity.UpdateReason.ADDED);

        // Squishy sound
        if (!world.isClientSide())
        {
            for (Direction value : Direction.values())
            {
                if (isConnectedIn(world, pos, state, value)
                        && BloodAcceptor.SIDED.find(world, pos.relative(value), value.getOpposite()) != null)
                {
                    squishyPlacementEffects(world, pos, false);
                    break;
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random)
    {
        super.animateTick(state, world, pos, random);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);

        BlockPos diff = sourcePos.subtract(pos);
        Direction dir = Direction.fromDelta(diff.getX(), diff.getY(), diff.getZ());

        // Not ideal, but it's better than a crash.
        if (dir == null)
            dir = Direction.NORTH;

        if (isConnectedIn(world, pos, state, dir)
                && BloodAcceptor.SIDED.find(world, sourcePos, dir.getOpposite()) != null
                && VascularConduit.find(world, sourcePos, world.getBlockState(sourcePos)) == null
                && !sourceBlock.equals(this))
        {
            // We are connected in this direction but the update does not originate from another conduit.
            // This means that an acceptor has just been destroyed or has emitted an update.
            updatePosition(world, pos, state, VascularConduitEntity.UpdateReason.CHANGED);
        }

        // Update if an acceptor has been placed or if something has been destroyed.
        // There is no way of telling whether the update was caused by a destroyed acceptor.
        var acceptor = BloodAcceptor.SIDED.find(world, sourcePos, dir);
        BlockState sourceState = world.getBlockState(sourcePos);
        boolean broken = world.getBlockState(sourcePos).isAir() && isConnectedIn(world, pos, state, dir);

        // Redstone likes to cause loads of updates where there is no easy way of checking whether they originate from redstone.
        // If sourceBlock is air, the update is probably from a placement.
        // If not, and if nothing was broken, and sourceBlock and sourceState are different, then it's probably redstone.
        boolean probablyRedstone = !broken
                && !sourceBlock.defaultBlockState().isAir()
                && !sourceState.is(sourceBlock)
                && !sourceBlock.defaultBlockState().canBeReplaced() // Grass replacement can look like redstone
                ;

        if (
                !probablyRedstone && (
                        acceptor != null // New acceptor or update
                        || broken // Acceptor broken
                        )
        )
        {
            // This is executed before getStateForNeighborUpdate, so the new connection must be made manually,
            BlockState newState = addConnection(world, pos, state, dir);
            world.setBlockAndUpdate(pos, newState);

            updatePosition(world, pos, newState, VascularConduitEntity.UpdateReason.CHANGED);

            // Squishy sounds for block connections!
            if (sourceBlock != this && sourceBlock != sourceState.getBlock())
                squishyPlacementEffects(world, pos, broken);
        }
    }

    public void squishyPlacementEffects(Level world, BlockPos pos, boolean broken)
    {
        if (broken)
        {
            world.playSound(null, pos, NMSounds.MEATGUN_MODULE_REMOVE.get(), SoundSource.BLOCKS,
                    0.5F, (float) world.getRandom().triangle(1, 0.2));
        }
        else
        {
            world.playSound(null, pos, NMSounds.MEATGUN_MODULE_PLACE.get(), SoundSource.BLOCKS,
                    0.5F, (float) world.getRandom().triangle(1, 0.3));
        }
    }

    private BlockState addConnection(Level world, BlockPos pos, BlockState state, Direction dir)
    {
        var property = DIR_TO_CONNECTION.get(dir);
        return state.setValue(property, ConnectionType.SIDE);
    }

    @Override
    public void onConnectionUpdate(Level world, BlockState state, BlockState newState, BlockPos pos, Player entity)
    {
        super.onConnectionUpdate(world, state, newState, pos, entity);
        updatePosition(world, pos, state, VascularConduitEntity.UpdateReason.CHANGED);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        super.onRemove(state, world, pos, newState, moved);
        if (world.isClientSide())
            return;

        if (!state.is(newState.getBlock()))
        {
            updatePosition(world, pos, state, VascularConduitEntity.UpdateReason.REMOVED);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
//        return super.onUse(state, world, pos, player, hand, hit);
        if (player.getItemInHand(hand).is(Items.STICK))
        {
            if (!world.isClientSide() && world.getBlockEntity(pos) instanceof VascularConduitBlockEntity be)
            {
                System.out.println(be.getNetwork());
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.VASCULAR_CONDUIT.create(pos, state);
    }

    @Override
    public VascularConduitEntity getEntity(Level world, BlockPos pos, BlockState state)
    {
        return VascularConduitEntity.find(world, pos);
    }
}
