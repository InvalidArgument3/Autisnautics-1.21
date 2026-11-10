/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * A conduit connects to another conduit or to a blood acceptor on that face.
 * The first conduit creates a network. Later conduits join it, and two networks merge.
 */

package com.neep.neepmeat.transport.block;

import com.mojang.serialization.MapCodec;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.transport.blood_network.BloodNetworkManager;
import com.neep.neepmeat.transport.blood_network.ConduitBloodNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VascularConduitBlock extends BaseEntityBlock {
    public static final MapCodec<VascularConduitBlock> CODEC = simpleCodec(properties -> new VascularConduitBlock());

    public VascularConduitBlock() {
        super(MeatlibBlockSettings.create().strength(1.0F).properties());
    }

    public static boolean connects(Level level, BlockPos pos, Direction direction) {
        BlockPos neighbor = pos.relative(direction);
        BlockState state = level.getBlockState(neighbor);
        return state.getBlock() instanceof VascularConduitBlock || BloodAcceptor.find(level, neighbor) != null;
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
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof VascularConduitBlockEntity self)) {
            return;
        }
        List<ConduitBloodNetwork> adjacent = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (!connects(level, pos, direction)) {
                continue;
            }
            BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
            if (neighbor instanceof VascularConduitBlockEntity conduit && conduit.network() != null && !adjacent.contains(conduit.network())) {
                adjacent.add(conduit.network());
            }
        }
        if (adjacent.isEmpty()) {
            BloodNetworkManager.get(level).create((ServerLevel) level).add(self);
        } else {
            adjacent.get(0).add(self);
            for (int index = 1; index < adjacent.size(); index++) {
                adjacent.get(0).merge(adjacent.get(index));
            }
        }
    }
}
