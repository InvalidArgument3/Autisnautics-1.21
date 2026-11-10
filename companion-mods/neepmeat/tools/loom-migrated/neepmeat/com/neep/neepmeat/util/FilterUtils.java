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

package com.neep.neepmeat.util;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;

import java.util.List;
import java.util.function.Predicate;

public class FilterUtils
{
    @FunctionalInterface
    public interface Filter
    {
        boolean test(long i1, long i2);
    }

    public static Predicate<ItemVariant> containsVariant(List<ItemVariant> list)
    {
        return list::contains;
    }

    public static Predicate<StorageView<ItemVariant>> matchVariant(List<StorageView<ItemVariant>> list)
    {
        return view -> ItemUtil.containsResource(list, view.getResource());
    }

    public static Predicate<StorageView<ItemVariant>> matchOperator(List<StorageView<ItemVariant>> list, Filter filter)
    {
        return stack -> ItemUtil.contains(list, stack, filter);
    }

    public static boolean any(TransferVariant<?> variant)
    {
        return true;
    }

}
