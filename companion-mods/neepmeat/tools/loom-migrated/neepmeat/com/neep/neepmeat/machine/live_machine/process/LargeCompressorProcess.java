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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.entity.LargeCompressorBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.Component;
import java.util.List;

public class LargeCompressorProcess implements Process
{
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.MOTOR_PORT,
            LivingMachineComponents.LARGE_COMPRESSOR
    );

    @Override
    public void serverTick(LivingMachineBlockEntity be)
    {
        be.withComponents(LivingMachineComponents.MOTOR_PORT, LivingMachineComponents.LARGE_COMPRESSOR).ifPresent(r ->
        {
            var motors = r.t1();
            var compressors = r.t2();

            LargeCompressorBlockEntity compressor = compressors.iterator().next();

            float progressIncrement = be.getProgressIncrement();

            long droplets = (long) (progressIncrement * NeepMeat.CONFIG.largeCompressor.powerToDroplets.getRealValue());

            try (Transaction transaction = Transaction.openOuter())
            {
                compressor.getOutputStorage().insert(NMFluids.COMPRESSED_AIR.variant(), droplets, transaction);
                transaction.commit();
            }
        });
    }

    @Override
    public List<ComponentType<?>> getRequired()
    {
        return REQUIRED;
    }

    @Override
    public Component getName()
    {
        return NeepMeat.translationKey("process", "large_compressor");
    }
}
