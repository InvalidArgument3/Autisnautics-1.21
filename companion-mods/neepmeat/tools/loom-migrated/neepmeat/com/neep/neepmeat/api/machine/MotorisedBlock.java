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

package com.neep.neepmeat.api.machine;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.machine.motor.MotorEntity;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static com.neep.neepmeat.api.PowerUtil.*;

public interface MotorisedBlock
{
    BlockApiLookup<MotorisedBlock, Void> LOOKUP = BlockApiLookup.get(
            new ResourceLocation(NeepMeat.NAMESPACE, "motorised_block"),
            MotorisedBlock.class, Void.class
    );

    boolean motorTick(MotorEntity motor);

    void setInputPower(float power);

    default void onMotorRemoved() {};

    default float getLoadTorque() { return PowerUtil.MOTOR_TORQUE_LOSS; }

    interface DiagnosticsProvider
    {
        @Nullable
        Diagnostics getDiagnostics();

        BlockApiLookup<DiagnosticsProvider, Void> LOOKUP = BlockApiLookup.get(
                new ResourceLocation(NeepMeat.NAMESPACE, "machine_diagnostics"),
                DiagnosticsProvider.class, Void.class
        );

        static void init()
        {
            LOOKUP.registerFallback((world, pos, state, blockEntity, context) ->
            {
                if (blockEntity instanceof DiagnosticsProvider d)
                    return d;

                return null;
            });
        }
    }

    class Diagnostics
    {
        protected CompoundTag nbt = new CompoundTag();

        public static Diagnostics insufficientPower(boolean problem, float power, float minPower)
        {
            Component title = Component.translatable("message.neepmeat.insufficient_power");
            Component message = Component.translatable("message.neepmeat.insufficient_power_2",
                    Component.literal(POWER_FORMAT.format(perUnitToAbsolute(power))).append(POWER_UNIT),
                    Component.literal(POWER_FORMAT.format(perUnitToAbsolute(minPower))).append(POWER_UNIT)
            );
            return new Diagnostics(problem, title, message);
        }

        public Diagnostics(boolean problem, Component title, Component message)
        {
            nbt.putBoolean("problem", problem);
            nbt.putString("title", title.getString());
            nbt.putString("message", message.getString());
        }

        public Diagnostics(CompoundTag nbt)
        {
            this.nbt = nbt;
        }

        public boolean problem()
        {
            return nbt.getBoolean("problem");
        }

        public Component title()
        {
            return Component.nullToEmpty(nbt.getString("title"));
        }

        public Component message()
        {
            return Component.nullToEmpty(nbt.getString("message"));
        }
        public CompoundTag nbt()
        {
            return nbt;
        }
    }
}
