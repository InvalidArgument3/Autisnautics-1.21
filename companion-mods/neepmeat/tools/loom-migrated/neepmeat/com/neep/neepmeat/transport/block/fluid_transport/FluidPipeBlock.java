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

package com.neep.neepmeat.transport.block.fluid_transport;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.FluidPipeBlockItem;
import com.neep.neepmeat.transport.FluidTransport;
import com.neep.neepmeat.transport.api.pipe.AbstractAxialFluidPipe;
import com.neep.neepmeat.transport.api.pipe.FluidPipe;
import com.neep.neepmeat.transport.api.pipe.PrecisePipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import com.neep.neepmeat.transport.fluid_network.node.BlockFluidPipeVertex;
import com.neep.meatlib.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.object.Color;

@SuppressWarnings("UnstableApiUsage")
public class FluidPipeBlock extends PrecisePipeBlock implements EntityBlock, FluidPipe
{
    public final PipeCol col;

    public FluidPipeBlock(RegistrationContext ctx, FluidPipe.PipeCol col, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings.factory(FluidPipeBlockItem::new), settings);
        this.col = col;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        FluidPipe.onStateReplaced(world, pos, state, newState, this);

        super.onRemove(state, world, pos, newState, moved);

        if (world.isClientSide())
            return;

        if (!state.is(newState.getBlock()))
        {
            removePipe((ServerLevel) world, state, pos);
//            updateNeighbourPipes(world, pos, state);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        BlockPos subtracted = fromPos.subtract(pos);
        Direction direction = Direction.fromDelta(subtracted.getX(), subtracted.getY(), subtracted.getZ());

        // WiredRedstone causes updates that originate in this block?
        BlockState nextState;
        if (direction == null)
            nextState = state;
        else
            nextState = updateShape(state, direction, world.getBlockState(fromPos), world, pos, fromPos);

        // Block state change must be applied to the world in order for PipeNetwork::discoverNodes to pick it up
        world.setBlock(pos, nextState, Block.UPDATE_CLIENTS);

        BlockState fromState = world.getBlockState(fromPos);

        // Neighbour state changes have already been applied, so there is no way of knowing if the update came from a
        // destroyed pipe.
        boolean foundPipe = FluidPipe.findFluidPipe(world, fromPos, fromState) != null;
        boolean nodeChange = false;
        if (!foundPipe)
        {
            // If the nodes have changed, we need to update the pipe.
            nodeChange = createStorageNodes(world, pos, nextState);
//            {
//                FluidPipeBlockEntity.find(world, pos).ifPresent(be -> be.updateConnectionChange(state, nextState));
//            }
        }
//        else if (!state.equals(nextState))
//        {
//            // The addition of a pipe can change this one's junction status
//        }
        if (foundPipe || nodeChange || !state.equals(nextState))
        {
            FluidPipeBlockEntity.find(world, pos).ifPresent(be -> be.updateConnectionChange(state, nextState));
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
//        BlockState updatedState = enforceApiConnections(world, pos, state);
//        world.setBlockState(pos, updatedState, Block.NOTIFY_ALL);
        if (!world.isClientSide())
        {
            createStorageNodes(world, pos, state);

            FluidPipeBlockEntity.find(world, pos).ifPresent(be -> be.updateHiddenConnections(state));
        }
    }

    @Override
    public void onConnectionUpdate(Level world, BlockState state, BlockState newState, BlockPos pos, Player entity)
    {
        if (world.isClientSide())
            return;

        createStorageNodes(world, pos, newState);
        FluidPipeBlockEntity.find(world, pos).ifPresent(be -> be.updateConnectionChange(state, newState));
//        FluidPipeBlockEntity.find(world, pos).ifPresent(be -> be.updateHiddenConnections(newState));
//        updateNeighbourPipes(world, pos, state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        if (state.getValue(WATERLOGGED))
        {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        // I know how manky this logic is, but my brain refuses to make it more readable.

        boolean alreadyConnected = getConnection(state, direction).isConnected();
        boolean canConnect = canConnectTo(state, neighborState, direction.getOpposite(), (Level) world, neighborPos, alreadyConnected);

        boolean otherIsPipe;
        boolean otherConnected;
        if (neighborState.getBlock() instanceof FluidPipeBlock neighborPipe)
        {
            otherConnected = neighborPipe.getConnection(neighborState, direction.getOpposite()).isConnected();
            otherIsPipe = true;
        }
        else
        {
            otherConnected = canConnect && alreadyConnected;
            otherIsPipe = false;
        }

        ConnectionType finalConnection;
        if (otherConnected || (!otherIsPipe && canConnect))
            finalConnection = ConnectionType.SIDE;
        else
            finalConnection = ConnectionType.NONE;

        return state.setValue(getConnection(direction), finalConnection);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.STICK))
        {
            if (!world.isClientSide() && world.getBlockEntity(pos) instanceof FluidPipeBlockEntity<?> be)
            {
                if (be.getPipeVertex() instanceof BlockFluidPipeVertex vertex && !vertex.canSimplify())
                {
                    player.sendSystemMessage(Component.nullToEmpty("Amount: " + vertex.getAmount() + ", Pump height: " + vertex.getPumpHeight() + ", Total height: " + vertex.getTotalHeight()));
                }
                else
                {
                    player.sendSystemMessage(Component.nullToEmpty("No vertex"));
                }
            }
            return InteractionResult.SUCCESS;
        }
        else if (stack.getItem() instanceof DyeItem dyeItem)
        {
            PipeCol newCol = PipeCol.get(dyeItem.getDyeColor());
            if (newCol != col)
            {
                FluidPipeBlock newPipe = FluidTransport.COLOURED_FLUID_PIPES.get(newCol);
                world.setBlockAndUpdate(pos, newPipe.withPropertiesOf(state));
                world.playSound(player, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1, 1);
                return InteractionResult.SUCCESS;
            }
            else
            {
                return InteractionResult.FAIL;
            }
        }

        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    protected boolean adjacentAutoConnect(Level world, BlockPos adjPos, BlockState adjState, BlockState hereState, Direction direction)
    {
        if (adjState.getBlock() instanceof FluidPipe pipe
                && canConnectTo(hereState, adjState, direction.getOpposite(), world, adjPos, true))
        {
            int adjConnections = pipe.countConnections(adjState);

            // There's no reason for placing a pipe in front of an axial pipe without wanting it to connect
            return adjConnections <= 1 || pipe instanceof AbstractAxialFluidPipe;
        }

        return adjState.getBlock() instanceof PumpBlock && adjState.getValue(PumpBlock.FACING).getAxis() == direction.getAxis();
    }

    @Override
    public boolean canConnectTo(BlockState hereState, BlockState toState, Direction toFace, Level world, BlockPos toPos, boolean newConnection)
    {
        if (toState.getBlock() instanceof FluidPipe otherPipe)
        {
            return col.matches(otherPipe.getCol(world, toPos, toState))
                    && otherPipe.connectInDirection(world, toPos, toState, toFace)
                    && (newConnection || FluidPipe.isConnectedIn(world, toPos, toState, toFace));
        }
        else if (toState.getBlock() instanceof PumpBlock pump
                && toState.getValue(PumpBlock.FACING).getAxis() == toFace.getAxis())
        {
            // Special handling for pumps
            return true;
        }
        else
        {
            return newConnection && FluidStorage.SIDED.find(world, toPos, toFace) != null;
        }
    }

    @Override
    protected boolean handCanChangeConnections(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction changeDirection)
    {
        return super.handCanChangeConnections(world, pos, state, player, hand, changeDirection);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.FLUID_PIPE.create(pos, state);
    }

    @Override
    public PipeCol getCol(Level world, BlockPos pos, BlockState blockState)
    {
        return col;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.FLUID_PIPE, ((world1, pos, state1, blockEntity) -> blockEntity.tick()), null, world);
    }

