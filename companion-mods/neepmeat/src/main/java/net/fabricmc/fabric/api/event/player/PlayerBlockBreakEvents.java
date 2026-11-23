/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.event.player;

import com.neep.meatlib.event.RegisterEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public final class PlayerBlockBreakEvents {
    public static final RegisterEvent<Before> BEFORE = new RegisterEvent<>();
    public static final RegisterEvent<After> AFTER = new RegisterEvent<>();
    private PlayerBlockBreakEvents() {}
    @FunctionalInterface public interface Before { boolean beforeBlockBreak(ServerLevel world, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity); }
    @FunctionalInterface public interface After { void afterBlockBreak(ServerLevel world, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity); }
}
