/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.transport.block;

import com.neep.neepmeat.transport.blood_network.ConduitBloodNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VascularConduitBlockEntity extends BlockEntity {
    private @Nullable ConduitBloodNetwork network;

    public VascularConduitBlockEntity(BlockPos pos, BlockState state) {
        super(VascularConduits.CONDUIT_BE.get(), pos, state);
    }

    public @Nullable ConduitBloodNetwork network() {
        return network;
    }

    public void setNetwork(@Nullable ConduitBloodNetwork network) {
        this.network = network;
    }
}
