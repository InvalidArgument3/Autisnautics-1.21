/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.plc;

import com.mojang.serialization.MapCodec;
import com.neep.meatlib.block.MeatlibBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Stores one program string. The assembler, robot arm, and NEEPASM executor are not ported.
 */
public class ProgramBoardBlock extends BaseEntityBlock {
    public static final MapCodec<ProgramBoardBlock> CODEC = simpleCodec(properties -> new ProgramBoardBlock());

    public ProgramBoardBlock() {
        super(MeatlibBlockSettings.create().strength(1.5F).properties());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ProgramBoardBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ProgramBoardBlockEntity board) {
            String held = player.getMainHandItem().getHoverName().getString();
            if (!player.getMainHandItem().isEmpty() && !held.isBlank()) {
                board.setProgram(held);
            }
            player.displayClientMessage(Component.literal(board.program().isBlank() ? "(empty program)" : board.program()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
