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

package com.neep.neepmeat.entity.goal;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface Action extends NbtSerialisable
{
    default void init() {}

    void tick();

    @Override
    default CompoundTag writeNbt(CompoundTag nbt) {return nbt;}

    default void readNbt(CompoundTag nbt) {};

    @FunctionalInterface
    interface Sequence<T>
    {
        void tick(T parent, int counter);
    }

    static <E> Action empty(E e)
    {
        return new EmptyAction<>(e);
    }

    abstract class Simple<T extends Simple<T>> implements Action
    {
        private Sequence<T> sequence;
        private int counter = 0;

        @Override
        public void tick()
        {
            var oldSequence = sequence;

            if (sequence != null)
                sequence.tick((T) this, counter);

            if (oldSequence == sequence)
                ++counter;
        }

        public void setSequence(Sequence<T> sequence)
        {
            // Reset the tick counter and replace the current sequence
            this.sequence = sequence;
            counter = 0;
        }

        public Sequence<T> getSequence() { return sequence; }
    }

    class EmptyAction<E> implements Action
    {
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "empty");

        public EmptyAction(E entity)
        {

        }

        @Override
        public void tick() { }
    }
}
