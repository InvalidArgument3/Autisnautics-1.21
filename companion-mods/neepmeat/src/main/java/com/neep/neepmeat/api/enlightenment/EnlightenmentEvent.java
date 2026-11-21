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

package com.neep.neepmeat.api.enlightenment;

import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public interface EnlightenmentEvent
{
    default void spawn() {}
    void tick();

    boolean isRemoved();

    default @Nullable Factory getType() { return null; }

    interface Factory
    {
        EnlightenmentEvent create(ServerLevel world, ServerPlayer player);

        boolean willSpawn(ServerLevel world, ServerPlayer player);
    }

    class SimpleFactory implements Factory
    {
        private final BiFunction<Level, ServerPlayer, EnlightenmentEvent> factory;
        private final Predicate predicate;

        public SimpleFactory(BiFunction<Level, ServerPlayer, EnlightenmentEvent> factory)
        {
            this.factory = factory;
            this.predicate = (w, p) -> true;
        }

        public SimpleFactory(BiFunction<Level, ServerPlayer, EnlightenmentEvent> factory, Predicate predicate)
        {
            this.factory = factory;
            this.predicate = predicate;
        }

        @Override
        public EnlightenmentEvent create(ServerLevel world, ServerPlayer player)
        {
            return factory.apply(world, player);
        }

        @Override
        public boolean willSpawn(ServerLevel world, ServerPlayer player)
        {
            return predicate.test(world, player);
        }

        @FunctionalInterface
        public interface Predicate
        {
            boolean test(Level world, ServerPlayer player);
        }
    }
}
