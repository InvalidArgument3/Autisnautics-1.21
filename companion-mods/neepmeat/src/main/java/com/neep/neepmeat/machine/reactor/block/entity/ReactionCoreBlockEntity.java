/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 *
 * The receiver-organism flood fill is not scanned here. Organisation still steps with the upstream formula.
 */

package com.neep.neepmeat.machine.reactor.block.entity;

import com.neep.neepmeat.machine.EffectMachines;
import com.neep.neepmeat.machine.reactor.ReactionCoreParameters;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ReactionCoreBlockEntity extends BlockEntity {
    private final ReactionCoreParameters parameters = new ReactionCoreParameters();
    private int disruption;
    private float maxOrgIncrease;

    public ReactionCoreBlockEntity(BlockPos pos, BlockState state) {
        super(EffectMachines.REACTION_CORE_BE.get(), pos, state);
    }

    public void setOrganisation(double organisation, float cap) {
        parameters.setOrganisation(organisation, cap);
    }

    public double organisation() {
        return parameters.organisation();
    }

    public void setDisruptorParams(int disruption, float maxOrgIncrease) {
        this.disruption = disruption;
        this.maxOrgIncrease = maxOrgIncrease;
    }

    public void serverTick() {
        if (level != null && level.getGameTime() % 5 == 0) {
            parameters.tick(disruption, maxOrgIncrease, 5);
        }
        disruption = 0;
        maxOrgIncrease = 0;
    }
}
