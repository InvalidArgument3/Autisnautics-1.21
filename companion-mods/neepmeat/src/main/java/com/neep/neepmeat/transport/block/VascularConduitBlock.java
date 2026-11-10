/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Each face stores {@link com.neep.neepmeat.transport.fluid_network.ConnectionType}.
 * SIDE means the neighbour is a conduit or a blood acceptor. NONE means it is not.
 * Breaking a conduit splits the remaining pipes into new networks.
 */

package com.neep.neepmeat.transport.block;

import com.mojang.serialization.MapCodec;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.transport.blood_network.BloodNetworkManager;
import com.neep.neepmeat.transport.blood_network.ConduitBloodNetwork;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class VascularConduitBlock extends BaseEntityBlock {
    public static final EnumProperty<ConnectionType> NORTH = EnumProperty.create("north", ConnectionType.class);
    public static final EnumProperty<ConnectionType> SOUTH = EnumProperty.create("south", ConnectionType.class);
    public static final EnumProperty<ConnectionType> EAST = EnumProperty.create("east", ConnectionType.class);
    public static final EnumProperty<ConnectionType> WEST = EnumProperty.create("west", ConnectionType.class);
    public static final EnumProperty<ConnectionType> UP = EnumProperty.create("up", ConnectionType.class);
    public static final EnumProperty<ConnectionType> DOWN = EnumProperty.create("down", ConnectionType.class);
    public static final Map<Direction, EnumProperty<ConnectionType>> FACING = new EnumMap<>(Direction.class);
    public static final MapCodec<VascularConduitBlock> CODEC = simpleCodec(properties -> new VascularConduitBlock());

    static {
        FACING.put(Direction.NORTH, NORTH);
        FACING.put(Direction.SOUTH, SOUTH);
        FACING.put(Direction.EAST, EAST);
        FACING.put(Direction.WEST, WEST);
        FACING.put(Direction.UP, UP);
        FACING.put(Direction.DOWN, DOWN);
    }

    public VascularConduitBlock() {
        super(MeatlibBlockSettings.create().strength(1.0F).properties());
        BlockState state = defaultBlockState();
        for (EnumProperty<ConnectionType> property : FACING.values()) {
            state = state.setValue(property, ConnectionType.NONE);
        }
        registerDefaultState(state);
    }

    public static boolean connected(BlockState state, Direction direction) {
        return state.getValue(FACING.get(direction)).isConnected();
    }

    public static BlockState withConnections(Level level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            boolean link = level.getBlockState(neighbor).getBlock() instanceof VascularConduitBlock
                    || BloodAcceptor.find(level, neighbor) != null;
            state = state.setValue(FACING.get(direction), link ? ConnectionType.SIDE : ConnectionType.NONE);
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VascularConduitBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!level.isClientSide) {
            level.setBlock(pos, withConnections(level, pos, state), 3);
            join(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        if (!level.isClientSide) {
            level.setBlock(pos, withConnections(level, pos, state), 3);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof VascularConduitBlockEntity conduit && conduit.network() != null) {
            conduit.network().splitAround(pos);
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    private static void join(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof VascularConduitBlockEntity self) || self.network() != null) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        List<ConduitBloodNetwork> adjacent = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (!connected(state, direction)) {
                continue;
            }
            if (level.getBlockEntity(pos.relative(direction)) instanceof VascularConduitBlockEntity conduit
                    && conduit.network() != null && !adjacent.contains(conduit.network())) {
                adjacent.add(conduit.network());
            }
        }
        if (adjacent.isEmpty()) {
            BloodNetworkManager.get(level).create((ServerLevel) level).add(self);
        } else {
            adjacent.getFirst().add(self);
            for (int index = 1; index < adjacent.size(); index++) {
                adjacent.getFirst().merge(adjacent.get(index));
            }
        }
    }

    @SuppressWarnings("unused")
    private static StringRepresentable name(ConnectionType type) {
        return type;
    }
}
