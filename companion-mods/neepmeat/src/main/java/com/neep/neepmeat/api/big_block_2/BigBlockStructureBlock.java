/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.big_block_2;

import com.mojang.serialization.MapCodec;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class BigBlockStructureBlock extends Block implements LivingMachineStructure {
    private final DeferredBlock<? extends BigBlock2> parent;
    private final boolean blood;

    public BigBlockStructureBlock(DeferredBlock<? extends BigBlock2> parent, boolean blood) {
        super(MeatlibBlockSettings.create().strength(2.0F).properties());
        this.parent = parent;
        this.blood = blood;
        registerDefaultState(defaultBlockState().setValue(BigBlock2.INDEX, 1).setValue(BigBlock2.AXIS, net.minecraft.core.Direction.Axis.Y));
    }

    public BlockPos origin(BlockPos pos, BlockState state) {
        return pos.subtract(parent.get().offsetOf(state));
    }

    public @Nullable BloodAcceptor blood(Level level, BlockPos pos) {
        if (!blood) {
            return null;
        }
        BlockState state = level.getBlockState(pos);
        Vec3i offset = parent.get().offsetOf(state);
        if (offset == null || !parent.get().volume(state).hasApi(offset, BigBlock2.BLOOD_API)) {
            return null;
        }
        BlockEntity entity = level.getBlockEntity(origin(pos, state));
        return entity instanceof BloodAcceptor acceptor ? acceptor : null;
    }

    @Override
    public EnumMap<StructureProperty, PropertyValue> getProperties() {
        return new EnumMap<>(StructureProperty.class);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BigBlock2.INDEX, BigBlock2.AXIS);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        super.onRemove(state, level, pos, newState, moved);
        if (newState.is(this)) {
            return;
        }
        BlockPos origin = origin(pos, state);
        if (level.getBlockState(origin).is(parent.get())) {
            level.destroyBlock(origin, false);
        }
    }
}
