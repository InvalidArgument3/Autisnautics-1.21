/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Upstream id vsc, the vascular source converter.
 * When it is drawing power, it removes min(internal, desired) from the network
 * and remembers that amount as power available to its source side.
 */

package com.neep.neepmeat.transport.block;

import com.mojang.serialization.MapCodec;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.neepmeat.transport.api.pipe.BalancingBloodAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VscBlock extends BaseEntityBlock {
    public static final MapCodec<VscBlock> CODEC = simpleCodec(properties -> new VscBlock());

    public VscBlock() {
        super(MeatlibBlockSettings.create().strength(2.0F).properties());
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
        return new VscBlockEntity(pos, state);
    }

    public static final class VscBlockEntity extends BlockEntity implements BalancingBloodAcceptor {
        private long desired = 400;
        private boolean cutOff;
        private long powerToSink = -1;
        private long powerToSource;

        public VscBlockEntity(BlockPos pos, BlockState state) {
            super(VascularConduits.VSC_BE.get(), pos, state);
        }

        public long powerToSource() {
            return powerToSource;
        }

        public void setDesired(long desired) {
            this.desired = desired;
        }

        @Override
        public long update(long internal) {
            if (powerToSink < 0) {
                if (internal >= desired || !cutOff) {
                    long minimum = Math.min(internal, desired);
                    powerToSource = minimum;
                    return -minimum;
                }
                powerToSource = 0;
                return 0;
            }
            powerToSource = desired;
            return powerToSink;
        }

        @Override
        public int priority() {
            return 0;
        }
    }
}
