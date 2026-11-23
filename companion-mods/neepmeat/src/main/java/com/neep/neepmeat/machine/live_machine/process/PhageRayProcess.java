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
 */

package com.neep.neepmeat.machine.live_machine.process;

import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.phage_ray.PhageRayBlockEntity;
import com.neep.neepmeat.machine.phage_ray.PhageRayEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PhageRayProcess implements Process {
    private static final List<ComponentType<?>> REQUIRED = List.of(LivingMachineComponents.PHAGE_RAY);

    @Override
    public void serverTick(LivingMachineBlockEntity be) {
        be.withComponents(LivingMachineComponents.PHAGE_RAY).ifPresent(result -> {
            PhageRayBlockEntity ray = (PhageRayBlockEntity) result.t1().iterator().next();
            ray.serverTick();
            boolean canHarvest = !be.getComponent(LivingMachineComponents.EXTRACTOR).isEmpty();
            boolean silkTouch = canHarvest && !be.getComponent(LivingMachineComponents.SILKY_EXTRACTOR).isEmpty();
            PhageRayEntity entity = ray.getTetheredEntity();
            if (entity == null) {
                return;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                entity.tickProcess(canHarvest, silkTouch, be.getCombinedItemOutput(), transaction);
                transaction.commit();
            }
        });
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return REQUIRED;
    }

    @Override
    public Component getName() {
        return NeepMeat.translationKey("process", "phage_ray");
    }

    public interface PhageRay {
        boolean trigger();
        Vec3 getBeamOrigin();
        Vec3 getClientBeamEnd(Vec3 start, float tickDelta);
        int getAge();
    }
}
