package com.neep.neepmeat.transport.block.item_transport;
import com.neep.neepmeat.machine.transport.ConnectedPipeBlock;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.FilterEntry;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.util.Wrenchable;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.block.item_transport.entity.WoodenHopperBlockEntity;
import com.neep.meatlib.transfer.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WoodenHopperBlock extends HopperBlock implements Wrenchable
{
    private static final VoxelShape TOP_SHAPE;
    private static final VoxelShape MIDDLE_SHAPE;
    private static final VoxelShape OUTSIDE_SHAPE;
    private static final VoxelShape DEFAULT_SHAPE;
    private static final VoxelShape DOWN_SHAPE;
    private static final VoxelShape EAST_SHAPE;
    private static final VoxelShape NORTH_SHAPE;
    private static final VoxelShape SOUTH_SHAPE;
    private static final VoxelShape WEST_SHAPE;

    private static final VoxelShape DEFAULT_COLLISION_SHAPE;
    private static final VoxelShape DOWN_COLLISION_SHAPE;
    private static final VoxelShape EAST_COLLISION_SHAPE;
    private static final VoxelShape NORTH_COLLISION_SHAPE;
    private static final VoxelShape SOUTH_COLLISION_SHAPE;
    private static final VoxelShape WEST_COLLISION_SHAPE;

    static
    {
        TOP_SHAPE = Block.box(0.0F, 10.0F, 0.0F, 16.0F, 16.0F, 16.0F);
        MIDDLE_SHAPE = Block.box(4.0F, 4.0F, 4.0F, 12.0F, 10.0F, 12.0F);
        OUTSIDE_SHAPE = Shapes.or(MIDDLE_SHAPE, TOP_SHAPE);
        DEFAULT_SHAPE = Shapes.join(OUTSIDE_SHAPE, Hopper.INSIDE, BooleanOp.ONLY_FIRST);
        DOWN_SHAPE = Shapes.or(DEFAULT_SHAPE, Block.box(5.0F, 0.0F, 5.0F, 11.0F, 4.0F, 11.0F));
        NORTH_SHAPE = Shapes.or(DEFAULT_SHAPE, Block.box(5.0F, 5.0F, 0.0F, 11.0F, 11.0F, 4.0F));
        SOUTH_SHAPE = Shapes.or(DEFAULT_SHAPE, Block.box(5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 16.0F));
        EAST_SHAPE = Shapes.or(DEFAULT_SHAPE, Block.box(12.0F, 5.0F, 5.0F, 16.0F, 11.0F, 11.0F));
        WEST_SHAPE = Shapes.or(DEFAULT_SHAPE, Block.box(0.0F, 5.0F, 5.0F, 4.0F, 11.0F, 11.0F));

        VoxelShape collisionMask = Block.box(0, 0, 0, 16, 15, 16);
        DEFAULT_COLLISION_SHAPE = Shapes.join(DEFAULT_SHAPE, collisionMask, BooleanOp.AND);
        DOWN_COLLISION_SHAPE = Shapes.join(DOWN_SHAPE, collisionMask, BooleanOp.AND);
        NORTH_COLLISION_SHAPE = Shapes.join(NORTH_SHAPE, collisionMask, BooleanOp.AND);
        SOUTH_COLLISION_SHAPE = Shapes.join(SOUTH_SHAPE, collisionMask, BooleanOp.AND);
        EAST_COLLISION_SHAPE = Shapes.join(EAST_SHAPE, collisionMask, BooleanOp.AND);
        WEST_COLLISION_SHAPE = Shapes.join(WEST_SHAPE, collisionMask, BooleanOp.AND);
    }

    public WoodenHopperBlock(RegistrationContext c, ItemSettings itemSettings, Properties settings)
    {
        super(settings);
        c.withItem(this, itemSettings);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(this))
        {
            if (!world.isClientSide() && world.getBlockEntity(pos) instanceof WoodenHopperBlockEntity be)
            {
                MeatlibStorageUtil.scatter(world, pos, be.getStorage(null));
                world.updateNeighbourForOutputSignal(pos, this);
            }
        }

        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
    {
        if (world.getBlockEntity(pos) instanceof WoodenHopperBlockEntity be)
        {
            return StorageUtil.calculateComparatorOutput(be.getStorage(null));
        }
        return 0;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.WOODEN_HOPPER.create(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.WOODEN_HOPPER, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(), null, world);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
        if (world.getBlockEntity(pos) instanceof WoodenHopperBlockEntity be && entity instanceof ItemEntity itemEntity)
        {
            be.onEntityCollided(itemEntity);
        }
    }

    @Override
    public boolean onWrench(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, BlockHitResult hitResult, ItemStack stack)
    {
        return Wrenchable.super.onWrench(world, pos, state, player, hand, hitResult, stack);
    }

    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return switch (state.getValue(FACING))
        {
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> DEFAULT_SHAPE;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return switch (state.getValue(FACING))
        {
            case DOWN -> DOWN_COLLISION_SHAPE;
            case NORTH -> NORTH_COLLISION_SHAPE;
            case SOUTH -> SOUTH_COLLISION_SHAPE;
            case WEST -> WEST_COLLISION_SHAPE;
            case EAST -> EAST_COLLISION_SHAPE;
            default -> DEFAULT_COLLISION_SHAPE;
        };
    }
}
