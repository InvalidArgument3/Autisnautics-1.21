/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.block;

import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.util.Wrenchable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.registries.DeferredBlock;

public class BaseHorFacingBlock extends Block implements MeatlibBlock, Wrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BaseHorFacingBlock(MeatlibBlockSettings settings) {
        super(settings.properties());
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    public static DeferredBlock<BaseHorFacingBlock> register(RegistrationContext context, String path, MeatlibBlockSettings settings, ItemSettings items) {
        DeferredBlock<BaseHorFacingBlock> block = context.block(path, () -> new BaseHorFacingBlock(settings));
        context.item(path, () -> new BaseBlockItem(block.get(), items));
        return block;
    }

    @Override
    public BlockItem blockItem() {
        return (BlockItem) net.minecraft.world.item.Item.BY_BLOCK.get(this);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getPlayer() == null) {
            return defaultBlockState();
        }
        Direction facing = context.getPlayer().isShiftKeyDown()
                ? context.getHorizontalDirection().getOpposite()
                : context.getHorizontalDirection();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
