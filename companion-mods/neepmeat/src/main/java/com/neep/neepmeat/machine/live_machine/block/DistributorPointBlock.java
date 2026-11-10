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
 *
 * Collision is the upstream pair of boxes. The structure is a data cable, so a search can pass through the disc.
 */

package com.neep.neepmeat.machine.live_machine.block;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.api.big_block.StructureBlockEntities;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPointBlockEntity;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DistributorPointBlock extends BigBlock<BigBlockStructure.Simple<BigBlockStructureEntity>> implements MeatlibBlock, EntityBlock, DataCable {
    public static final DirectionProperty VERTICAL_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
    private static final VoxelShape UP = Shapes.join(Block.box(-16, 0, -16, 32, 8, 32), Block.box(-8, 8, -8, 24, 16, 24), BooleanOp.OR);
    private static final VoxelShape DOWN = Shapes.join(Block.box(-16, 8, -16, 32, 16, 32), Block.box(-8, 0, -8, 24, 8, 24), BooleanOp.OR);
    private final BigBlockPattern pattern;

    public DistributorPointBlock(RegistrationContext ctx, Properties settings) {
        super(ctx, settings.noOcclusion());
        this.pattern = BigBlockPattern.makeOddCylinder(1, 0, 0, getStructure().defaultBlockState());
        registerDefaultState(getStateDefinition().any().setValue(VERTICAL_DIRECTION, Direction.UP));
    }

    @Override
    protected BigBlockStructure.Simple<BigBlockStructureEntity> registerStructureBlock(RegistrationContext ctx) {
        BlockEntityType[] box = new BlockEntityType[1];
        BigBlockStructure.BlockEntityRegisterererer<BigBlockStructureEntity> register = structure -> {
            box[0] = BlockEntityType.Builder.of((pos, state) -> new BigBlockStructureEntity(box[0], pos, state), structure).build(null);
            return StructureBlockEntities.register("distributor_point_structure", box[0]);
        };
        return ctx.append(this, new CableStructure(this, MeatlibBlockSettings.copyOf(properties), register,
                (pos, state) -> new BigBlockStructureEntity(box[0], pos, state)), "distributor_point_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState state) {
        return pattern;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return state.getValue(VERTICAL_DIRECTION) == Direction.UP ? UP : DOWN;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getNearestLookingVerticalDirection();
        Direction chosen = ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown() ? facing : facing.getOpposite();
        return defaultBlockState().setValue(VERTICAL_DIRECTION, chosen);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof DistributorPointBlockEntity point) {
            point.onPlaced();
        }
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClientSide() && !newState.is(this) && world.getBlockEntity(pos) instanceof DistributorPointBlockEntity point) {
            point.onBroken();
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    /** NeepBus write. "Send" queues a dispatch. "Set channel" replaces the address. */
    public void busWrite(Level world, BlockPos pos, String port, DataVariant value) {
        if (world.getBlockEntity(pos) instanceof DistributorPointBlockEntity point) {
            point.busWrite(port, value);
        }
    }

    @Override
    public BlockItem blockItem() {
        return (BlockItem) Item.BY_BLOCK.get(this);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DistributorPointBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VERTICAL_DIRECTION);
    }

    public static final class CableStructure extends BigBlockStructure.Simple<BigBlockStructureEntity> implements DataCable {
        public CableStructure(BigBlock<?> parent, Properties settings, BlockEntityRegisterererer<BigBlockStructureEntity> register,
                              java.util.function.BiFunction<BlockPos, BlockState, BigBlockStructureEntity> factory) {
            super(parent, settings, register, factory);
        }
    }
}
