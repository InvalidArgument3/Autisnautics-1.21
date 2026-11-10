/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port of the basic item pipe connection rules. Filters, merge pipes,
 * drivers, and the routing-network cache are not in this class.
 */

package com.neep.neepmeat.machine.transport;

import com.mojang.serialization.MapCodec;
import com.neep.meatlib.block.MeatlibBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

public class ItemPipeBlock extends ConnectedPipeBlock {
    public static final MapCodec<ItemPipeBlock> CODEC = simpleCodec(properties -> new ItemPipeBlock());

    public ItemPipeBlock() {
        super(MeatlibBlockSettings.create().strength(0.8F).nonOpaque().properties());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean connectsTo(LevelAccessor level, BlockPos neighbourPos, BlockState neighbour, Direction fromNeighbour) {
        if (neighbour.getBlock() instanceof ItemPipeBlock) {
            return true;
        }
        return level instanceof Level real && real.getCapability(Capabilities.ItemHandler.BLOCK, neighbourPos, fromNeighbour) != null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemPipeBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof ItemPipeBlockEntity pipe) {
                pipe.serverTick();
            }
        };
    }
}
