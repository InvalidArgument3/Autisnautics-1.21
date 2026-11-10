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

package com.neep.meatlib.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * This is not intended for optimisation, but rather for situations where lazy initialisation is the only option.
 * An example could be a field in a BlockEntity that requires a World instance to be initialised.
 */
public interface LazySupplier<T> extends Supplier<T>
{
    @Override
    @NotNull
    T get();

    boolean isInitialised();

    void invalidate();

    static <T> LazySupplier<T> of(Supplier<T> supplier)
    {
        return new LazySupplierImpl<>(supplier);
    }

    class LazySupplierImpl<T> implements LazySupplier<T>
    {
        protected final Supplier<T> supplier;
        protected T object;
        public LazySupplierImpl(Supplier<T> supplier)
        {
            this.supplier = supplier;
        }

        @Override
        public @NotNull T get()
        {
            if (object == null)
            {
                object = supplier.get();
                Objects.requireNonNull(object);
            }

            return object;
        }

        @Override
        public boolean isInitialised()
        {
            return object != null;
        }

        @Override
        public void invalidate()
        {
            object = null;
        }
    }
}
