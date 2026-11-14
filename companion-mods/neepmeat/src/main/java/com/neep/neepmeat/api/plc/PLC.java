/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.plc;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.memory.MemoryManager;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.robot.PLCActuator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface PLC extends VM {
    void addRobotAction(RobotAction action, PLCConsumer callback);

    PLCActuator getActuator();

    CompoundTag volatileStorage();

    default <T extends PLCActuator> void controlActuator(Class<T> clazz, ActionConsumer<T> consumer) throws NeepASM.RuntimeException {
        var actuator = getActuator();
        if (clazz.isAssignableFrom(actuator.getClass())) {
            consumer.accept(clazz.cast(actuator));
        } else {
            throw new NeepASM.RuntimeException("Current actuator does not support action");
        }
    }

    default <T extends PLCActuator, R> R controlActuator1(Class<T> clazz, ActionFunction<T, R> function) throws NeepASM.RuntimeException {
        var actuator = getActuator();
        if (clazz.isAssignableFrom(actuator.getClass())) {
            return function.accept(clazz.cast(actuator));
        }
        throw new NeepASM.RuntimeException("Current actuator does not support action");
    }

    default void controlActuator(Consumer<PLCActuator> consumer) {
        consumer.accept(getActuator());
    }

    void selectActuator(@Nullable BlockPos pos) throws NeepASM.RuntimeException;

    MemoryManager getMemory();

    void raiseError(Error error);

    @Override
    default void say(String message) {
        if (this instanceof BlockEntity be && be.getLevel() instanceof ServerLevel level) {
            BlockPos pos = be.getBlockPos();
            String text = "[PLC at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "] " + message;
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 20 * 20) {
                    player.sendSystemMessage(Component.literal(text));
                }
            }
        }
    }

    interface PLCProvider {
        PLC get();
    }

    @FunctionalInterface
    interface PLCConsumer {
        void accept(PLC plc) throws NeepASM.RuntimeException;
    }

    @FunctionalInterface
    interface ActionConsumer<T> {
        void accept(T t) throws NeepASM.RuntimeException;
    }

    @FunctionalInterface
    interface ActionFunction<T, R> {
        R accept(T t) throws NeepASM.RuntimeException;
    }

    class Error {
        private final Component what;

        public Error(Component what) {
            this.what = what;
        }

        public Error(NeepASM.RuntimeException exception) {
            this(exception.getMessage());
        }

        public Error(String what) {
            this.what = Component.literal(what);
        }

        public Component what() {
            return what;
        }
    }
}
