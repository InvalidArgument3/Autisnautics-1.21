/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * The driver pulls one item per ten ticks from an adjacent inventory and inserts
 * it into an adjacent item pipe with a route from PipeRouteFinder. The upstream
 * request scheduler (RoutingNetworkImpl) is not wired; path finding is.
 */

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
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import com.mojang.serialization.MapCodec;
import com.neep.meatlib.block.MeatlibBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class PipeDriverBlock extends BaseEntityBlock {
    public static final BooleanProperty VALID = BooleanProperty.create("valid");
    public static final MapCodec<PipeDriverBlock> CODEC = simpleCodec(properties -> new PipeDriverBlock());

    public PipeDriverBlock() {
        super(MeatlibBlockSettings.create().strength(1.5F).properties());
        registerDefaultState(defaultBlockState().setValue(VALID, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VALID);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeDriverBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof PipeDriverBlockEntity driver) {
                driver.serverTick();
            }
        };
    }
}
