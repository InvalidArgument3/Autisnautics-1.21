/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.screen;

import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PLCMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final String source;
    private final String error;
    private final @Nullable PLCBlockEntity plc;

    public PLCMenu(int id, PLCBlockEntity plc) {
        super(NeepMachines.PLC_MENU.get(), id);
        this.plc = plc;
        this.pos = plc.getBlockPos();
        this.source = plc.source();
        this.error = plc.error();
    }

    public PLCMenu(int id, BlockPos pos, String source, String error, @Nullable PLCBlockEntity plc) {
        super(NeepMachines.PLC_MENU.get(), id);
        this.pos = pos;
        this.source = source;
        this.error = error;
        this.plc = plc;
    }

    public BlockPos pos() {
        return pos;
    }

    public String source() {
        return source;
    }

    public String error() {
        return error;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
    }
}
