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

package com.neep.neepmeat.api.plc;

import com.neep.neepmeat.api.plc.memory.MemoryManager;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.robot.PLCActuator;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface PLC extends VM
{
    void addRobotAction(RobotAction action, PLCConsumer callback);

    PLCActuator getActuator();

    CompoundTag volatileStorage();

//    default <T extends PLCActuator> Optional<T> getActuator(Class<T> clazz)
//    {
//        var actuator = getActuator();
//        if (clazz.isAssignableFrom(actuator.getClass()))
//            return Optional.of((T) actuator);
//
//        return Optional.empty();
//    }

    default <T extends PLCActuator> void controlActuator(Class<T> clazz, ActionConsumer<T> consumer) throws NeepASM.RuntimeException
    {
        var actuator = getActuator();
        if (clazz.isAssignableFrom(actuator.getClass()))
            consumer.accept((T) actuator);
        else
            throw new NeepASM.RuntimeException("Current actuator does not support action");
    }

    default <T extends PLCActuator, R> R controlActuator1(Class<T> clazz, ActionFunction<T, R> function) throws NeepASM.RuntimeException
    {
        var actuator = getActuator();
        if (clazz.isAssignableFrom(actuator.getClass()))
            return function.accept((T) actuator);
        else
            throw new NeepASM.RuntimeException("Current actuator does not support action");
    }

    default void controlActuator(Consumer<PLCActuator> consumer)
    {
        PLCActuator actuator = getActuator();
        consumer.accept(actuator);
    }

    /**
     * @param pos Position to look for actuator. Pass null to select the default actuator.
     */
    void selectActuator(@Nullable BlockPos pos) throws NeepASM.RuntimeException;

    MemoryManager getMemory();

    void raiseError(Error error);

    @Override
    default void say(String message)
    {
        if (this instanceof BlockEntity be)
        {
            BlockPos pos = be.getBlockPos();
            PlayerLookup.around((ServerLevel) be.getLevel(), pos, 20).forEach(p -> p.sendSystemMessage(
                    Component.nullToEmpty("[PLC at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "] " + message)));
        }
    }

    interface PLCProvider
    {
        PLC get();
    }

    @FunctionalInterface
    interface PLCConsumer
    {
        void accept(PLC plc) throws NeepASM.RuntimeException;
    }

    @FunctionalInterface
    interface ActionConsumer<T>
    {
        void accept(T t) throws NeepASM.RuntimeException;
    }

    @FunctionalInterface
    interface ActionFunction<T, R>
    {
        R accept(T t) throws NeepASM.RuntimeException;
    }

    class Error
    {
        private final Component what;

        public Error(Component what)
        {
            this.what = what;
        }

        public Error(NeepASM.RuntimeException e)
        {
            this(e.getMessage());
        }

        public Error(String what)
        {
            this.what = Component.nullToEmpty(what);
        }

        public Component what()
        {
            return what;
        }
    }
}