    @Environment(value= EnvType.CLIENT)
    public static int getTint(BlockState state, BlockAndTintGetter world, BlockPos pos, int index)
    {
        if (index == 1 && state.getBlock() instanceof FluidPipeBlock fluidPipeBlock)
        {
            int col = fluidPipeBlock.col.hexCode();
            Color first = Color.ofOpaque(col);
            Color second = Color.ofOpaque(0x844b2f);

            int d = 120;
            int d1 = 255 - d;
            Color mixed = Color.ofRGB(
                    (d * first.getRed() + d1 * second.getRed()) / 255,
                    (d * first.getGreen() + d1 * second.getGreen()) / 255,
                    (d * first.getBlue() + d1 * second.getBlue()) / 255
            );
            return mixed.getColor();
        }
        return 0xFFFFFF;
    }

    @Environment(value= EnvType.CLIENT)
    public static int getItemTint(ItemStack stack, int i)
    {
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof FluidPipeBlock fluidPipeBlock)
        {
            return fluidPipeBlock.col.hexCode();
//            Color first = Color.ofOpaque(col);
//            Color second = Color.ofOpaque(0x844b2f);
//
//            int d = 120;
//            int d1 = 255 - d;
//            Color mixed = Color.ofRGB(
//                    (d * first.getRed() + d1 * second.getRed()) / 255,
//                    (d * first.getGreen() + d1 * second.getGreen()) / 255,
//                    (d * first.getBlue() + d1 * second.getBlue()) / 255
//            );
//            return mixed.getColor();
        }
        return 0xFFFFFF;
    }
}